package org.fluentd.benchmark


import org.junit.jupiter.api.Assertions.assertEquals
import org.msgpack.core.MessagePack
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object JSONParserSpec: Spek({
    describe("a JSON parser") {
        context("parse JSON") {
            val parser = JSONParser()
            it("returns ByteArray represents map") {
                parser.parse("""{ "key1": "value1", "key2": "value2" }""") {
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
