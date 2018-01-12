package org.fluentd.benchmark

import org.komamitsu.fluency.Fluency
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import kotlin.system.exitProcess

@Command(name = "FluentdBenchmarkClient", version = ["1.0.0"], description = ["Benchmark client for Fluentd"])
class FluentBenchmarkClient: Runnable {

    @Option(names = ["-h", "--help"], usageHelp = true, description = ["Print this help and exit"])
    private var helpRequested: Boolean = false

    @Option(names = arrayOf("-V", "--version"), versionHelp = true, description = ["display version info"])
    private var versionInfoRequested: Boolean = false

    @Option(names = ["-H", "--host"], paramLabel = "HOST",
            description = ["The IP address or host name of the server", "(default: localhost)"])
    private var host: String = "localhost"

    @Option(names = ["-P", "--port"], paramLabel = "PORT",
            description = ["The port number of the host", "(default: 24224)"])
    private var port: Int = 24224

    // TODO Use authentication related options
    @Option(names = ["--shared-key"], paramLabel = "SHAREDKEY", description = ["The shared key"])
    private var sharedKey: String? = null

    @Option(names = ["--username"], paramLabel = "USERNAME", description = ["The username for authentication"])
    private var username: String? = null

    @Option(names = ["--password"], paramLabel = "PASSWORD", description = ["The password for authentication"])
    private var password: String? = null

    @Option(names = ["--require-ack-response"],
            description = [
                "Change the protocol to at-least-once",
                "(default: false)"
            ])
    private var requireAckResponse: Boolean = false

    // Buffer options
    @Option(names = ["--buffer-chunk-initial-size"], paramLabel = "SIZE", description = [""])
    private var bufferChunkInitialSize: String? = null

    @Option(names = ["--buffer-chunk-retention-size"], paramLabel = "SIZE", description = [""])
    private var bufferChunkRetentionSize: String? = null

    @Option(names = ["--max-buffer-size"], paramLabel = "SIZE", description = ["The max buffer size"])
    private var maxBufferSize: String? = null

    // Load options
    @Option(names = ["--n-events"], paramLabel = "N", description = ["Emit N events"])
    private var nEvents: Int = 10000

    @Option(names = ["--flood"], paramLabel = "PERIOD",
            description = [
                "Flood of events are emitted for PEDIOD seconds/minutes/hours",
                "Endless if PERIOD is not specified"
            ])
    private var flood: String? = null

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
            description = [
                "Report statistics at intervals of INTERVAL seconds/minutes/hours",
                "If INTERVAL isn't specified, report each 1 second"
            ])
    private var reportPeriodically: String? = null

    override fun run() {
        val conf: Fluency.Config = Fluency.Config()
        conf.isAckResponseMode = requireAckResponse

        if (!bufferChunkInitialSize.isNullOrEmpty()) {
            conf.bufferChunkInitialSize = sizeToInt(bufferChunkInitialSize!!)
        }
        if (!bufferChunkRetentionSize.isNullOrEmpty()) {
            conf.bufferChunkRetentionSize = sizeToInt(bufferChunkRetentionSize!!)
        }
        if (!maxBufferSize.isNullOrEmpty()) {
            conf.maxBufferSize = sizeToLong(maxBufferSize!!)
        }


        val floodPeriod = if (!flood.isNullOrEmpty()) {
            timeToInt(flood!!)
        } else {
            0
        }

        println(floodPeriod)

        log.info("Run benchmark!")
        val client = BenchmarkClient(
                host = host,
                port = port,
                fluencyConfig = conf,
                tag = tag,
                timestampType = timestampType,
                floodPeriod = floodPeriod)
        try {
            client.run()
        } finally {
            println("finaly !!!!!")
            client.stop()
            println("stopped !!!!!")
            // exitProcess(0)
            //client.statistics.finish()
            //val reporter = StatisticsReporter(client.statistics)
            //reporter.report()
        }
    }

    companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)!!
        @JvmStatic fun main(args: Array<String>) {
            CommandLine.run(FluentBenchmarkClient(), System.err, *args)
        }
    }

}

