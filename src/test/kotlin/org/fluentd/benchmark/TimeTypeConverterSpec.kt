package org.fluentd.benchmark

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on
import org.junit.jupiter.api.Assertions.assertEquals

object TimeTypeConverterSpec: Spek({
    given("a converter") {
        on("convert %s",
                data("1s", expected = 1L),
                data("7s", expected = 7L),
                data("1m", expected = 60L),
                data("5m", expected = 300L),
                data("1h", expected = 3600L),
                data("2h", expected = 7200L)
        ) { value, expected ->
            val converter = TimeTypeConverter()
            it("returns $expected") {
                assertEquals(expected, converter.convert(value))
            }
        }
    }
})
