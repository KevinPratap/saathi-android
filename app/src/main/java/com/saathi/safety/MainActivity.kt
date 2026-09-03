package com.saathi.safety

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Main entry point for Saathi Safety MVP.
 * Checks if AccessibilityService is enabled and guides user to enable it.
 */
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (isAccessibilityEnabled()) {
            showMainScreen()
        } else {
            showAccessibilityPrompt()
        }
    }
    
    private fun isAccessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains(packageName)
    }
    
    private fun showAccessibilityPrompt() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Saathi को permission दें")
            .setMessage("Saathi को आपकी screen देखने की permission चाहिए ताकि वह आपकी protection कर सके।")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showMainScreen() {
        setContentView(R.layout.activity_main)
        // Simple main screen with protection status
        // TODO: Add protection status indicator
    }
}
