package org.fluentd.benchmark

import org.fluentd.benchmark.test.TestAppender
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.concurrent.TimeUnit

object StatisticsReporterSpec: Spek({
    describe("StatisticsReporter") {
        beforeEachTest {
            TestAppender.events.clear()
        }
        context("report") {
            val statistics = Statistics()
            statistics.set(1000)
            TimeUnit.MILLISECONDS.sleep(200L)
            statistics.finish()
            val reporter = StatisticsReporter(statistics)
            it("displays statistics") {
                reporter.report()
                val event = TestAppender.events.first()
                assertEquals(1, TestAppender.events.size)
                assertEquals(3, event.message.parameters.size)
                val (nEvents, elapsed, average) = event.message.parameters
                assertEquals(1000L, nEvents)
                assertEquals(0.2f, elapsed as Float, 0.01f)
                assertEquals(5000.0f, average as Float, 30f)
            }
        }
    }
})
