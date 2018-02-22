package org.fluentd.benchmark

import picocli.CommandLine
import java.io.IOException
import java.util.jar.Manifest

class ManifestVersionProvider: CommandLine.IVersionProvider {
    override fun getVersion(): Array<String> {
        val resources = javaClass.classLoader.getResources("META-INF/MANIFEST.MF")
        resources.iterator().forEach { url ->
            try {
                val manifest = Manifest(url.openStream())
                val title = manifest.mainAttributes.getValue("Implementation-Title")
                if (title == "fluent-benchmark-client") {
                    val version = manifest.mainAttributes.getValue("Manifest-Version")
                    return arrayOf(version)
                }
            } catch (ex: IOException) {
                return arrayOf("Unable to read from $url $ex")
            }
        }
        return arrayOf("Unable to read version from META-INF/MANIFEST.MF")
    }
}
