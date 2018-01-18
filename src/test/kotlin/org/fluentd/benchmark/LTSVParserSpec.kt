package org.fluentd.benchmark

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.jupiter.api.Assertions.assertEquals
import org.msgpack.core.MessagePack

object LTSVParserSpec: Spek({
    given("a LTSV parser") {
        on("parse LTSV text") {
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