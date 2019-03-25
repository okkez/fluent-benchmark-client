package org.fluentd.benchmark

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.junit.jupiter.api.Assertions.assertEquals
import org.msgpack.core.MessagePack

object LTSVParserSpec: Spek({
    describe("a LTSV parser") {
        context("parse LTSV text") {
            val parser = LTSVParser()
            it("returns ByteArray that represents map") {
                parser.parse("key1:value1\tkey2:value2") {
                    val unpacker = MessagePack.newDefaultUnpacker(it)
                    assertEquals(2, unpacker.unpackMapHeader())
                    assertEquals("key1", unpacker.unpackString())
                    assertEquals("value1", unpacker.unpackString())
                    assertEquals("key2", unpacker.unpackString())
                    assertEquals("value2", unpacker.unpackString())
                }
            }
        }
    }
})