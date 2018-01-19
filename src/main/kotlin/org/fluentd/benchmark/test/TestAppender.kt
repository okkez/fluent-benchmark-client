package org.fluentd.benchmark.test

import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import java.io.Serializable

@Plugin(name = "TestAppender", category = "Core", elementType = "appender", printObject = true)
class TestAppender : AbstractAppender {
    constructor(name: String?,
                filter: Filter?,
                layout: Layout<out Serializable>?) : super(name, filter, layout)
    constructor(name: String?,
                filter: Filter?,
                layout: Layout<out Serializable>?,
                ignoreExceptions: Boolean) : super(name, filter, layout, ignoreExceptions)

    companion object {
        var events = mutableListOf<LogEvent>()

        @JvmStatic
        @PluginFactory
        fun createAppender(@PluginAttribute("name") name: String,
                           @PluginAttribute("ignoreExceptions") ignoreExceptions: Boolean,
                           @PluginElement("Layout") layout: Layout<out Serializable>?,
                           @PluginElement("Filters") filter: Filter?): TestAppender {
            return TestAppender(name, filter, layout)
        }
    }

    override fun append(event: LogEvent?) {
        if (event != null) {
            events.add(event)
        }
    }
}