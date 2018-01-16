package org.fluentd.benchmark

interface Parser<T> {
    fun parse(text: String, block: (T) -> Unit)
}
