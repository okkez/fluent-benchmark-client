package org.fluentd.benchmark

import org.slf4j.LoggerFactory

class PeriodicalReporter(private val statistics: Statistics, private val interval: Int = 1000): Runnable {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)!!
    }

    private var isRunning = false

    override fun run() {
        isRunning = true
        var lastChecked = System.currentTimeMillis()
        while (true) {
            if (!isRunning) {
                break
            }
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                break
            }
            val now = System.currentTimeMillis()
            if (now - lastChecked >= interval) {
                log.info("count={} total={} avg={}",
                        statistics.nEvents(), statistics.nTotalEvents(), statistics.average())
                lastChecked = now
            }
        }
    }

    fun stop() {
        isRunning = false
    }
}