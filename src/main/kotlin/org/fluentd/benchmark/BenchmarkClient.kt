package org.fluentd.benchmark

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel
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
    private lateinit var statistics: SendChannel<Statistics.Recorder>

    fun run() = runBlocking {
        statistics = createStatistics()
        val reporter = PeriodicalReporter(statistics)
        mainJob = launch {
            while (isActive) {
                emitEvent(mapOf("message" to "Hello Kotlin!!"))
                statistics.send(Statistics.Recorder.Update)
            }
            statistics.send(Statistics.Recorder.Finish)
            fluency.close()
        }
        reporter.run()
        delay(floodPeriod * 1000L)
        mainJob.cancel()
        reporter.stop()
        mainJob.join()
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
        if (mainJob.isActive) {
            mainJob.cancel()
        }
        if (!fluency.isTerminated) {
            fluency.close()
        }
    }

    suspend fun report() {
        val response = CompletableDeferred<Statistics>()
        statistics.send(Statistics.Recorder.Get(response))
        val s = response.await()
        val reporter = StatisticsReporter(s)
        reporter.report()
    }
}
