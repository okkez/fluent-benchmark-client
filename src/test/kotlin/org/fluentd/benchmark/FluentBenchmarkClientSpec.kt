package org.fluentd.benchmark

import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

object FluentBenchmarkClientSpec: Spek({
    given("command line arguments") {
        on("no arguments") {
            it("will run and stop properly") {
                val args = arrayOf("--dry-run")
                runBlocking {
                    FluentBenchmarkClient.main(args)
                }
                assertTrue(true)
            }
        }
    }
})
