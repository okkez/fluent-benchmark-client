package org.fluentd.benchmark

import kotlinx.coroutines.experimental.runBlocking
import org.komamitsu.fluency.Fluency
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = "fluent-benchmark-client", version = ["1.0.0"],
        sortOptions = false, abbreviateSynopsis = true,
        optionListHeading = "%nOptions:%n",
        description = ["Benchmark client for Fluentd"])
class FluentBenchmarkClient: Runnable {

    @Option(names = ["-H", "--host"], paramLabel = "HOST",
            description = ["The IP address or host name of the server (localhost)"])
    private var host: String = "localhost"

    @Option(names = ["-P", "--port"], paramLabel = "PORT",
            description = ["The port number of the host (24224)"])
    private var port: Int = 24224

    // TODO Support authentication related options when Fluency supports them
    @Option(names = ["--shared-key"], paramLabel = "SHAREDKEY", hidden = true, description = ["The shared key"])
    private var sharedKey: String? = null

    @Option(names = ["--username"], paramLabel = "USERNAME", hidden = true, description = ["The username for authentication"])
    private var username: String? = null

    @Option(names = ["--password"], paramLabel = "PASSWORD", hidden = true, description = ["The password for authentication"])
    private var password: String? = null

    @Option(names = ["--require-ack-response"], description = ["Change the protocol to at-least-once (false)"])
    private var requireAckResponse: Boolean = false

    // Buffer options
    @Option(names = ["--buffer-chunk-initial-size"], paramLabel = "SIZE",
            converter = [SizeTypeConverter::class],
            description = ["Initial chunk buffer size (1MB)"])
    private var bufferChunkInitialSize: Int? = null

    @Option(names = ["--buffer-chunk-retention-size"], paramLabel = "SIZE",
            converter = [SizeTypeConverter::class],
            description = ["Threshold chunk buffer size to flush (4MB)"])
    private var bufferChunkRetentionSize: Int? = null

    @Option(names = ["--max-buffer-size"], paramLabel = "SIZE",
            converter = [SizeTypeConverter::class],
            description = ["The max buffer size (16MB)"])
    private var maxBufferSize: Long? = null

    @Option(names = ["--wait-until-buffer-flushed"], paramLabel = "SECONDS",
            description = ["Max wait SECONDS until all buffers are flushed"])
    private var waitUntilBufferFlushed: Int? = null

    @Option(names = ["--wait-until-flusher-terminated"], paramLabel = "SECONDS",
            description = ["Max wait SECONDS until the flusher is terminated"])
    private var waitUntilFlusherTerminated: Int? = null

    @Option(names = ["--flush-interval"], paramLabel = "MILLISECONDS",
            description = ["Flush interval in milli seconds"])
    private var flushInterval: Int? = null

    // Load options
    @Option(names = ["--n-events"], paramLabel = "N", description = ["Emit N events (1000)"])
    private var nEvents: Int = 1000

    @Option(names = ["--interval"], paramLabel = "INTERVAL",
            converter = [TimeTypeConverter::class],
            description = [
                "Emit events at intervals of INTERVAL seconds/minutes/hours",
                "e.g: 3s, 3m, 1h. Default is seconds.",
                "conflict with --period"
            ])
    private var fixedInterval: Int? = null

    @Option(names = ["--period"], paramLabel = "PERIOD",
            converter = [TimeTypeConverter::class],
            description = [
                "Emit events on average in PERIOD seconds/minutes/hours",
                "e.g: 3s, 3m, 1h. Default is seconds.",
                "conflict with --interval"
            ])
    private var fixedPeriod: Int? = null

    @Option(names = ["--flood"], paramLabel = "PERIOD",
            converter = [TimeTypeConverter::class],
            description = [
                "Flood of events are emitted for PEDIOD seconds/minutes/hours",
                "e.g: 3s, 3m, 1h. Default is seconds.",
                "conflict with --interval, --period"
            ])
    private var flood: Int? = null

    @Option(names = ["--tag"], paramLabel = "TAG", description = ["Tag for each event"])
    private var tag: String = "benchmark.data"

    enum class TimestampType {
        EventTime,
        Integer
    }

    @Option(names = ["--timestamp-type"], paramLabel = "TYPE",
            description = ["Timestamp type for each event: EventTime, Integer"])
    private var timestampType: TimestampType = TimestampType.EventTime

    // Report options
    @Option(names = ["--report-periodically"], paramLabel = "INTERVAL",
            converter = [TimeTypeConverter::class],
            description = [
                "Report statistics at intervals of INTERVAL seconds/minutes/hours",
                "If INTERVAL isn't specified, report each 1 second"
            ])
    private var reportInterval: Int = 1

    @Option(names = ["-h", "--help"], usageHelp = true, description = ["Print this help and exit"])
    private var helpRequested: Boolean = false

    @Option(names = ["-V", "--version"], versionHelp = true, description = ["Display version info and exit"])
    private var versionInfoRequested: Boolean = false

    override fun run() = runBlocking {
        val conf = buildFluencyConfig()

        val mode = when {
            fixedInterval != null -> BenchmarkClient.Mode.FIXED_INTERVAL
            fixedPeriod != null -> BenchmarkClient.Mode.FIXED_PERIOD
            flood != null -> BenchmarkClient.Mode.FLOOD
            else -> BenchmarkClient.Mode.FLOOD
        }

        log.info("Run benchmark!")
        val client = BenchmarkClient(
                host = host,
                port = port,
                fluencyConfig = conf,
                tag = tag,
                timestampType = timestampType,
                nEvents = nEvents,
                interval = fixedInterval,
                period = fixedPeriod,
                mode = mode,
                reportInterval = reportInterval
        )
        try {
            client.run()
        } finally {
            client.stop()
            client.report()
        }
    }

    private fun buildFluencyConfig(): Fluency.Config {
        val conf: Fluency.Config = Fluency.Config()
        conf.isAckResponseMode = requireAckResponse

        if (bufferChunkInitialSize != null) {
            conf.bufferChunkInitialSize = bufferChunkInitialSize
        }
        if (bufferChunkRetentionSize != null) {
            conf.bufferChunkRetentionSize = bufferChunkRetentionSize
        }
        if (maxBufferSize != null) {
            conf.maxBufferSize = maxBufferSize
        }
        if (waitUntilBufferFlushed != null && waitUntilBufferFlushed!! > 0) {
            conf.waitUntilBufferFlushed = waitUntilBufferFlushed
        }
        if (waitUntilFlusherTerminated != null && waitUntilFlusherTerminated!! > 0) {
            conf.waitUntilFlusherTerminated = waitUntilFlusherTerminated
        }
        if (flushInterval != null && flushInterval!! > 0) {
            conf.flushIntervalMillis = flushInterval
        }

        return conf
    }

    companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)!!
        @JvmStatic fun main(args: Array<String>) {
            CommandLine.run(FluentBenchmarkClient(), System.err, *args)
        }
    }

}

