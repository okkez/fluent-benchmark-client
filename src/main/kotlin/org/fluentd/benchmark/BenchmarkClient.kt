package org.fluentd.benchmark

import kotlinx.coroutines.experimental.*
import org.komamitsu.fluency.EventTime
import org.komamitsu.fluency.Fluency

class BenchmarkClient(host: String,
                      port: Int,
                      fluencyConfig: Fluency.Config,
                      private val tag: String,
                      private val timestampType: FluentBenchmarkClient.TimestampType,
                      private val floodPeriod: Int) {

    private val fluency = Fluency.defaultFluency(host, port, fluencyConfig)
    private lateinit var mainJob: Job

    fun run() = runBlocking {
        mainJob = launch {
            while (isActive) {
                emitEvent(mapOf("message" to "Hello Kotlin!!"))
            }
            println("Complete!")
        }
        delay(floodPeriod * 1000L)
        mainJob.cancel()
        mainJob.join()
        fluency.close()
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

    fun stop() {
        // mainJob.cancel()
    }
}
