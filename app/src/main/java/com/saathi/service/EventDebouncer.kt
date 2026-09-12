package com.saathi.service

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Coroutine-based sliding event debouncer.
 * Collapses high-frequency accessibility event bursts (e.g. 60Hz scrolling) into a stabilized evaluation window.
 */
class EventDebouncer(
    private val debounceDelayMs: Long = 300L,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {

    private val jobs = ConcurrentHashMap<String, Job>()

    /**
     * Debounces an action associated with a key. Any pending action for that key is canceled.
     */
    fun debounce(key: String = "DEFAULT", action: suspend () -> Unit) {
        jobs[key]?.cancel()
        val job = scope.launch {
            delay(debounceDelayMs)
            action.invoke()
        }
        jobs[key] = job
    }

    /**
     * Cancels any pending debounced job for a key.
     */
    fun cancel(key: String = "DEFAULT") {
        jobs.remove(key)?.cancel()
    }

    /**
     * Cancels all pending debounced jobs.
     */
    fun cancelAll() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
    }

    /**
     * Returns true if there is an active job waiting to execute for the given key.
     */
    fun isPending(key: String = "DEFAULT"): Boolean {
        return jobs[key]?.isActive == true
    }
}
