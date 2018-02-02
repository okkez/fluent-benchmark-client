package org.fluentd.benchmark

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.komamitsu.fluency.Fluency
import java.time.Instant
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
                var newInterval = interval
                val nEventsPerSec = ((1.0 / interval) * TimeUnit.SECONDS.toMicros(1)).toLong()
                var needInterval = true
                for (i in 1L..config.nEvents) {
                    emitEvent(record)
                    if (i.rem(nEventsPerSec / 10) == 0L) {
                        val elapsedInMicros = (System.currentTimeMillis() - start) * 1000L
                        val diff = TimeUnit.MILLISECONDS.toMicros(100) - elapsedInMicros
                        if (diff > 0) {
                            delay(diff, TimeUnit.MICROSECONDS)
                            needInterval = true
                        } else {
                            newInterval = (newInterval * 0.9).toLong()
                            needInterval = false
                        }
                        start = System.currentTimeMillis()
                    } else {
                        if (needInterval) {
                            delay(newInterval, TimeUnit.MICROSECONDS)
                        }
                    }
                }
                statistics.send(Statistics.Recorder.Finish)
                fluency.close()
            }
        }
    }

    override suspend fun emitEventsPerSec(): Job = launch {
        var start = System.currentTimeMillis()
        var interval = TimeUnit.SECONDS.toMicros(1) / config.nEventsPerSec!!
        var needInterval = true
        fluency.use { _ ->
            while (isActive) {
                emitEvent(record)
                if (eventCounter.get().rem(config.nEventsPerSec / 10) == 0L) {
                    val elapsedInMicros = (System.currentTimeMillis() - start) * 1000L
                    val diff = TimeUnit.MILLISECONDS.toMicros(100) - elapsedInMicros
                    if (diff > 0) {
                        delay(diff, TimeUnit.MICROSECONDS)
                        needInterval = true
                    } else {
                        interval = (interval * 0.9).toLong()
                        needInterval = false
                    }
                    start = System.currentTimeMillis()
                } else {
                    if (needInterval) {
                        delay(interval, TimeUnit.MICROSECONDS)
                    }
                }
            }
        }
    }

    override suspend fun emitEventsInFlood(): Job = launch {
        try {
            while (isActive) {
                emitEvent(record)
            }
        } finally {
            statistics.send(Statistics.Recorder.Finish)
            fluency.close()
        }
    }
}
