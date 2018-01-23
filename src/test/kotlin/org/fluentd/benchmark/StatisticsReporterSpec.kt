package org.fluentd.benchmark

import org.fluentd.benchmark.test.TestAppender
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.concurrent.TimeUnit

object StatisticsReporterSpec: Spek({
    given("StatisticsReporter") {
        beforeEachTest {
            TestAppender.events.clear()
        }
        on("report") {
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
