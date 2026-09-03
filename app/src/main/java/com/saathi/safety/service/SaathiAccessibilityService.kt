package com.saathi.safety.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.saathi.safety.overlay.OverlayManager

/**
 * Core AccessibilityService for Saathi Safety MVP.
 * Reads UI tree and detects scam patterns in real-time.
 */
class SaathiAccessibilityService : AccessibilityService() {
    
    private lateinit var overlayManager: OverlayManager
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(this)
        startForegroundService()
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        val rootNode = rootInActiveWindow ?: return
        val text = extractText(rootNode)
        
        // Check for scam patterns
        if (ScamDetector.isScam(text)) {
            overlayManager.showWarning()
        }
    }
    
    private fun extractText(node: android.view.accessibility.AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        node.text?.let { sb.append(it).append(" ") }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                sb.append(extractText(child))
            }
        }
        return sb.toString()
    }
    
    private fun startForegroundService() {
        val intent = Intent(this, SaathiForegroundService::class.java)
        startForegroundService(intent)
    }
    
    override fun onInterrupt() {}
    
    override fun onDestroy() {
        overlayManager.hideWarning()
        super.onDestroy()
    }
}
