package org.fluentd.benchmark

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel
import org.komamitsu.fluency.EventTime
import org.komamitsu.fluency.Fluency
import java.nio.ByteBuffer

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
                    DynamicRecordBenchmarkClient(host, port, fluencyConfig, benchmarkCofnig)
                }
                else -> {
                    FixedRecordBenchmarkClient(host, port, fluencyConfig, benchmarkCofnig)
                }
            }
        }

    }

    fun run() = runBlocking {
        statistics = createStatistics()
        val reporter = PeriodicalReporter(statistics, config.reportInterval * 1000)
        mainJob = when(config.mode) {
            Mode.FIXED_INTERVAL -> {
                when {
                    config.interval != null && config.interval!! > 0 -> emitEventsInInterval(config.interval!!)
                    else -> emitEventsInInterval()
                }
            }
            Mode.FIXED_PERIOD -> emitEventsInPeriod()
            Mode.FLOOD -> emitEventsInFlood()
        }
        reporter.run()
        if (config.period != null && config.period!! > 0) {
            delay(config.period!! * 1000L)
            mainJob.cancel()
        }
        mainJob.join()
        reporter.stop()
    }

    suspend fun emitEventsInInterval(interval: Int = 1): Job
    suspend fun emitEventsInFlood(): Job
    suspend fun emitEventsInPeriod(): Job

    fun emitEvent(data: Map<String, Any>) {
        when (config.timestampType) {
            TimestampType.EventTime -> {
                fluency.emit(config.tag, EventTime.fromEpochMilli(System.currentTimeMillis()), data)
            }
            TimestampType.Integer -> {
                fluency.emit(config.tag, data)
            }
        }
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
