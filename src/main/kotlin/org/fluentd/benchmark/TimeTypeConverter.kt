package org.fluentd.benchmark

import picocli.CommandLine

class TimeTypeConverter: CommandLine.ITypeConverter<Long> {
    override fun convert(value: String?): Long {
        if (value.isNullOrEmpty()) {
            return 0
        }
        val pattern = Regex("""\A(\d+)([smh])""")
        val m = pattern.matchEntire(value!!)
        if (m != null) {
            val digit = m.groupValues[1].toLong()
            val unit = when (m.groupValues[2]) {
                "s" -> 1
                "m" -> 60
                "h" -> 60 * 60
                else -> 1
            }
            return digit * unit
        }
        return 0
    }
}
