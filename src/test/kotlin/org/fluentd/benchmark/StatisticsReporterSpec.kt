package org.fluentd.benchmark

import org.apache.logging.log4j.core.LogEvent
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
            it("displays statistics") {
                val statistics = Statistics()
                statistics.set(1000)
                TimeUnit.MILLISECONDS.sleep(200L)
                statistics.finish()
                val reporter = StatisticsReporter(statistics)
                reporter.report()
                val event: LogEvent = TestAppender.events.first()
                assertEquals(1, TestAppender.events.size)
                assertEquals(3, event.message.parameters.size)
                assertEquals("\ntotalEvents: 1000\ntotalElapsed(sec): 0.2\naverage(events/sec): 5000.0", event.message.formattedMessage)
            }
        }
    }
})
