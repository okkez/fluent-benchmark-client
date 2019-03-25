package org.fluentd.benchmark

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.junit.jupiter.api.Assertions.assertEquals

object NumberTypeConverterSpec: Spek({
    describe("a size converter") {
        hashMapOf(
                "1k" to 1000,
                "5K" to 5000,
                "1m" to 1000000,
                "5M" to 5000000,
                "1g" to 1000000000,
                "1000" to 1000
                ).forEach { value, expected ->
            context("convert $value") {
                val converter = NumberTypeConverter()
                it("returns $expected") {
                    assertEquals(expected, converter.convert(value))
                }
            }
        }
        context("convert 5G") {
            val value = "5G"
            val expected = 5000000000L
            val converter = NumberTypeConverter()
            it("returns $expected") {
                assertEquals(expected, converter.convert(value))
            }
        }
    }
})
