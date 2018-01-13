package org.fluentd.benchmark

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class Statistics(val start: Instant = Instant.now()) {
    sealed class Recorder {
        object Update: Recorder()
        class Get(val response: CompletableDeferred<Statistics>): Recorder()
        object Finish: Recorder()
    }

    private val counter = AtomicLong()
    private val totalCounter = AtomicLong()

    var finish: Instant? = null
        get() {
            field = field ?: Instant.now()
            return field
        }

    fun add(up: Long = 1): Long {
            totalCounter.addAndGet(up)
            return counter.addAndGet(up)
    }

    fun nEvents(clear: Boolean = true): Long {
        return when {
            clear -> counter.getAndSet(0)
            else -> counter.get()
        }
    }

    fun nTotalEvents(): Long {
        return totalCounter.get()
    }

    fun totalElapsedTime(): Float {
        finish ?: finish()
        return (finish!!.toEpochMilli() - start.toEpochMilli()).toFloat() / 1000
    }

    fun average(elapsed: Long = Instant.now().epochSecond - start.epochSecond): Float {
        return nTotalEvents() / elapsed.toFloat()
    }

    fun finish() {
        finish = finish ?: Instant.now()
    }

    fun format(): String {
        return ""
    }
}

fun createStatistics() = actor<Statistics.Recorder> {
    val statistics = Statistics()
    for (message in channel) {
        when (message) {
            is Statistics.Recorder.Update -> statistics.add()
            is Statistics.Recorder.Get -> message.response.complete(statistics)
            is Statistics.Recorder.Finish -> statistics.finish()
        }
    }
}
