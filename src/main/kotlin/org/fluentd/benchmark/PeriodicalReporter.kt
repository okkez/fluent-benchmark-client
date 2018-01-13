package org.fluentd.benchmark

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.SendChannel
import org.slf4j.LoggerFactory

class PeriodicalReporter(private val statistics: SendChannel<Statistics.Recorder>, private val interval: Int = 1000) {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)!!
    }

    private var isRunning = false
    private lateinit var job: Job

    fun run() = runBlocking {
        job = launch {
            while (isActive) {
                val response = CompletableDeferred<Statistics>()
                statistics.send(Statistics.Recorder.Get(response))
                val s = response.await()
                log.info("count={} total={} avg={}",
                        s.nEvents(), s.nTotalEvents(), s.average())
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