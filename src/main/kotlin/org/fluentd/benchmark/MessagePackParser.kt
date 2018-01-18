package org.fluentd.benchmark

class MessagePackParser: Parser<ByteArray> {
    override fun parse(text: String, block: (ByteArray) -> Unit) {
        block(text.trim().toByteArray())
    }
}