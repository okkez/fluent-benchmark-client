package org.fluentd

import org.slf4j.LoggerFactory

class PeriodicalReporter(private val statistics: Statistics, private val interval: Int = 1000): Runnable {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)!!
    }

    override fun run() {
        var lastChecked = System.currentTimeMillis()
        while (true) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                break
            }
            var now = System.currentTimeMillis()
            if (now - lastChecked >= interval) {
                log.info("count={} total={} avg={}",
                        statistics.nEvents(), statistics.nTotalEvents(), statistics.average())
                lastChecked = now
            }
        }
    }
}