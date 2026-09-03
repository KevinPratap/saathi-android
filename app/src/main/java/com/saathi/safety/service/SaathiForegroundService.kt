package com.saathi.safety.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.saathi.safety.R

/**
 * Foreground service to keep Saathi alive and show persistent notification.
 * OEMs are less likely to kill foreground services.
 */
class SaathiForegroundService : Service() {
    
    companion object {
        const val CHANNEL_ID = "saathi_protection"
        const val NOTIFICATION_ID = 1001
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Saathi Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Saathi is protecting you from scams"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Saathi सक्रिय है")
            .setContentText("Saathi आपकी protection कर रहा है")
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .build()
    }
}
