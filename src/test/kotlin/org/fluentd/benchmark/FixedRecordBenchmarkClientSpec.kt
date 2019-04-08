package org.fluentd.benchmark

import org.fluentd.benchmark.test.TestServer
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.junit.jupiter.api.Assertions.assertEquals
import org.komamitsu.fluency.Fluency

object FixedRecordBenchmarkClientSpec: Spek({
    describe("a benchmark client") {
//        context("runs in flood mode for 1 second") {
//            val fluencyConfig = Fluency.Config()
//            fluencyConfig.flushIntervalMillis = 100
//            val benchmarkConfig = BenchmarkConfig.create {
//                tag = "test.tag"
//                timestampType = BenchmarkClient.TimestampType.EventTime
//                nEvents = 10000
//                interval = null
//                period = 1 // sec
//                recordKey = "message"
//                recordValue = "This is test message."
//                inputFileFormat = BenchmarkClient.FileFormat.LTSV
//                inputFilePath = null
//                mode = BenchmarkClient.Mode.FLOOD
//                reportInterval = 200 // msec
//
//            }
//            val port = TestServer.unusedPort()
//            val client = FixedRecordBenchmarkClient("127.0.0.1", port, fluencyConfig, benchmarkConfig)
//            val server = TestServer(port)
//            it("processes a lot of events") {
//                server.run {
//                    client.run()
//                }
//                assertTrue(server.processedEvents() > 0L)
//                assertTrue(client.eventCounter.get() > 0L)
//                assertEquals(server.processedEvents(), client.eventCounter.get())
//                client.stop()
//            }
//        }
        context("runs in fixed period mode, processes 10000 events in 1 second") {
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
                inputFilePath = null
                mode = BenchmarkClient.Mode.FIXED_PERIOD
                reportInterval = 200 // msec

            }
            val port = TestServer.unusedPort()
            val client = FixedRecordBenchmarkClient("127.0.0.1", port, fluencyConfig, benchmarkConfig)
            val server = TestServer(port)
            it("processes 10000 events") {
                server.run(10000L) {
                    client.run()
                }
                assertEquals(10000L, client.eventCounter.get())
                assertEquals(10000L, server.processedEvents())
                client.stop()
            }
        }
        context("runs in fixed interval mode, processes 3 events") {
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
                inputFilePath = null
                mode = BenchmarkClient.Mode.FIXED_INTERVAL
                reportInterval = 200 // msec

            }
            val port = TestServer.unusedPort()
            val client = FixedRecordBenchmarkClient("127.0.0.1", port, fluencyConfig, benchmarkConfig)
            val server = TestServer(port)
            it("processes 3 events") {
                server.run(3L) {
                    client.run()
                }
                assertEquals(3L, client.eventCounter.get())
                assertEquals(3L, server.processedEvents())
                client.stop()
            }
        }
        context("runs in N events per second mode, processes 2000 events") {
            val fluencyConfig = Fluency.Config()
            fluencyConfig.flushIntervalMillis = 100
            val benchmarkConfig = BenchmarkConfig.create {
                tag = "test.tag"
                timestampType = BenchmarkClient.TimestampType.EventTime
                nEventsPerSec = 1000L
                interval = null // sec
                period = 2
                recordKey = "message"
                recordValue = "This is test message."
                inputFileFormat = BenchmarkClient.FileFormat.LTSV
                inputFilePath = null
                mode = BenchmarkClient.Mode.EVENTS_PER_SEC
                reportInterval = 200 // msec

            }
            val port = TestServer.unusedPort()
            val client = FixedRecordBenchmarkClient("127.0.0.1", port, fluencyConfig, benchmarkConfig)
            val server = TestServer(port)
            it("processes 2000 events") {
                server.run(2000L) {
                    client.run()
                }
                assertEquals(2000L, client.eventCounter.get())
                assertEquals(2000L, server.processedEvents())
                client.stop()
            }
        }
    }
})
