package org.fluentd.benchmark

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals

object StatisticsSpec: Spek({
    describe("statistics") {
        context("start and finish") {
            val statistics = Statistics()
            statistics.set(1000)
            TimeUnit.MILLISECONDS.sleep(200)
            statistics.finish()
            it("records start timestamp and finish timestamp") {
                assertEquals(0.2f, statistics.totalElapsedTime(), 0.005f)
                assertEquals(1000, statistics.nEvents())
                assertEquals(5000f, statistics.average(statistics.totalElapsedTime()), 50f)
            }
        }
    }
})
