package org.fluentd.benchmark

import org.msgpack.core.MessagePack
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer

class BenchmarkConfig(val tag: String,
                      val timestampType: BenchmarkClient.TimestampType,
                      val nEvents: Long,
                      val nEventsPerSec: Long?,
                      val interval: Long?,
                      val period: Long?,
                      val recordKey: String,
                      val recordValue: String,
                      val inputFileFormat: BenchmarkClient.FileFormat,
                      val inputFilePath: String?,
                      val mode: BenchmarkClient.Mode,
                      val reportInterval: Long) {

    private constructor(builder: Builder): this(
            builder.tag,
            builder.timestampType,
            builder.nEvents,
            builder.nEventsPerSec,
            builder.interval,
            builder.period,
            builder.recordKey,
            builder.recordValue,
            builder.inputFileFormat,
            builder.inputFilePath,
            builder.mode,
            builder.reportInterval
    )

    companion object {
        fun create(init: Builder.() -> Unit) = Builder(init).build()
    }

    fun record(): ByteArray {
        val buffer = ByteArrayOutputStream()
        val packer = MessagePack.newDefaultPacker(buffer)
        packer.packMapHeader(1)
        packer.packString(recordKey)
        packer.packString(recordValue)
        packer.flush()
        packer.close()
        return buffer.toByteArray()
    }

    fun parser(): Parser<ByteArray> {
        return when (inputFileFormat) {
            BenchmarkClient.FileFormat.LTSV -> LTSVParser()
            BenchmarkClient.FileFormat.JSON -> JSONParser()
        }
    }

    class Builder private constructor() {
        constructor(init: Builder.() -> Unit): this() {
            init()
        }

        lateinit var tag: String
        var timestampType = BenchmarkClient.TimestampType.EventTime
        var nEvents: Long = 10000L
        var nEventsPerSec: Long? = null
        var interval: Long? = null
        var period: Long? = null
        lateinit var recordKey: String
        lateinit var recordValue: String
        var inputFileFormat = BenchmarkClient.FileFormat.LTSV
        var inputFilePath: String? = null
        lateinit var mode: BenchmarkClient.Mode
        var reportInterval: Long = 1

        fun tag(init: Builder.() -> String) = apply { tag = init() }

        fun timestampType(init: Builder.() -> BenchmarkClient.TimestampType) = apply { timestampType = init() }

        fun nEvents(init: Builder.() -> Long) = apply { nEvents = init() }

        fun nEventsPerSec(init: Builder.() -> Long?) = apply { nEventsPerSec = init() }

        fun interval(init: Builder.() -> Long?) = apply { interval = init() }

        fun period(init: Builder.() -> Long?) = apply { period = init() }

        fun recordKey(init: Builder.() -> String) = apply { recordKey = init() }

        fun recordValue(init: Builder.() -> String) = apply { recordValue = init() }

        fun inputFileFormat(init: Builder.() -> BenchmarkClient.FileFormat) = apply { inputFileFormat = init() }

        fun inputFilePath(init: Builder.() -> String) = apply { inputFilePath = init() }

        fun reportInterval(init: Builder.() -> Long) = apply { reportInterval = init() }

        fun mode(init: Builder.() -> BenchmarkClient.Mode) = apply { mode = init() }

        fun build() = BenchmarkConfig(this)
    }
}
