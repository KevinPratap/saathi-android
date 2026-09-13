package com.saathi.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.saathi.R
import com.saathi.model.RiskLevel
import com.saathi.model.ScamAlertData
import com.saathi.model.ScamCategory
import com.saathi.overlay.OverlayManager
import com.saathi.watchdog.WatchdogService
import java.util.Locale

/**
 * Executive user-facing Activity for Saathi.
 * Pure minimalist dark-mode design with zero clutter, 
 * clean typography, high-contrast security indicators, 
 * and natural English voice guidance.
 */
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isTtsReady = false

    private lateinit var cardProtectionStatus: MaterialCardView
    private lateinit var imgStatusIcon: ImageView
    private lateinit var txtStatusTitle: TextView
    private lateinit var txtStatusSub: TextView
    private lateinit var btnEnableSettings: MaterialButton
    private lateinit var btnVoiceAssistant: ImageButton
    private lateinit var viewVoiceRipple: View
    private lateinit var cardWhatsAppHelp: View
    private lateinit var cardBillPayHelp: View
    private lateinit var cardTestScamAlert: View
    private lateinit var btnCallFamily: View

    private lateinit var overlayManager: OverlayManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        overlayManager = OverlayManager(this)
        tts = TextToSpeech(this, this)

        initViews()
        setupListeners()
        ensureWatchdogStarted()
    }

    override fun onResume() {
        super.onResume()
        refreshProtectionState()
    }

    override fun onDestroy() {
        if (isTtsReady) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private fun initViews() {
        cardProtectionStatus = findViewById(R.id.cardProtectionStatus)
        imgStatusIcon = findViewById(R.id.imgStatusIcon)
        txtStatusTitle = findViewById(R.id.txtStatusTitle)
        txtStatusSub = findViewById(R.id.txtStatusSub)
        btnEnableSettings = findViewById(R.id.btnEnableSettings)
        btnVoiceAssistant = findViewById(R.id.btnVoiceAssistant)
        viewVoiceRipple = findViewById(R.id.viewVoiceRipple)
        cardWhatsAppHelp = findViewById(R.id.cardWhatsAppHelp)
        cardBillPayHelp = findViewById(R.id.cardBillPayHelp)
        cardTestScamAlert = findViewById(R.id.cardTestScamAlert)
        btnCallFamily = findViewById(R.id.btnCallFamily)
    }

    private fun setupListeners() {
        btnVoiceAssistant.setOnClickListener {
            triggerHapticFeedback()
            animateVoiceRipple()
            speakAloud("Hello. I am Saathi, your on-device safety guardian. How may I assist you?")
        }

        btnEnableSettings.setOnClickListener {
            triggerHapticFeedback()
            if (!isAccessibilityServiceEnabled()) {
                openAccessibilitySettings()
            } else if (!Settings.canDrawOverlays(this)) {
                openOverlaySettings()
            }
        }

        cardWhatsAppHelp.setOnClickListener {
            triggerHapticFeedback()
            speakAloud("To record a voice message on WhatsApp, tap and hold the microphone icon in your chat.")
            Toast.makeText(this, "WhatsApp Voice Guide Active", Toast.LENGTH_SHORT).show()
        }

        cardBillPayHelp.setOnClickListener {
            triggerHapticFeedback()
            speakAloud("When making payments, always verify the merchant name and amount before entering your secret PIN.")
            Toast.makeText(this, "Payment Safety Guide Active", Toast.LENGTH_SHORT).show()
        }

        cardTestScamAlert.setOnClickListener {
            triggerHapticFeedback()
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please enable 'Display over other apps' to test alert", Toast.LENGTH_LONG).show()
                openOverlaySettings()
                return@setOnClickListener
            }

            val testAlert = ScamAlertData(
                category = ScamCategory.OTP_THEFT,
                riskLevel = RiskLevel.HIGH,
                titleDevanagari = "Critical Threat: OTP Intercept",
                titleEnglish = "High Risk: Confidential OTP Request",
                messageDevanagari = "Never share your confidential OTP or banking password with any caller or website.",
                messageEnglish = "Never share your confidential OTP or banking password with any caller or website.",
                triggerSnippet = "OTP: 492018"
            )

            overlayManager.showInterventionModal(testAlert) {
                Toast.makeText(this, "Threat Safely Dismissed", Toast.LENGTH_SHORT).show()
            }
        }

        btnCallFamily.setOnClickListener {
            triggerHapticFeedback()
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:112")
            }
            startActivity(dialIntent)
        }
    }

    private fun refreshProtectionState() {
        val hasAccessibility = isAccessibilityServiceEnabled()
        val hasOverlay = Settings.canDrawOverlays(this)

        if (hasAccessibility && hasOverlay) {
            cardProtectionStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.saathi_emerald_bg))
            cardProtectionStatus.strokeColor = ContextCompat.getColor(this, R.color.saathi_emerald_border)
            imgStatusIcon.setImageResource(R.drawable.ic_shield_emerald)
            imgStatusIcon.clearColorFilter()
            txtStatusTitle.text = getString(R.string.status_shield_active)
            txtStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.saathi_emerald))
            txtStatusSub.text = getString(R.string.status_shield_active_desc)
            btnEnableSettings.visibility = View.GONE
        } else {
            cardProtectionStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.saathi_amber_bg))
            cardProtectionStatus.strokeColor = ContextCompat.getColor(this, R.color.saathi_amber)
            imgStatusIcon.setImageResource(R.drawable.ic_alert_triangle)
            imgStatusIcon.clearColorFilter()
            txtStatusTitle.text = getString(R.string.status_shield_inactive)
            txtStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.saathi_amber))
            txtStatusSub.text = getString(R.string.status_shield_inactive_desc)
            btnEnableSettings.visibility = View.VISIBLE
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedPackage = packageName
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == expectedPackage }
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Unable to open Accessibility settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openOverlaySettings() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Unable to open overlay permission settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerHapticFeedback() {
        val vibrator = ContextCompat.getSystemService(this, Vibrator::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(35)
        }
    }

    private fun animateVoiceRipple() {
        viewVoiceRipple.scaleX = 0.95f
        viewVoiceRipple.scaleY = 0.95f
        viewVoiceRipple.alpha = 1.0f
        viewVoiceRipple.animate()
            .scaleX(1.4f)
            .scaleY(1.4f)
            .alpha(0.0f)
            .setDuration(800)
            .start()
    }

    private fun speakAloud(text: String) {
        if (!isTtsReady) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SaathiVoicePrompt")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.ENGLISH
            tts.setSpeechRate(0.95f)
            isTtsReady = true
        }
    }

    private fun ensureWatchdogStarted() {
        try {
            val watchdogIntent = Intent(this, WatchdogService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(watchdogIntent)
            } else {
                startService(watchdogIntent)
            }
        } catch (_: Exception) {
            // Background restrictions
        }
    }
}
