package org.fluentd.benchmark

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.fluentd.benchmark.test.TestAppender
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

object PeriodicalReporterSpec: Spek({
    given("periodical reporter") {
        beforeEachTest {
            TestAppender.events.clear()
        }
        on("run in 1sec w/ interval 100ms") {
            runBlocking {
                val statistics = Statistics.create()
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
            }
            it("records 10 events") {
                val event = TestAppender.events.first()
                assertEquals(10, TestAppender.events.size)
                assertEquals(3, event.message.parameters.size)
            }
        }
    }
})
