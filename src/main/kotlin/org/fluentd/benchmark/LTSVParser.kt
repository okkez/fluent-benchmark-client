package org.fluentd.benchmark

class LTSVParser: Parser<Map<String, Any>> {
    override fun parse(text: String, block: (Map<String, Any>) -> Unit) {
        val map = HashMap<String, Any>()
        text.splitToSequence("\t").forEach {
            val pair = it.split(Regex(""":"""), 2)
            map.put(pair[0], pair[1])
        }
        block(map)
    }
}