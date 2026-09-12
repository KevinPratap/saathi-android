package com.saathi.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import com.saathi.model.OverlayMode
import com.saathi.model.ScamAlertData

/**
 * Manages system overlay window lifecycle via WindowManager.
 * Enforces dual presentation modes: touch-passthrough ambient banners vs. touch-intercepting modals.
 */
class OverlayManager(private val context: Context) : IOverlayManager {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private var overlayView: SaathiOverlayView? = null
    private var isAttached = false

    override fun showWarningBanner(alertData: ScamAlertData) {
        if (!canDrawOverlays()) return
        val params = createBannerLayoutParams()
        attachOrUpdate(params, OverlayMode.AmbientBanner(alertData))
    }

    override fun showInterventionModal(alertData: ScamAlertData, onDismiss: () -> Unit) {
        if (!canDrawOverlays()) return
        val params = createModalLayoutParams()
        attachOrUpdate(params, OverlayMode.InterventionModal(alertData) {
            dismissOverlay()
            onDismiss.invoke()
        })
    }

    override fun dismissOverlay() {
        overlayView?.let { view ->
            if (isAttached && windowManager != null) {
                try {
                    windowManager.removeView(view)
                } catch (_: Exception) {
                    // View might already be detached
                }
                isAttached = false
            }
        }
        overlayView = null
    }

    override fun isOverlayVisible(): Boolean = isAttached

    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    private fun createBannerLayoutParams(): WindowManager.LayoutParams {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private fun createModalLayoutParams(): WindowManager.LayoutParams {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        ).apply {
            dimAmount = 0.82f // 82% ambient darkening for maximum contrast
            gravity = Gravity.CENTER
        }
    }

    private fun attachOrUpdate(params: WindowManager.LayoutParams, mode: OverlayMode) {
        if (windowManager == null) return

        if (overlayView == null) {
            overlayView = SaathiOverlayView(context)
        }
        overlayView?.setMode(mode)

        try {
            if (!isAttached) {
                windowManager.addView(overlayView, params)
                isAttached = true
            } else {
                windowManager.updateViewLayout(overlayView, params)
            }
        } catch (_: Exception) {
            isAttached = false
        }
    }
}
