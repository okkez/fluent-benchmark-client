package org.fluentd

import org.komamitsu.fluency.EventTime
import org.komamitsu.fluency.Fluency
import kotlin.concurrent.thread

class BenchmarkClient(private val fluency: Fluency,
                      private val tag: String,
                      private val timestampType: FluentBenchmarkClient.TimestampType) {

    lateinit var statistics: Statistics

    fun run() {
        statistics = Statistics()
        val reporter = PeriodicalReporter(statistics)
        thread { reporter.run() }
        while (true) {
            emitEvent(mapOf("message" to "Hello Kotlin!!"))
            statistics.add(1)
        }
    }

    fun emitEvent(data: Map<String, Any>) {
        when (timestampType) {
            FluentBenchmarkClient.TimestampType.EventTime -> {
                fluency.emit(tag, EventTime.fromEpochMilli(System.currentTimeMillis()), data)
            }
            FluentBenchmarkClient.TimestampType.Integer -> {
                fluency.emit(tag, data)
            }
        }

    }
}
