package org.fluentd.benchmark

import java.nio.ByteBuffer

class BenchmarkConfig(val tag: String,
                      val timestampType: BenchmarkClient.TimestampType,
                      val nEvents: Int,
                      val interval: Int?,
                      val period: Int?,
                      val recordKey: String,
                      val recordValue: String,
                      val inputFileFormat: BenchmarkClient.FileFormat,
                      val inputFilePath: String?,
                      val mode: BenchmarkClient.Mode,
                      val reportInterval: Int) {

    private constructor(builder: Builder): this(
            builder.tag,
            builder.timestampType,
            builder.nEvents,
            builder.interval,
            builder.period,
            builder.recordKey,
            builder.recordValue,
            builder.inputFileFormat,
            builder.inputFilePath,
            builder.mode,
            builder.reportInterval
    )

    private var record: Map<String, Any>? = null

    companion object {
        fun create(init: Builder.() -> Unit) = Builder(init).build()
    }

    fun record(): Map<String, Any> {
        record = record ?: mapOf(recordKey to recordValue)
        return record!!
    }

    fun parser(): Parser<ByteBuffer> {
        return when (inputFileFormat) {
            BenchmarkClient.FileFormat.LTSV -> LTSVParser()
            BenchmarkClient.FileFormat.JSON -> JSONParser()
            BenchmarkClient.FileFormat.MessagePack -> MessagePackParser()
        }
    }

    class Builder private constructor() {
        constructor(init: Builder.() -> Unit): this() {
            init()
        }

        lateinit var tag: String
        var timestampType = BenchmarkClient.TimestampType.EventTime
        var nEvents: Int = 10000
        var interval: Int? = null
        var period: Int? = null
        lateinit var recordKey: String
        lateinit var recordValue: String
        var inputFileFormat = BenchmarkClient.FileFormat.LTSV
        var inputFilePath: String? = null
        lateinit var mode: BenchmarkClient.Mode
        var reportInterval: Int = 1

        fun tag(init: Builder.() -> String) = apply { tag = init() }

        fun timestampType(init: Builder.() -> BenchmarkClient.TimestampType) = apply { timestampType = init() }

        fun nEvents(init: Builder.() -> Int) = apply { nEvents = init() }

        fun interval(init: Builder.() -> Int?) = apply { interval = init() }

        fun period(init: Builder.() -> Int?) = apply { period = init() }

        fun recordKey(init: Builder.() -> String) = apply { recordKey = init() }

        fun recordValue(init: Builder.() -> String) = apply { recordValue = init() }

        fun inputFileFormat(init: Builder.() -> BenchmarkClient.FileFormat) = apply { inputFileFormat = init() }

        fun inputFilePath(init: Builder.() -> String) = apply { inputFilePath = init() }

        fun reportInterval(init: Builder.() -> Int) = apply { reportInterval = init() }

        fun mode(init: Builder.() -> BenchmarkClient.Mode) = apply { mode = init() }

        fun build() = BenchmarkConfig(this)
    }
}