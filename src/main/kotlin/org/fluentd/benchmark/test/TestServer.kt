package org.fluentd.benchmark.test

import influent.forward.ForwardCallback
import influent.forward.ForwardServer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class TestServer {

    private val callback = ForwardCallback.of { stream ->
        counter.addAndGet(stream.entries.size.toLong())
        return@of CompletableFuture.completedFuture(null)
    }
    private val builder = ForwardServer.Builder(callback)
    private val server = builder.build()
    private val counter = AtomicLong(0)

    fun start() {
        server.start()
        TimeUnit.MILLISECONDS.sleep(100)
    }

    fun shutdown() {
        server.shutdown()
    }

    fun processedEvents(): Long {
        return counter.get()
    }
}
