package org.fluentd.benchmark

import org.slf4j.LoggerFactory

class StatisticsReporter(private val statistics: Statistics) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)!!
    }

    fun report() {
        log.info("""

            totalEvents: ${statistics.nEvents()}
            totalElapsed(sec): ${statistics.totalElapsedTime()}
            average(events/sec): ${statistics.average(statistics.totalElapsedTime())}
            """.trimIndent())
    }
}