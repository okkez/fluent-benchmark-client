package org.fluentd.benchmark

import picocli.CommandLine

class SizeTypeConverter: CommandLine.ITypeConverter<Number> {
    override fun convert(value: String?): Number {
        if (value.isNullOrEmpty()) {
            return 0
        }
        val pattern = Regex("""\A(\d+)([kKmMgG])?\z""")
        val m = pattern.matchEntire(value!!)
        if (m != null) {
            val digit = m.groupValues[1].toLong()
            val unit: Long = when (m.groupValues[2]) {
                "k", "K" -> 1024
                "m", "M" -> 1024 * 1024
                "g", "G" -> 1024 * 1024 * 1024
                else -> 1
            }
            return digit * unit
        }

        return 0
    }
}
