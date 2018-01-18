package org.fluentd.benchmark

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.msgpack.jackson.dataformat.MessagePackFactory

class JSONParser: Parser<ByteArray> {
    override fun parse(text: String, block: (ByteArray) -> Unit) {
        val objectMapper = jacksonObjectMapper()
        val map = objectMapper.readValue<Map<String, Any>>(text.trim())
        val messagepackMapper = ObjectMapper(MessagePackFactory())
        block(messagepackMapper.writeValueAsBytes(map))
    }
}