fun sizeToLong(size: String): Long {
    val pattern = Regex("""\A(\d+)([kKmMgG])\z""")
    val m = pattern.matchEntire(size)
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

fun sizeToInt(size: String): Int {
    return sizeToLong(size).toInt()
}


fun timeToInt(interval: String): Int {
    val pattern = Regex("""\A(\d+)([smh])""")
    val m = pattern.matchEntire(interval)
    if (m != null) {
        val digit = m.groupValues[1].toInt()
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
