package org.fluentd.benchmark

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel
import org.komamitsu.fluency.EventTime
import org.komamitsu.fluency.Fluency

class BenchmarkClient(host: String,
                      port: Int,
                      fluencyConfig: Fluency.Config,
                      private val config: BenchmarkConfig) {

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
        val reporter = PeriodicalReporter(statistics, config.reportInterval * 1000)
        mainJob = when(config.mode) {
            Mode.FIXED_INTERVAL -> {
                when {
                    config.interval != null && config.interval > 0 -> emitEventsInInterval(config.interval)
                    else -> emitEventsInInterval()
                }
            }
            Mode.FIXED_PERIOD -> emitEventsInPeriod()
            Mode.FLOOD -> emitEventsInFlood()
        }
        reporter.run()
        if (config.period != null && config.period > 0) {
            delay(config.period * 1000L)
            mainJob.cancel()
        }
        mainJob.join()
        reporter.stop()
    }

    private suspend fun emitEventsInInterval(interval: Int = 1): Job {
        return launch {
            repeat(config.nEvents) {
                emitEvent(config.record())
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
                emitEvent(config.record())
                statistics.send(Statistics.Recorder.Update)
            }
            statistics.send(Statistics.Recorder.Finish)
            fluency.close()
        }
    }

    private suspend fun emitEventsInPeriod(): Job {
        return when {
            config.period != null && config.period > 0 -> emitEventsInInterval(config.nEvents / config.period)
            else -> emitEventsInInterval()
        }
    }

    private fun emitEvent(data: Map<String, Any>) {
        when (config.timestampType) {
            FluentBenchmarkClient.TimestampType.EventTime -> {
                fluency.emit(config.tag, EventTime.fromEpochMilli(System.currentTimeMillis()), data)
            }
            FluentBenchmarkClient.TimestampType.Integer -> {
                fluency.emit(config.tag, data)
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
