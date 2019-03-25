package org.fluentd.benchmark

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.junit.jupiter.api.Assertions.assertEquals

object SizeTypeConverterSpec: Spek({
    describe("a size converter") {
        hashMapOf(
                "1k" to 1024,
                "5K" to 5120,
                "1m" to 1048576,
                "5M" to 5242880,
                "1g" to 1073741824,
                "10" to 10
        ).forEach { value, expected ->
            context("convert $value") {
                val converter = SizeTypeConverter()
                it("returns $expected") {
                    assertEquals(expected, converter.convert(value))
                }
            }
        }

        context("convert 5G") {
            val value = "5G"
            val expected = 5368709120L
            val converter = SizeTypeConverter()
            it("returns $expected") {
                assertEquals(expected, converter.convert(value))
            }
        }
    }
})
