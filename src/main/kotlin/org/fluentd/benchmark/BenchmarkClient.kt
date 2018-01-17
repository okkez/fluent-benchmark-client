package org.fluentd.benchmark

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.komamitsu.fluency.EventTime
import org.komamitsu.fluency.Fluency
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.floor

interface BenchmarkClient {

    enum class Mode {
        FIXED_INTERVAL,
        FIXED_PERIOD,
        FLOOD,
    }

    enum class FileFormat {
        LTSV,
        JSON,
        MessagePack,
    }

    enum class TimestampType {
        EventTime,
        Integer
    }

    val host: String
    val port: Int
    val fluencyConfig: Fluency.Config
    val config: BenchmarkConfig
    val fluency: Fluency // = Fluency.defaultFluency(host, port, fluencyConfig)
    var mainJob: Job
    var statistics: SendChannel<Statistics.Recorder>
    val eventCounter: AtomicLong

    companion object {
        fun create(init: Builder.() -> Unit) = Builder(init).build()
    }

    class Builder private constructor() {
        constructor(init: Builder.() -> Unit): this() {
            init()
        }

        lateinit var host: String
        var port: Int = 24224
        lateinit var fluencyConfig: Fluency.Config
        lateinit var benchmarkCofnig: BenchmarkConfig

        fun host(init: Builder.() -> String) = apply { host = init() }
        fun port(init: Builder.() -> Int) = apply { port = init() }
        fun fluencyConfig(init: Builder.() -> Fluency.Config) = apply { fluencyConfig = init() }
        fun benchmarkConfig(init: Builder.() -> BenchmarkConfig) = apply { benchmarkCofnig = init() }

        fun build(): BenchmarkClient {
            return when {
                benchmarkCofnig.inputFilePath.isNullOrEmpty() -> {
                    FixedRecordBenchmarkClient(host, port, fluencyConfig, benchmarkCofnig)
                }
                else -> {
                    DynamicRecordBenchmarkClient(host, port, fluencyConfig, benchmarkCofnig)
                }
            }
        }

    }

    fun run() = runBlocking {
        statistics = createStatistics()
        val reporter = PeriodicalReporter(statistics, eventCounter, TimeUnit.SECONDS.toMillis(config.reportInterval))
        mainJob = when(config.mode) {
            Mode.FIXED_INTERVAL -> {
                when {
                    config.interval != null && config.interval!! > 0 -> {
                        emitEventsInInterval(config.interval!! * TimeUnit.SECONDS.toMicros(1))
                    }
                    else -> emitEventsInInterval()
                }
            }
            Mode.FIXED_PERIOD -> emitEventsInPeriod()
            Mode.FLOOD -> emitEventsInFlood()
        }
        reporter.run()
        if (config.period != null && config.period!! > 0) {
            delay(config.period!!, TimeUnit.SECONDS)
            mainJob.cancel()
        }
        mainJob.join()
        reporter.stop()
    }

    suspend fun emitEventsInInterval(interval: Long = TimeUnit.SECONDS.toMicros(1)): Job
    suspend fun emitEventsInFlood(): Job
    suspend fun emitEventsInPeriod(): Job {
        return when {
            config.period != null && config.period!! > 0 -> {
                val interval = floor(config.period!!.toFloat() / config.nEvents * TimeUnit.MICROSECONDS.toMicros(1))
                emitEventsInInterval(interval.toLong())
            }
            else -> emitEventsInInterval()
        }
    }

    fun emitEvent(data: Map<String, Any>) {
        when (config.timestampType) {
            TimestampType.EventTime -> {
                fluency.emit(config.tag, EventTime.fromEpochMilli(System.currentTimeMillis()), data)
            }
            TimestampType.Integer -> {
                fluency.emit(config.tag, data)
            }
        }
        eventCounter.incrementAndGet()
    }

    fun emitEvent(data: ByteBuffer) {
        when (config.timestampType) {
            TimestampType.EventTime -> {
                fluency.emit(config.tag, EventTime.fromEpochMilli(System.currentTimeMillis()), data)
            }
            TimestampType.Integer -> {
                fluency.emit(config.tag, data)
            }
        }
        eventCounter.incrementAndGet()
    }

    fun stop() {
        if (mainJob.isActive) {
            mainJob.cancel()
        }
        if (!fluency.isTerminated) {
            fluency.close()
        }
    }

    suspend fun report() {
        val response = CompletableDeferred<Statistics>()
        statistics.send(Statistics.Recorder.Get(response))
        val s = response.await()
        val reporter = StatisticsReporter(s)
        reporter.report()
    }
}
