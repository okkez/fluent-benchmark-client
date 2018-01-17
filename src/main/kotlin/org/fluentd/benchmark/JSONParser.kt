package org.fluentd.benchmark

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.msgpack.jackson.dataformat.MessagePackFactory
import java.nio.ByteBuffer

class JSONParser: Parser<ByteBuffer> {
    override fun parse(text: String, block: (ByteBuffer) -> Unit) {
        val objectMapper = jacksonObjectMapper()
        val map = objectMapper.readValue<Map<String, Any>>(text.trim())
        val messagepackMapper = ObjectMapper(MessagePackFactory())
        block(ByteBuffer.wrap(messagepackMapper.writeValueAsBytes(map)))
    }
}