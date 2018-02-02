package org.fluentd.benchmark

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.komamitsu.fluency.Fluency
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class DynamicRecordBenchmarkClient(
        override val host: String,
        override val port: Int,
        override val fluencyConfig: Fluency.Config,
        override val config: BenchmarkConfig): BenchmarkClient {

    companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)!!
    }

    override val fluency: Fluency = Fluency.defaultFluency(host, port, fluencyConfig)
    override var mainJob: Job? = null
    override lateinit var statistics: SendChannel<Statistics.Recorder>
    override val eventCounter: AtomicLong = AtomicLong()

    private val parser = config.parser()
    private val records = mutableListOf<ByteArray>()

    override fun run() = runBlocking {
        prepareRecords()
        log.info("Prepare {} records", records.size)
        super.run()
    }

    private fun prepareRecords() {
        val reader = File(config.inputFilePath).bufferedReader()
        reader.lines().forEach {
            parser.parse(it) {
                records.add(it)
            }
        }
    }

    override suspend fun emitEventsInInterval(interval: Long): Job = launch {
        if (config.nEvents < 1000) {
            while (isActive) {
                records.forEach {
                    emitEvent(it)
                    delay(interval, TimeUnit.MICROSECONDS)
                    if (config.nEvents <= eventCounter.get()) {
                        return@launch
                    }
                }
            }
        } else {
            var start = System.currentTimeMillis()
            while (isActive) {
                records.forEach {
                    emitEvent(it)
                    if (eventCounter.get().rem(config.nEvents / 100) == 0L) {
                        val elapsed = (System.currentTimeMillis() - start) * 1000
                        val diff = interval * (config.nEvents / 100) - elapsed
                        if (diff > 0) {
                            delay(diff, TimeUnit.MICROSECONDS)
                        }
                        start = System.currentTimeMillis()
                    }
                    if (config.nEvents <= eventCounter.get()) {
                        return@launch
                    }
                }
            }

        }
    }

    override suspend fun emitEventsInFlood(): Job {
        return launch {
            while (isActive) {
                records.forEach {
                    emitEvent(it)
                    if (!isActive) {
                        return@forEach
                    }
                }

            }
            statistics.send(Statistics.Recorder.Finish)
            fluency.close()
        }
    }
}