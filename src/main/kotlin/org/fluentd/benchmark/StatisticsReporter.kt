package org.fluentd.benchmark

import org.slf4j.LoggerFactory

class StatisticsReporter(private val statistics: Statistics) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)!!
    }

    fun report() {
        log.info("\ntotalEvents: {}\ntotalElapsed(sec): {}\naverage(events/sec): {}",
                statistics.nEvents(),
                statistics.totalElapsedTime(),
                statistics.average(statistics.totalElapsedTime()))
    }
}
