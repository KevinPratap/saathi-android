package com.saathi.watchdog

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock

/**
 * Isolated out-of-process Watchdog Daemon (:watchdog).
 * Maintains a persistent foreground heartbeat monitor to ensure Saathi survives aggressive OEM killers.
 */
class WatchdogService : Service(), IWatchdogController {

    private val binder = LocalBinder()
    private val CHANNEL_ID = "saathi_watchdog_channel"
    private val NOTIFICATION_ID = 1002

    @Volatile
    private var lastHeartbeatTimeMs: Long = SystemClock.elapsedRealtime()

    inner class LocalBinder : Binder() {
        fun getService(): WatchdogService = this@WatchdogService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Record heartbeat or trigger health check
        sendHeartbeat(SystemClock.elapsedRealtime())
        return START_STICKY
    }

    override fun sendHeartbeat(timestamp: Long) {
        this.lastHeartbeatTimeMs = timestamp
    }

    override fun isServiceHealthy(): Boolean {
        val delta = SystemClock.elapsedRealtime() - lastHeartbeatTimeMs
        return delta <= 90_000L // 90 seconds threshold
    }

    override fun requestServiceRestart() {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            launchIntent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                startActivity(it)
            }
        } catch (_: Exception) {
            // Intent recovery fallback
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Saathi Guardian Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors background service health and provides self-healing."
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Saathi Guardian Active")
            .setContentText("Continuous anti-fraud protection is running.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()
    }
}
