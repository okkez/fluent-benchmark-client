package org.fluentd.benchmark

import kotlinx.coroutines.*
import org.fluentd.benchmark.test.TestAppender
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

object PeriodicalReporterSpec: Spek({
    describe("periodical reporter") {
        beforeEachTest {
            TestAppender.events.clear()
        }
        context("run in 1sec w/ interval 100ms") {
            it("records 10 events") {
                runBlocking {
                    val statistics = Statistics.create() //createActor()
                    val counter = AtomicLong()
                    counter.set(1)
                    val reporter = PeriodicalReporter(statistics, counter, interval = 100)
                    delay(TimeUnit.SECONDS.toMillis(1))
                    launch {
                        repeat(10) {
                            counter.incrementAndGet()
                            delay(10)
                        }
                    }
                    val job = launch {
                        reporter.run()
                        delay(TimeUnit.SECONDS.toMillis(1))
                        reporter.stop()
                    }
                    job.join()
                    statistics.close()
                }
                val event = TestAppender.events.first()
                assertEquals(10, TestAppender.events.size)
                assertEquals(3, event.message.parameters.size)
            }
        }
    }
})
