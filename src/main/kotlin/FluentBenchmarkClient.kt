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

    @Option(names = ["-H", "--host"], paramLabel = "HOST", description = ["The IP address or host name of the server"])
    private var host: String = "localhost"

    @Option(names = ["-P", "--port"], paramLabel = "PORT", description = ["The port number of the host"])
    private var port: Int = 24224

    override fun run() {
        var conf: Fluency.Config = Fluency.Config()
        var fluency: Fluency = Fluency.defaultFluency(host, port, conf)
        println("Run!")
        var client: BenchmarkClient = BenchmarkClient(fluency)
        client.run()
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            CommandLine.run(FluentBenchmarkClient(), System.err, *args)
        }
    }

}

