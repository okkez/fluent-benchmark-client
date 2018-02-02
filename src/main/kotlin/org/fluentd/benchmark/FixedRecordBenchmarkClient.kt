package org.fluentd.benchmark

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.komamitsu.fluency.Fluency
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class FixedRecordBenchmarkClient(
        override val host: String,
        override val port: Int,
        override val fluencyConfig: Fluency.Config,
        override val config: BenchmarkConfig): BenchmarkClient {

    override val fluency: Fluency = Fluency.defaultFluency(host, port, fluencyConfig)
    override var mainJob: Job? = null
    override lateinit var statistics: SendChannel<Statistics.Recorder>
    override val eventCounter: AtomicLong = AtomicLong()

    private val record = config.record()

    /**
     * @param interval The intervals in microseconds
     */
    override suspend fun emitEventsInInterval(interval: Long): Job = launch {
        when {
            config.nEvents < 1000 -> {
                for (i in 1L..config.nEvents) {
                    emitEvent(record)
                    delay(interval, TimeUnit.MICROSECONDS)
                }
                statistics.send(Statistics.Recorder.Finish)
                fluency.close()
            }
            else -> {
                var start = System.currentTimeMillis()
                for (i in 1L..config.nEvents) {
                    emitEvent(record)
                    if (i.rem(config.nEvents / 100) == 0L) {
                        val elapsed = (System.currentTimeMillis() - start) * 1000
                        val diff = interval * (config.nEvents / 100) - elapsed
                        if (diff > 0) {
                            delay(diff, TimeUnit.MICROSECONDS)
                        }
                        start = System.currentTimeMillis()
                    }
                }
                statistics.send(Statistics.Recorder.Finish)
                fluency.close()
            }
        }
    }

    override suspend fun emitEventsInFlood(): Job = launch {
        while (isActive) {
            emitEvent(record)
        }
        statistics.send(Statistics.Recorder.Finish)
        fluency.close()
    }
}
