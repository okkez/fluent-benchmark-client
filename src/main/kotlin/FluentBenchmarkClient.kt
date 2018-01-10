package org.fluentd

import org.komamitsu.fluency.Fluency
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option

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

    @Option(names = ["--require-ack-response"], description = ["Change the protocol to at-least-once (default: false)"])
    private var requireAckResponse: Boolean = false

    @Option(names = ["--ack-response-timeout"], paramLabel = "TIMEOUT",
            description = ["This option is used when require_ack_response is true"])
    private var ackResponseTimeout: Int? = null

    override fun run() {
        var conf: Fluency.Config = Fluency.Config()
        var fluency: Fluency = Fluency.defaultFluency(host, port, conf)
        println("Run!")
        var client: BenchmarkClient = BenchmarkClient(fluency)
        client.run()
        fluency.close()
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            CommandLine.run(FluentBenchmarkClient(), System.err, *args)
        }
    }

}

