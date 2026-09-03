package com.saathi.watchdog

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build

/**
 * 15-minute periodic JobScheduler health check (Tier 3 Resilience).
 * Persisted across device reboots to resurrect WatchdogService if terminated by aggressive OEM task managers.
 */
class WatchdogJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        try {
            val intent = Intent(this, WatchdogService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (_: Exception) {
            // Background start restriction handling
        }
        return false // Job work completed synchronously
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true // Reschedule if aborted
    }
}
