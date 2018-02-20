package org.fluentd.benchmark

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on

import org.junit.jupiter.api.Assertions.assertEquals

object SizeTypeConverterSpec: Spek({
    given("a size converter") {
        on("convert %s",
                data("1k", expected = 1024),
                data("5K", expected = 5120),
                data("1m", expected = 1048576),
                data("5M", expected = 5242880),
                data("1g", expected = 1073741824),
                data("10", expected = 10)
        ) { value: String, expected: Number ->
            val converter = SizeTypeConverter()
            it("returns $expected") {
                assertEquals(expected, converter.convert(value))
            }
        }
        on("convert %s",
                data("5G", expected = 5368709120L)
        ) {value: String, expected: Number ->
            val converter = SizeTypeConverter()
            it("returns $expected") {
                assertEquals(expected, converter.convert(value))
            }
        }
    }
})
