package org.fluentd.benchmark

import org.msgpack.core.MessagePack
import java.nio.ByteBuffer

class LTSVParser: Parser<ByteBuffer> {
    override fun parse(text: String, block: (ByteBuffer) -> Unit) {
        val packer = MessagePack.newDefaultBufferPacker()
        val list = text.split("\t")
        packer.packMapHeader(list.size)
        list.forEach {
            val pair = it.split(Regex(""":"""), 2)
            packer.packString(pair[0])
            packer.packString(pair[1])
        }
        packer.close()
        block(ByteBuffer.wrap(packer.toByteArray()))
    }
}