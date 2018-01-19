package org.fluentd.benchmark

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.channels.actor
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class Statistics(val start: Instant = Instant.now()) {
    sealed class Recorder {
        class Set(val count: Long): Recorder()
        class Get(val response: CompletableDeferred<Statistics>): Recorder()
        object Finish: Recorder()
    }

    companion object {
        fun create()= actor<Statistics.Recorder> {
            val statistics = Statistics()
            for (message in channel) {
                when (message) {
                    is Statistics.Recorder.Set -> statistics.set(message.count)
                    is Statistics.Recorder.Get -> message.response.complete(statistics)
                    is Statistics.Recorder.Finish -> statistics.finish()
                }
            }
        }
    }

    private val counter = AtomicLong()

    var finish: Instant? = null
        get() {
            field = field ?: Instant.now()
            return field
        }

    fun set(count: Long): Long = counter.getAndSet(count)

    fun nEvents(): Long = counter.get()

    fun totalElapsedTime(): Float {
        finish ?: finish()
        return (finish!!.toEpochMilli() - start.toEpochMilli()).toFloat() / 1000
    }

    fun average(elapsed: Number = Instant.now().epochSecond - start.epochSecond): Float {
        return nEvents() / elapsed.toFloat()
    }

    fun finish() {
        finish = finish ?: Instant.now()
    }
}
