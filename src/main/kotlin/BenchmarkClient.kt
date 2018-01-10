package org.fluentd

import org.komamitsu.fluency.EventTime
import org.komamitsu.fluency.Fluency

class BenchmarkClient(private val fluency: Fluency,
                      private val tag: String,
                      private val timestampType: FluentBenchmarkClient.TimestampType) {

    fun run() {
        while (true) {
            emitEvent(mapOf("message" to "Hello Kotlin!!"))
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
