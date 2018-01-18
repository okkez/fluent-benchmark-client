package org.fluentd.benchmark

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.Assert.*
import org.msgpack.core.MessagePack

object BenchmarkConfigSpec: Spek({
    given("a benchmark config") {
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
        on("values") {
            it("tag equals to test.tag") {
                assertEquals("test.tag", config.tag)
            }
        }
        on("record") {
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
        on("parser") {
            it("is instance of LTSVParser") {
                assertTrue(config.parser() is LTSVParser)
            }
        }
    }

})
