package org.fluentd.benchmark

import org.fluentd.benchmark.test.TestServer
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.komamitsu.fluency.Fluency

object DynamicRecordBenchmarkClientSpec: Spek({
    given("a benchmark client") {
        on("runs in flood mode in 1 second") {
            val fluencyConfig = Fluency.Config()
            fluencyConfig.flushIntervalMillis = 100
            val benchmarkConfig = BenchmarkConfig.create {
                tag = "test.tag"
                timestampType = BenchmarkClient.TimestampType.EventTime
                nEvents = 10000
                interval = null
                period = 1 // sec
                recordKey = "message"
                recordValue = "This is test message."
                inputFileFormat = BenchmarkClient.FileFormat.LTSV
                inputFilePath = javaClass.classLoader.getResource("dummy.ltsv").path
                mode = BenchmarkClient.Mode.FLOOD
                reportInterval = 200 // msec
            }
            val client = DynamicRecordBenchmarkClient("127.0.0.1", 24224, fluencyConfig, benchmarkConfig)
            val server = TestServer()
            it("processes a lot of events") {
                server.run {
                    client.run()
                }
                assertTrue(server.processedEvents() > 0L)
                assertTrue(client.eventCounter.get() > 0L)
                assertEquals(server.processedEvents(), client.eventCounter.get())
            }
        }
        on("runs in fixed period mode, processes 10000 events in 1 second") {
            val fluencyConfig = Fluency.Config()
            fluencyConfig.flushIntervalMillis = 100
            val benchmarkConfig = BenchmarkConfig.create {
                tag = "test.tag"
                timestampType = BenchmarkClient.TimestampType.EventTime
                nEvents = 10000
                interval = null
                period = 1 // sec
                recordKey = "message"
                recordValue = "This is test message."
                inputFileFormat = BenchmarkClient.FileFormat.LTSV
                inputFilePath = javaClass.classLoader.getResource("dummy.ltsv").path
                mode = BenchmarkClient.Mode.FIXED_PERIOD
                reportInterval = 200 // msec

            }
            val client = DynamicRecordBenchmarkClient("127.0.0.1", 24224, fluencyConfig, benchmarkConfig)
            val server = TestServer()
            it("processes 10000 events") {
                server.run(10000L) {
                    client.run()
                }
                assertEquals(10000L, client.eventCounter.get())
                assertEquals(10000L, server.processedEvents())
            }
        }
        on("runs in fixed interval mode, processes 3 events") {
            val fluencyConfig = Fluency.Config()
            fluencyConfig.flushIntervalMillis = 100
            val benchmarkConfig = BenchmarkConfig.create {
                tag = "test.tag"
                timestampType = BenchmarkClient.TimestampType.EventTime
                nEvents = 3
                interval = 1 // sec
                period = null
                recordKey = "message"
                recordValue = "This is test message."
                inputFileFormat = BenchmarkClient.FileFormat.LTSV
                inputFilePath = javaClass.classLoader.getResource("dummy.ltsv").path
                mode = BenchmarkClient.Mode.FIXED_INTERVAL
                reportInterval = 200 // msec

            }
            val client = DynamicRecordBenchmarkClient("127.0.0.1", 24224, fluencyConfig, benchmarkConfig)
            val server = TestServer()
            it("processes 3 events") {
                server.run(3L) {
                    client.run()
                }
                assertEquals(3L, client.eventCounter.get())
                assertEquals(3L, server.processedEvents())
            }
        }
    }
})