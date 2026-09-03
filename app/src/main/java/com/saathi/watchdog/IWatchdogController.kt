package com.saathi.watchdog

/**
 * Lifecycle and health monitoring interface for background watchdog.
 */
interface IWatchdogController {
    /**
     * Records a heartbeat timestamp from SaathiAccessibilityService.
     */
    fun sendHeartbeat(timestamp: Long = System.currentTimeMillis())

    /**
     * Checks if the monitored service has issued a heartbeat within the allowable threshold (90 seconds).
     */
    fun isServiceHealthy(): Boolean

    /**
     * Requests an auto-recovery restart of the main guardian process.
     */
    fun requestServiceRestart()
}
