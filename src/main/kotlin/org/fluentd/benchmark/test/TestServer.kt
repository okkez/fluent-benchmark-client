package org.fluentd.benchmark.test

import influent.forward.ForwardCallback
import influent.forward.ForwardServer
import kotlinx.coroutines.*
import java.net.ServerSocket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.coroutineContext

class TestServer(port: Int = 24224) {

    companion object {
        fun unusedPort(): Int {
            return ServerSocket(0).use {
                return@use  it.localPort
            }
        }
    }

    private val callback = ForwardCallback.of { stream ->
        counter.addAndGet(stream.entries.size.toLong())
        return@of CompletableFuture.completedFuture(null)
    }
    private val builder = ForwardServer.Builder(callback)
    private val server = builder.localAddress(port).build()
    private val counter = AtomicLong(0)
    private var isRunning = false

    fun run(expected: Long? = null, timeout: Long = 10L, body: () -> Unit) = runBlocking {
        var serverJob: Job? = null
        try {
            start()
            serverJob = GlobalScope.launch { body() }
            if (expected != null) {
                waitFor(expected, timeout)
            }
        } finally {
            serverJob!!.cancel()
            serverJob.join()
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
        withTimeout(TimeUnit.SECONDS.toMillis(timeout)) {
            while (isActive) {
                if (expected == counter.get()) {
                    return@withTimeout
                }
                delay(1)
            }
        }
    }

    fun processedEvents(): Long {
        return counter.get()
    }
}
