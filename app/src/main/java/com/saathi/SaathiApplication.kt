package com.saathi

import android.app.Application
import android.content.Intent
import android.os.Build
import com.saathi.data.AppDatabase
import com.saathi.engine.ScamDetectionEngine
import com.saathi.watchdog.WatchdogService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entrypoint for Saathi.
 * Initializes the Room database, on-device threat engine, and watchdog companion service.
 */
class SaathiApplication : Application() {

    val applicationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val scamDetectionEngine: ScamDetectionEngine by lazy {
        ScamDetectionEngine()
    }

    override fun onCreate() {
        super.onCreate()
        loadPersistedPatterns()
        startWatchdogDaemon()
    }

    private fun loadPersistedPatterns() {
        applicationScope.launch {
            try {
                val patterns = database.patternDao().getActivePatterns()
                if (patterns.isNotEmpty()) {
                    scamDetectionEngine.loadPatterns(patterns)
                }
            } catch (_: Exception) {
                // Engine operates with built-in default signatures if DB is empty
            }
        }
    }

    private fun startWatchdogDaemon() {
        try {
            val intent = Intent(this, WatchdogService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (_: Exception) {
            // Background start protection
        }
    }
}
