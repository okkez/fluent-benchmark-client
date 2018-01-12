package org.fluentd.benchmark

import kotlin.math.ceil

class StatisticsReporter(private val statistics: Statistics) {
    fun report() {
        println("""
            totalEvents: ${statistics.nTotalEvents()}
            totalElapsed(sec): ${statistics.totalElapsedTime()}
            average(events/sec): ${statistics.average(ceil(statistics.totalElapsedTime()).toLong())}
            """.trimIndent())
    }
}