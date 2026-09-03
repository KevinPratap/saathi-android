package com.saathi.watchdog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Reactive system event receiver (USER_PRESENT, POWER_CONNECTED).
 * Wakes up the watchdog whenever the user unlocks their device or connects a charger.
 */
class WatchdogPingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_USER_PRESENT ||
            action == Intent.ACTION_POWER_CONNECTED ||
            action == Intent.ACTION_POWER_DISCONNECTED
        ) {
            try {
                val serviceIntent = Intent(context, WatchdogService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (_: Exception) {
                // Background start handling
            }
        }
    }
}
