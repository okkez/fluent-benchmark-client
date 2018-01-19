package org.fluentd.benchmark.test

import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent

class TestAppender: AppenderSkeleton() {

    companion object {
        var events = mutableListOf<LoggingEvent>()
    }

    override fun requiresLayout(): Boolean {
        return false
    }

    override fun append(event: LoggingEvent?) {
        if (event != null) {
            events.add(event)
        }
    }

    override fun close() {
        // Do nothing
    }
}