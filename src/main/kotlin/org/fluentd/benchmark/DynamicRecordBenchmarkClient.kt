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

    override val fluency: Fluency = Fluency.defaultFluency(host, port, fluencyConfig)
    override lateinit var mainJob: Job
    override lateinit var statistics: SendChannel<Statistics.Recorder>
    override val eventCounter: AtomicLong = AtomicLong()

    private val parser = config.parser()
    private val records = mutableListOf<ByteArray>()

    override fun run() = runBlocking {
        prepareRecords()
        println(records.size)
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

    override suspend fun emitEventsInInterval(interval: Long): Job {
        return launch {
            while (isActive) {
                records.forEach {
                    emitEvent(it)
                    delay(interval, TimeUnit.MICROSECONDS)
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