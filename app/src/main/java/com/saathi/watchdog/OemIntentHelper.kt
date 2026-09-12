package com.saathi.watchdog

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Helper utility to navigate users to OEM-specific battery optimization and autostart settings.
 */
object OemIntentHelper {

    fun getBatteryOptimizationIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    fun getAutoStartIntent(): Intent? {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val intent = Intent()

        return when {
            // Xiaomi / Redmi / POCO (MIUI / HyperOS)
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
                intent.component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
                intent
            }
            // Samsung (One UI)
            manufacturer.contains("samsung") -> {
                intent.component = ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity"
                )
                intent
            }
            // Oppo / Realme (ColorOS / Realme UI)
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
                intent.component = ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
                intent
            }
            // Vivo / iQOO (Funtouch OS / OriginOS)
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> {
                intent.component = ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
                intent
            }
            // OnePlus (OxygenOS)
            manufacturer.contains("oneplus") -> {
                intent.component = ComponentName(
                    "com.oneplus.security",
                    "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                )
                intent
            }
            // Huawei / Honor (EMUI / Magic UI)
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                intent.component = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
                intent
            }
            else -> null
        }
    }
}
