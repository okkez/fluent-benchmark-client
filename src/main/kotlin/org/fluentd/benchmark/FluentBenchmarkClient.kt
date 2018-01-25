package org.fluentd.benchmark

import kotlinx.coroutines.experimental.runBlocking
import org.komamitsu.fluency.Fluency
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.lang.management.ManagementFactory

@Command(name = "fluent-benchmark-client", version = ["1.0.0"],
        sortOptions = false, abbreviateSynopsis = true,
        optionListHeading = "%nOptions:%n",
        description = ["Benchmark client for Fluentd"])
class FluentBenchmarkClient: Runnable {

    @Option(names = ["-H", "--host"], paramLabel = "HOST",
            description = ["The IP address or host name of the server (localhost)"])
    private var _host: String = "localhost"

    @Option(names = ["-P", "--port"], paramLabel = "PORT",
            description = ["The port number of the host (24224)"])
    private var _port: Int = 24224

    // TODO Support authentication related options when Fluency supports them
    @Option(names = ["--shared-key"], paramLabel = "SHAREDKEY", hidden = true, description = ["The shared key"])
    private var _sharedKey: String? = null

    @Option(names = ["--username"], paramLabel = "USERNAME", hidden = true, description = ["The username for authentication"])
    private var _username: String? = null

    @Option(names = ["--password"], paramLabel = "PASSWORD", hidden = true, description = ["The password for authentication"])
    private var _password: String? = null

    @Option(names = ["--require-ack-response"], description = ["Change the protocol to at-least-once (false)"])
    private var _requireAckResponse: Boolean = false

    // Buffer options
    @Option(names = ["--buffer-chunk-initial-size"], paramLabel = "SIZE",
            converter = [SizeTypeConverter::class],
            description = ["Initial chunk buffer size (1MB)"])
    private var _bufferChunkInitialSize: Int? = null

    @Option(names = ["--buffer-chunk-retention-size"], paramLabel = "SIZE",
            converter = [SizeTypeConverter::class],
            description = ["Threshold chunk buffer size to flush (4MB)"])
    private var _bufferChunkRetentionSize: Int? = null

    @Option(names = ["--max-buffer-size"], paramLabel = "SIZE",
            converter = [SizeTypeConverter::class],
            description = ["The max buffer size (16MB)"])
    private var _maxBufferSize: Long? = null

    @Option(names = ["--wait-until-buffer-flushed"], paramLabel = "SECONDS",
            description = ["Max wait SECONDS until all buffers are flushed"])
    private var _waitUntilBufferFlushed: Int? = null

    @Option(names = ["--wait-until-flusher-terminated"], paramLabel = "SECONDS",
            description = ["Max wait SECONDS until the flusher is terminated"])
    private var _waitUntilFlusherTerminated: Int? = null

    @Option(names = ["--flush-interval"], paramLabel = "MILLISECONDS",
            description = ["Flush interval in milli seconds"])
    private var _flushInterval: Int? = null

    // Load options
    @Option(names = ["--n-events"], paramLabel = "N", description = ["Emit N events (1000)"])
    private var _nEvents: Int = 1000

    @Option(names = ["--interval"], paramLabel = "INTERVAL",
            converter = [TimeTypeConverter::class],
            description = [
                "Emit events at intervals of INTERVAL seconds/minutes/hours",
                "e.g: 3s, 3m, 1h. Default is seconds.",
                "conflict with --period"
            ])
    private var _fixedInterval: Int? = null

    @Option(names = ["--period"], paramLabel = "PERIOD",
            converter = [TimeTypeConverter::class],
            description = [
                "Emit events on average in PERIOD seconds/minutes/hours",
                "e.g: 3s, 3m, 1h. Default is seconds.",
                "conflict with --interval"
            ])
    private var _fixedPeriod: Long? = null

    @Option(names = ["--flood"], paramLabel = "PERIOD",
            converter = [TimeTypeConverter::class],
            description = [
                "Flood of events are emitted for PEDIOD seconds/minutes/hours",
                "e.g: 3s, 3m, 1h. Default is seconds.",
                "conflict with --interval, --period"
            ])
    private var _flood: Long? = null

    @Option(names = ["--record-key"], paramLabel = "KEY",
            description = ["The KEY of record"])
    private var _recordKey: String = "message"

