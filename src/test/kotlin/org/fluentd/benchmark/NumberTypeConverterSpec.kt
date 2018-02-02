package org.fluentd.benchmark

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on
import org.junit.jupiter.api.Assertions.assertEquals

object NumberTypeConverterSpec: Spek({
    given("a size converter") {
        on("convert %s",
                data("1k", expected = 1000L),
                data("5K", expected = 5000L),
                data("1m", expected = 1000000L),
                data("5M", expected = 5000000L),
                data("1g", expected = 1000000000L),
                data("5G", expected = 5000000000L),
                data("1000", expected = 1000L)
        ) { value: String, expected: Number ->
            val converter = NumberTypeConverter()
            it("returns $expected") {
                assertEquals(expected, converter.convert(value))
            }
        }
    }
})
