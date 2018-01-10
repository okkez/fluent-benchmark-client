package org.fluentd

import org.komamitsu.fluency.Fluency

class BenchmarkClient(private val fluency: Fluency) {

    fun run() {
        fluency.emit("dummy.log", mapOf("message" to "Hello Kotlin!!"))
    }
}