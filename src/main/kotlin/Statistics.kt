package org.fluentd

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class Statistics(val start: Instant = Instant.now()) {
    private val counter = AtomicLong()
    private val totalCounter = AtomicLong()

    var finish: Instant? = null
        get() {
            field = field ?: Instant.now()
            return field
        }

    fun add(up: Long): Long {
        synchronized(this) {
            totalCounter.addAndGet(up)
            return counter.addAndGet(up)
        }
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

    fun finish() {
        finish = Instant.now()
    }

    fun format(): String {
        return ""
    }
}
