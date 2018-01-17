package org.fluentd.benchmark

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.komamitsu.fluency.Fluency
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.buildSequence

class DynamicRecordBenchmarkClient(
        override val host: String,
        override val port: Int,
        override val fluencyConfig: Fluency.Config,
        override val config: BenchmarkConfig): BenchmarkClient {

    override val fluency: Fluency = Fluency.defaultFluency(host, port, fluencyConfig)
    override lateinit var mainJob: Job
    override lateinit var statistics: SendChannel<Statistics.Recorder>

    private val parser = config.parser()

    suspend override fun emitEventsInInterval(interval: Long): Job {
        return launch {
            while (isActive) {
                for (line in readLines()) {
                    parser.parse(line) {
                        emitEvent(it)
                    }
                    statistics.send(Statistics.Recorder.Update)
                    delay(interval, TimeUnit.MICROSECONDS)
                    val response = CompletableDeferred<Statistics>()
                    statistics.send(Statistics.Recorder.Get(response))
                    val s = response.await()
                    if (config.nEvents < s.nTotalEvents()) {
                        return@launch
                    }
                }
            }

        }
    }

    suspend override fun emitEventsInFlood(): Job {
        return launch {
            while (isActive) {
                for (line in readLines()) {
                    parser.parse(line) {
                        emitEvent(it)
                    }
                    statistics.send(Statistics.Recorder.Update)
                    if (!isActive) {
                        break
                    }
                }

            }
            statistics.send(Statistics.Recorder.Finish)
            fluency.close()
        }
    }

    suspend override fun emitEventsInPeriod(): Job {
        TODO("not implemented")
    }

    private fun readLines() = buildSequence {
        val reader = File(config.inputFilePath).bufferedReader()
        for (line in reader.lineSequence()) {
            yield(line)
        }
    }
}