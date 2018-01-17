package org.fluentd.benchmark

import java.nio.ByteBuffer

class MessagePackParser: Parser<ByteBuffer> {
    override fun parse(text: String, block: (ByteBuffer) -> Unit) {
        block(ByteBuffer.wrap(text.trim().toByteArray()))
    }
}