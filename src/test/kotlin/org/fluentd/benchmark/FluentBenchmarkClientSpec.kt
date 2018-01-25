package org.fluentd.benchmark

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on
import org.junit.jupiter.api.Assertions.*
import picocli.CommandLine

object FluentBenchmarkClientSpec: Spek({
    given("command line arguments") {
        on("no arguments",
                data(arrayOf("--dry-run"), true)
        ) { args, expected ->
            it("will run and stop properly") {
                CommandLine.run(FluentBenchmarkClient(), System.err,*args)
                assertTrue(expected)
            }
        }
        on("conflict arguments",
                data(arrayOf("--dry-run", "--period=1s", "--interval=1"),
                        "--interval conflict with --period"),
                data(arrayOf("--dry-run", "--flood=1s", "--interval=1"),
                        "--flood conflict with --interval and --period"),
                data(arrayOf("--dry-run", "--flood=1s", "--period=1s"),
                        "--flood conflict with --interval and --period"),
                data(arrayOf("--dry-run", "--input-file-path=/tmp/dummy.log", "--record-key=dummy"),
                        "--input-file-path conflict with --record-key and --record-value"),
                data(arrayOf("--dry-run", "--input-file-path=/tmp/dummy.log", "--record-value=dummy"),
                        "--input-file-path conflict with --record-key and --record-value")
        ) { args, message ->
            it("will stop abnormally") {
                try {
                    CommandLine.run(FluentBenchmarkClient(), System.err,*args)
                    fail<String>("Failed")
                } catch (ex: CommandLine.ExecutionException) {
                    assertEquals(message, ex.cause!!.message)
                }
            }
        }
    }
})
