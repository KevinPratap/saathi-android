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
    private lateinit var btnCallFamily: MaterialButton

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
            speakAloud("नमस्ते! मैं आपका साथी हूँ। आप क्या करना चाहते हैं? मैं आपकी सहायता के लिए तैयार हूँ।")
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
            speakAloud("WhatsApp पर आवाज़ भेजने के लिए, नीचे दाईं ओर दिए गए हरे माइक बटन को दबाकर रखें और बोलें।")
            Toast.makeText(this, "WhatsApp Voice Guide Activated", Toast.LENGTH_SHORT).show()
        }

        cardBillPayHelp.setOnClickListener {
            triggerHapticFeedback()
            speakAloud("PhonePe या Google Pay से बिजली का बिल भरने के लिए 'Bill Pay' चुनें। साथी आपको सुरक्षित रूप से रास्ता दिखाएगा।")
            Toast.makeText(this, "Bill Pay Guide Activated", Toast.LENGTH_SHORT).show()
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
                titleDevanagari = "रुकिए! सावधान (ओटीपी फ्रॉड)",
                titleEnglish = "STOP: Fraudulent OTP Request",
                messageDevanagari = "यह स्क्रीन आपसे गुप्त बैंक ओटीपी मांग रही है। किसी को यह कोड कभी न बताएं!",
                messageEnglish = "Never share your confidential OTP with unverified callers or websites.",
                triggerSnippet = "OTP: 492018"
            )

            overlayManager.showInterventionModal(testAlert) {
                Toast.makeText(this, "Alert Safely Dismissed", Toast.LENGTH_SHORT).show()
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
            cardProtectionStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.saathi_safe_light))
            cardProtectionStatus.strokeColor = ContextCompat.getColor(this, R.color.saathi_safe)
            imgStatusIcon.setImageResource(R.drawable.ic_shield_24)
            imgStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.saathi_safe))
            txtStatusTitle.text = getString(R.string.status_active_title)
            txtStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.saathi_safe_dark))
            txtStatusSub.text = getString(R.string.status_active_sub)
            btnEnableSettings.visibility = View.GONE
        } else {
            cardProtectionStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.alert_yellow_card))
            cardProtectionStatus.strokeColor = ContextCompat.getColor(this, R.color.saathi_primary_dark)
            imgStatusIcon.setImageResource(R.drawable.ic_warning_24)
            imgStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.saathi_primary_dark))
            txtStatusTitle.text = getString(R.string.status_inactive_title)
            txtStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
            txtStatusSub.text = getString(R.string.status_inactive_sub)
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
            Toast.makeText(this, "Unable to open Accessibility settings directly", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Unable to open overlay permission screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerHapticFeedback() {
        val vibrator = ContextCompat.getSystemService(this, Vibrator::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(45)
        }
    }

    private fun animateVoiceRipple() {
        viewVoiceRipple.scaleX = 0.9f
        viewVoiceRipple.scaleY = 0.9f
        viewVoiceRipple.alpha = 1.0f
        viewVoiceRipple.animate()
            .scaleX(1.35f)
            .scaleY(1.35f)
            .alpha(0.0f)
            .setDuration(900)
            .start()
    }

    private fun speakAloud(text: String) {
        if (!isTtsReady) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SaathiVoicePrompt")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.language = Locale.ENGLISH
            }
            tts.setSpeechRate(0.85f)
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
