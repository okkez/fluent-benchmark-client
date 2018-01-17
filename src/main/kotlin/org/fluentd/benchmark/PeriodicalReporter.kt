package org.fluentd.benchmark

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong

class PeriodicalReporter(private val statistics: SendChannel<Statistics.Recorder>,
                         private val eventCounter: AtomicLong,
                         private val interval: Long = 1000) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)!!
    }

    private var isRunning = false
    private lateinit var job: Job
    private var previous: Long = 0

    fun run() = runBlocking {
        job = launch {
            while (isActive) {
                val response = CompletableDeferred<Statistics>()
                statistics.send(Statistics.Recorder.Set(eventCounter.get()))
                statistics.send(Statistics.Recorder.Get(response))
                val s = response.await()
                val total = s.nEvents()
                log.info("count={} total={} avg={}",
                        total - previous, total, s.average())
                previous = total
                delay(interval)
            }
        }
    }

    fun stop() {
        if (job.isActive) {
            job.cancel()
        }
    }
}