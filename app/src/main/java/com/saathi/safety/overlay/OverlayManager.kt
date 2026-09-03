package com.saathi.safety.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.saathi.safety.R

/**
 * Manages the overlay warning shown when a scam is detected.
 * Uses WindowManager to draw over any app.
 */
class OverlayManager(private val context: Context) {
    
    private var overlayView: View? = null
    private val windowManager: WindowManager = 
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    
    fun showWarning() {
        if (overlayView != null) return
        
        val view = TextView(context).apply {
            text = "⚠️ Saathi Warning!\n\nThis looks like a scam.\nDo not share OTP or send money.\n\nTap to dismiss"
            textSize = 20f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFD32F2F.toInt())
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setOnClickListener { hideWarning() }
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        
        try {
            windowManager.addView(view, params)
            overlayView = view
        } catch (e: Exception) {
            // Permission not granted or overlay failed
        }
    }
    
    fun hideWarning() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // Already removed
            }
            overlayView = null
        }
    }
}
