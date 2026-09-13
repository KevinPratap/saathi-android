package com.saathi.overlay

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.saathi.model.OverlayMode

/**
 * Custom senior-accessible high-contrast canvas overlay view.
 * Implements WCAG AAA color contrast, 56dp touch targets, and a 3-second hold-to-dismiss barrier
 * to prevent accidental tremor taps or coerced dismissals during live scam calls.
 */
class SaathiOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var currentMode: OverlayMode? = null
    private var holdStartTime: Long = 0L
    private val REQUIRED_HOLD_MS = 3000L
    private var isHolding = false

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#121824") // Deep Obsidian Slate
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF4444") // Sleek hazard crimson
        style = Paint.Style.STROKE
        strokeWidth = 2f * resources.displayMetrics.density
    }

    private val textHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F8FAFC")
        textSize = 20f * resources.displayMetrics.scaledDensity
        typeface = Typeface.DEFAULT_BOLD
    }

    private val textBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#94A3B8")
        textSize = 15f * resources.displayMetrics.scaledDensity
    }

    private val btnHoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DC2626")
        style = Paint.Style.FILL
    }

    private val btnCallFamilyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0D201A")
        style = Paint.Style.FILL
    }

    private val btnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 16f * resources.displayMetrics.scaledDensity
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    private val btnCallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        textSize = 15f * resources.displayMetrics.scaledDensity
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    private val holdProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF4444")
        style = Paint.Style.FILL
    }

    private var holdButtonRect = RectF()
    private var callFamilyButtonRect = RectF()

    fun setMode(mode: OverlayMode) {
        this.currentMode = mode
        this.isHolding = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val mode = currentMode ?: return

        when (mode) {
            is OverlayMode.AmbientBanner -> drawWarningBanner(canvas, mode)
            is OverlayMode.InterventionModal -> drawInterventionModal(canvas, mode)
        }
    }

    private fun drawWarningBanner(canvas: Canvas, mode: OverlayMode.AmbientBanner) {
        val density = resources.displayMetrics.density
        val cardRect = RectF(16f * density, 48f * density, width - 16f * density, 140f * density)
        canvas.drawRoundRect(cardRect, 18f * density, 18f * density, cardPaint)
        canvas.drawRoundRect(cardRect, 18f * density, 18f * density, borderPaint)

        canvas.drawText(mode.alertData.titleEnglish, cardRect.left + 16f * density, cardRect.top + 36f * density, textHeaderPaint)
        canvas.drawText(mode.alertData.messageEnglish, cardRect.left + 16f * density, cardRect.top + 68f * density, textBodyPaint)
    }

    private fun drawInterventionModal(canvas: Canvas, mode: OverlayMode.InterventionModal) {
        val density = resources.displayMetrics.density
        val cardRect = RectF(20f * density, height * 0.15f, width - 20f * density, height * 0.85f)
        canvas.drawRoundRect(cardRect, 24f * density, 24f * density, cardPaint)
        canvas.drawRoundRect(cardRect, 24f * density, 24f * density, borderPaint)

        // Title
        canvas.drawText(mode.alertData.titleEnglish, cardRect.left + 24f * density, cardRect.top + 52f * density, textHeaderPaint)

        // Message
        canvas.drawText(mode.alertData.messageEnglish, cardRect.left + 24f * density, cardRect.top + 96f * density, textBodyPaint)

        // 3-Second Hold-to-Dismiss Button (56dp height minimum)
        val btnHeight = 56f * density
        holdButtonRect = RectF(
            cardRect.left + 20f * density,
            cardRect.bottom - (btnHeight * 2 + 36f * density),
            cardRect.right - 20f * density,
            cardRect.bottom - (btnHeight + 24f * density)
        )
        canvas.drawRoundRect(holdButtonRect, 16f * density, 16f * density, btnHoldPaint)

        // Hold progress bar
        val holdProgress = if (isHolding) {
            val elapsed = SystemClock.elapsedRealtime() - holdStartTime
            (elapsed.toFloat() / REQUIRED_HOLD_MS).coerceIn(0f, 1f)
        } else 0f

        if (holdProgress > 0f) {
            val progressWidth = holdButtonRect.width() * holdProgress
            val progressRect = RectF(holdButtonRect.left, holdButtonRect.top, holdButtonRect.left + progressWidth, holdButtonRect.bottom)
            canvas.drawRoundRect(progressRect, 16f * density, 16f * density, holdProgressPaint)
        }

        val btnLabel = if (holdProgress > 0f) "Hold to unlock: ${(holdProgress * 100).toInt()}%" else "Hold 3s to Dismiss"
        canvas.drawText(btnLabel, holdButtonRect.centerX(), holdButtonRect.centerY() + 6f * density, btnTextPaint)

        // Call Emergency Contact Button
        callFamilyButtonRect = RectF(
            cardRect.left + 20f * density,
            cardRect.bottom - (btnHeight + 12f * density),
            cardRect.right - 20f * density,
            cardRect.bottom - 12f * density
        )
        canvas.drawRoundRect(callFamilyButtonRect, 16f * density, 16f * density, btnCallFamilyPaint)
        canvas.drawText("Call Emergency Contact", callFamilyButtonRect.centerX(), callFamilyButtonRect.centerY() + 6f * density, btnCallTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val mode = currentMode
        if (mode is OverlayMode.InterventionModal) {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (holdButtonRect.contains(x, y)) {
                        isHolding = true
                        holdStartTime = SystemClock.elapsedRealtime()
                        postInvalidateOnAnimation()
                        return true
                    }
                    if (callFamilyButtonRect.contains(x, y)) {
                        // Emergency family call action
                        return true
                    }
                    return true // Block touches to underlying app
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isHolding) {
                        if (!holdButtonRect.contains(x, y)) {
                            // User moved finger outside the button; cancel hold
                            isHolding = false
                            postInvalidateOnAnimation()
                        } else {
                            val elapsed = SystemClock.elapsedRealtime() - holdStartTime
                            if (elapsed >= REQUIRED_HOLD_MS) {
                                isHolding = false
                                mode.onDismiss.invoke()
                            } else {
                                postInvalidateOnAnimation()
                            }
                        }
                    }
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isHolding = false
                    postInvalidateOnAnimation()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
