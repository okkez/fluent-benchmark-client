package org.fluentd.benchmark

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.komamitsu.fluency.Fluency
import java.util.concurrent.TimeUnit

class FixedRecordBenchmarkClient(
        override val host: String,
        override val port: Int,
        override val fluencyConfig: Fluency.Config,
        override val config: BenchmarkConfig): BenchmarkClient {

    override val fluency: Fluency = Fluency.defaultFluency(host, port, fluencyConfig)
    override lateinit var mainJob: Job
    override lateinit var statistics: SendChannel<Statistics.Recorder>

    override suspend fun emitEventsInInterval(interval: Long): Job {
        return launch {
            repeat(config.nEvents) {
                emitEvent(config.record())
                statistics.send(Statistics.Recorder.Update)
                delay(interval, TimeUnit.MICROSECONDS)
            }
            statistics.send(Statistics.Recorder.Finish)
            fluency.close()
        }
    }

    override suspend fun emitEventsInFlood(): Job {
        return launch {
            while (isActive) {
                emitEvent(config.record())
                statistics.send(Statistics.Recorder.Update)
            }
            statistics.send(Statistics.Recorder.Finish)
            fluency.close()
        }
    }
}