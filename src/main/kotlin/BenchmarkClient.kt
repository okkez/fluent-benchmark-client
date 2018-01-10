package org.fluentd

import org.komamitsu.fluency.Fluency

class BenchmarkClient(private val fluency: Fluency, private val tag: String) {

    fun run() {
        while (true) {
            fluency.emit(tag, mapOf("message" to "Hello Kotlin!!"))
        }
    }
}
