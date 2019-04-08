package org.fluentd.benchmark

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.msgpack.core.MessagePack

object BenchmarkConfigSpec: Spek({
    describe("a benchmark config") {
        val config = BenchmarkConfig.create {
            tag = "test.tag"
            timestampType = BenchmarkClient.TimestampType.EventTime
            nEvents = 100
            interval = 100
            period = 100
            recordKey = "key"
            recordValue = "value"
            inputFileFormat = BenchmarkClient.FileFormat.LTSV
            inputFilePath = "/tmp/dummy.tsv"
            mode = BenchmarkClient.Mode.FLOOD
            reportInterval = 1000
        }
        context("values") {
            it("tag equals to test.tag") {
                assertEquals("test.tag", config.tag)
            }
        }
        context("record") {
            it("equals to {'key': 'value' }") {
                val unpacker = MessagePack.newDefaultUnpacker(config.record())
                val header = unpacker.unpackMapHeader()
                val key = unpacker.unpackString()
                val value = unpacker.unpackString()
                assertEquals(1, header)
                assertEquals("key", key)
                assertEquals("value", value)
            }
        }
        context("parser") {
            it("is instance of LTSVParser") {
                assertTrue(config.parser() is LTSVParser)
            }
        }
    }

})
