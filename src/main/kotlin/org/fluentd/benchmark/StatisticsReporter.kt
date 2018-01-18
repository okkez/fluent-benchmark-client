package org.fluentd.benchmark

class StatisticsReporter(private val statistics: Statistics) {
    fun report() {
        println("""
            totalEvents: ${statistics.nEvents()}
            totalElapsed(sec): ${statistics.totalElapsedTime()}
            average(events/sec): ${statistics.average(statistics.totalElapsedTime())}
            """.trimIndent())
    }
}