    @Option(names = ["--record-value"], paramLabel = "MESSAGE",
            description = ["The MESSAGE of record"])
    private var _recordValue: String = "Hello, Fluentd! This is a test message."

    @Option(names = ["--input-file-format"], paramLabel = "FORMAT",
            description = [
                "Format of input file. ltsv/json",
                "This option must use with --input-file"
            ])
    private var _inputFileFormat: BenchmarkClient.FileFormat = BenchmarkClient.FileFormat.LTSV

    @Option(names = ["--input-file"], paramLabel = "PATH",
            description = ["Input file path"])
    private var _inputFilePath: String? = null

    @Option(names = ["--tag"], paramLabel = "TAG", description = ["Tag for each event"])
    private var _tag: String = "benchmark.data"

    @Option(names = ["--timestamp-type"], paramLabel = "TYPE",
            description = ["Timestamp type for each event: EventTime, Integer"])
    private var _timestampType: BenchmarkClient.TimestampType = BenchmarkClient.TimestampType.EventTime

    // Report options
    @Option(names = ["--report-periodically"], paramLabel = "INTERVAL",
            converter = [TimeTypeConverter::class],
            description = [
                "Report statistics at intervals of INTERVAL seconds/minutes/hours",
                "If INTERVAL isn't specified, report each 1 second"
            ])
    private var _reportInterval: Long = 1

    @Option(names = ["-N", "--dry-run"], description = ["Dry run"])
    private var _dryRunRequested: Boolean = false

    @Option(names = ["-h", "--help"], usageHelp = true, description = ["Print this help and exit"])
    private var _helpRequested: Boolean = false

    @Option(names = ["-V", "--version"], versionHelp = true, description = ["Display version info and exit"])
    private var _versionInfoRequested: Boolean = false

    override fun run() = runBlocking {
        val benchmarkMode = when {
            _fixedInterval != null -> BenchmarkClient.Mode.FIXED_INTERVAL
            _fixedPeriod != null -> BenchmarkClient.Mode.FIXED_PERIOD
            _flood != null -> BenchmarkClient.Mode.FLOOD
            else -> BenchmarkClient.Mode.FLOOD
        }

        val benchmarkConfig = BenchmarkConfig.create {
            tag = _tag
            timestampType = _timestampType
            nEvents = _nEvents
            interval = _fixedInterval
            period {
                when (benchmarkMode) {
                    BenchmarkClient.Mode.FIXED_PERIOD -> _fixedPeriod
                    BenchmarkClient.Mode.FLOOD -> _flood
                    else -> null
                }
            }
            recordKey = _recordKey
            recordValue = _recordValue
            inputFileFormat = _inputFileFormat
            inputFilePath = _inputFilePath
            mode = benchmarkMode
            reportInterval = _reportInterval
        }
        var pid = ManagementFactory.getRuntimeMXBean().name.split(Regex("""@"""), 2)[0]
        log.info("Run benchmark! mode=$benchmarkMode PID=$pid")
        val client = BenchmarkClient.create {
            host = _host
            port = _port
            fluencyConfig = buildFluencyConfig()
            benchmarkConfig { benchmarkConfig }
        }
        if (_dryRunRequested) {
            client.stop()
            return@runBlocking
        }
        try {
            client.run()
        } finally {
            client.stop()
            client.report()
        }
    }

    private fun buildFluencyConfig(): Fluency.Config {
        val conf: Fluency.Config = Fluency.Config()
        conf.isAckResponseMode = _requireAckResponse

        if (_bufferChunkInitialSize != null) {
            conf.bufferChunkInitialSize = _bufferChunkInitialSize
        }
        if (_bufferChunkRetentionSize != null) {
            conf.bufferChunkRetentionSize = _bufferChunkRetentionSize
        }
        if (_maxBufferSize != null) {
            conf.maxBufferSize = _maxBufferSize
        }
        if (_waitUntilBufferFlushed != null && _waitUntilBufferFlushed!! > 0) {
            conf.waitUntilBufferFlushed = _waitUntilBufferFlushed
        }
        if (_waitUntilFlusherTerminated != null && _waitUntilFlusherTerminated!! > 0) {
            conf.waitUntilFlusherTerminated = _waitUntilFlusherTerminated
        }
        if (_flushInterval != null && _flushInterval!! > 0) {
            conf.flushIntervalMillis = _flushInterval
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

