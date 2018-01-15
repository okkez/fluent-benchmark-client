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
                      private val nEvents: Int,
                      private val interval: Int?,
                      private val period: Int?,
                      private val recordKey: String,
                      private val recordValue: String,
                      private val mode: Mode,
                      private val reportInterval: Int) {

    enum class Mode {
        FIXED_INTERVAL,
        FIXED_PERIOD,
        FLOOD,
    }

    private val fluency = Fluency.defaultFluency(host, port, fluencyConfig)
    private lateinit var mainJob: Job
    private lateinit var statistics: SendChannel<Statistics.Recorder>

    fun run() = runBlocking {
        statistics = createStatistics()
        val reporter = PeriodicalReporter(statistics, reportInterval * 1000)
        mainJob = when(mode) {
            Mode.FIXED_INTERVAL -> {
                when {
                    interval != null && interval > 0 -> emitEventsInInterval(interval)
                    else -> emitEventsInInterval()
                }
            }
            Mode.FIXED_PERIOD -> emitEventsInPeriod()
            Mode.FLOOD -> emitEventsInFlood()
        }
        reporter.run()
        if (period != null && period > 0) {
            delay(period * 1000L)
            mainJob.cancel()
        }
        mainJob.join()
        reporter.stop()
    }

    private suspend fun emitEventsInInterval(interval: Int = 1): Job {
        return launch {
            repeat(nEvents) {
                emitEvent(mapOf(recordKey to recordValue))
                statistics.send(Statistics.Recorder.Update)
                delay(interval * 1000L)
            }
            statistics.send(Statistics.Recorder.Finish)
            fluency.close()
        }
    }

    private suspend fun emitEventsInFlood(): Job {
        return launch {
            while (isActive) {
                emitEvent(mapOf(recordKey to recordValue))
                statistics.send(Statistics.Recorder.Update)
            }
            statistics.send(Statistics.Recorder.Finish)
            fluency.close()
        }
    }

    private suspend fun emitEventsInPeriod(): Job {
        return when {
            period != null && period > 0 -> emitEventsInInterval(nEvents / period)
            else -> emitEventsInInterval()
        }
    }

    private fun emitEvent(data: Map<String, Any>) {
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
