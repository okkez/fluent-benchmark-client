package org.fluentd.benchmark.test

import influent.forward.ForwardCallback
import influent.forward.ForwardServer
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeout
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
    private var isRunning = false

    fun run(expected: Long? = null, timeout: Long = 10L, body: () -> Unit) {
        try {
            start()
            body()
            if (expected != null) {
                waitFor(expected, timeout)
            }
        } finally {
            shutdown()
        }
    }

    fun start() {
        if (isRunning) {
            return
        }
        server.start()
        isRunning = true
        TimeUnit.MILLISECONDS.sleep(100)
    }

    fun shutdown() {
        if (isRunning) {
            isRunning = false
            server.shutdown()
        }
    }

    fun waitFor(expected: Long, timeout: Long = 10L) = runBlocking {
        withTimeout(timeout, TimeUnit.SECONDS) {
            while (isActive) {
                if (expected == counter.get()) {
                    return@withTimeout
                }
                delay(1, TimeUnit.MILLISECONDS)
            }
        }
    }

    fun processedEvents(): Long {
        return counter.get()
    }
}
