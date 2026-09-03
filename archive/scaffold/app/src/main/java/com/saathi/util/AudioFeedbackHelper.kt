package com.saathi.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * Senior-tailored audio and haptic feedback helper.
 * Designed to avoid shrill frequencies that trigger panic or cannot be heard due to presbycusis.
 */
class AudioFeedbackHelper(private val context: Context) {

    private val vibrator: Vibrator? = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    /**
     * Calming, gentle single-tick haptic pulse for guidance beacons (40ms).
     */
    fun triggerGuidanceTick() {
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(40L, 80)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(40L)
        }
    }

    /**
     * Rhythmic urgency pulse for high-risk scam interdiction (150ms ON, 100ms OFF, 250ms ON).
     * Non-alarming rhythmic vibration pattern that breaks trance without inducing shock.
     */
    fun triggerUrgencyPulse() {
        if (vibrator == null || !vibrator.hasVibrator()) return

        val timings = longArrayOf(0L, 150L, 100L, 250L)
        val amplitudes = intArrayOf(0, 180, 0, 220)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, -1)
        }
    }

    /**
     * Plays a pleasant chime tone at mid frequencies (400-800Hz) audible for seniors.
     */
    fun playNoticeTone() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        } catch (_: Exception) {
            // Audio output fallback
        }
    }
}
