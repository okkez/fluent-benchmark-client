package org.fluentd.benchmark

import org.msgpack.core.MessagePack
import java.io.ByteArrayOutputStream

class LTSVParser: Parser<ByteArray> {
    override fun parse(text: String, block: (ByteArray) -> Unit) {
        val buffer = ByteArrayOutputStream()
        val packer = MessagePack.newDefaultPacker(buffer)
        val list = text.trim().split("\t")
        packer.packMapHeader(list.size)
        list.forEach {
            val pair = it.split(Regex(""":"""), 2)
            packer.packString(pair[0])
            packer.packString(pair[1])
        }
        packer.flush()
        block(buffer.toByteArray())
    }
}