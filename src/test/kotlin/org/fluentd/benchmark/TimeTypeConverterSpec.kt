package org.fluentd.benchmark

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.junit.jupiter.api.Assertions.assertEquals

object TimeTypeConverterSpec: Spek({
    describe("a converter") {
        hashMapOf(
                "1s" to 1L,
                "7s" to 7L,
                "1m" to 60L,
                "5m" to 300L,
                "1h" to 3600L,
                "2h" to 7200L
        ).forEach { value, expected ->
            context("convert $value") {
                val converter = TimeTypeConverter()
                it("returns $expected") {
                    assertEquals(expected, converter.convert(value))
                }
            }
        }
    }
})
