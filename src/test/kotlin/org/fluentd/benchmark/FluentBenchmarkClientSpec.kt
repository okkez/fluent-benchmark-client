package org.fluentd.benchmark

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.junit.jupiter.api.Assertions.*
import picocli.CommandLine

object FluentBenchmarkClientSpec: Spek({
    describe("command line arguments") {
        hashMapOf(arrayOf("--dry-run") to true).forEach { args, expected ->
            context("no arguments") {
                it("will run and stop properly") {
                    CommandLine.run(FluentBenchmarkClient(), System.err,*args)
                    assertTrue(expected)
                }
            }
        }
        hashMapOf(
                arrayOf("--dry-run", "--period=1s", "--interval=1") to
                        "--interval conflict with --period",
                arrayOf("--dry-run", "--flood=1s", "--interval=1") to
                        "--flood conflict with --interval and --period",
                arrayOf("--dry-run", "--flood=1s", "--period=1s") to
                        "--flood conflict with --interval and --period",
                arrayOf("--dry-run", "--input-file=/tmp/dummy.log", "--record-key=dummy") to
                        "--input-file-path conflict with --record-key and --record-value",
                arrayOf("--dry-run", "--input-file=/tmp/dummy.log", "--record-value=dummy") to
                        "--input-file-path conflict with --record-key and --record-value"
        ).forEach { args, message ->
            context("conflict arguments") {
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
    }
})
