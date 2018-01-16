package org.fluentd.benchmark

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class JSONParser: Parser<Map<String, Any>> {
    override fun parse(text: String, block: (Map<String, Any>) -> Unit) {
        val objectMapper = jacksonObjectMapper()
        val map = objectMapper.readValue<Map<String, Any>>(text)
        block(map)
    }
}