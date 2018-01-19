package org.fluentd.benchmark

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.assertEquals

object StatisticsSpec: Spek({
    given("statistics") {
        on("start and finish") {
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
