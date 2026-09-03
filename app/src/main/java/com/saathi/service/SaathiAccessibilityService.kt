package com.saathi.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.saathi.SaathiApplication
import com.saathi.engine.IScamDetectionEngine
import com.saathi.engine.ScamDetectionEngine
import com.saathi.model.*
import com.saathi.overlay.IOverlayManager
import com.saathi.overlay.OverlayManager
import com.saathi.util.AudioFeedbackHelper
import com.saathi.watchdog.WatchdogService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * System-Bound Accessibility Service.
 * Ingests UI window content changes, performs bounded DFS traversal, constructs immutable snapshots,
 * debounces event bursts, and triggers ScamDetectionEngine and OverlayManager.
 */
class SaathiAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var debouncer: EventDebouncer
    private lateinit var scamEngine: IScamDetectionEngine
    private lateinit var overlayManager: IOverlayManager
    private lateinit var audioHelper: AudioFeedbackHelper

    override fun onCreate() {
        super.onCreate()
        debouncer = EventDebouncer(debounceDelayMs = 300L, scope = serviceScope)
        scamEngine = (applicationContext as? SaathiApplication)?.scamDetectionEngine ?: ScamDetectionEngine()
        overlayManager = OverlayManager(this)
        audioHelper = AudioFeedbackHelper(this)
        pingWatchdog()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: ""
        // Ignore events originating from Saathi's own UI overlay to prevent recursion
        if (pkgName == packageName) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                debouncer.debounce(pkgName) {
                    processActiveWindow(pkgName)
                }
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted by framework
    }

    override fun onDestroy() {
        super.onDestroy()
        debouncer.cancelAll()
        overlayManager.dismissOverlay()
        serviceScope.cancel()
    }

    private fun processActiveWindow(packageName: String) {
        val rootNode = try {
            rootInActiveWindow
        } catch (_: Exception) {
            null
        } ?: return

        try {
            val nodes = mutableListOf<NodeMetadata>()
            val textBuilder = StringBuilder()

            traverseHierarchy(rootNode, depth = 0, maxDepth = 25, maxNodes = 250, nodes, textBuilder)

            val snapshot = UiNodeSnapshot(
                packageName = packageName,
                timestampMs = System.currentTimeMillis(),
                nodes = nodes,
                flattenedText = textBuilder.toString()
            )

            evaluateSnapshot(snapshot)
            pingWatchdog()
        } finally {
            try {
                @Suppress("DEPRECATION")
                rootNode.recycle()
            } catch (_: Exception) {
                // Defensive recycle protection across Android versions
            }
        }
    }

    private fun traverseHierarchy(
        node: AccessibilityNodeInfo,
        depth: Int,
        maxDepth: Int,
        maxNodes: Int,
        nodes: MutableList<NodeMetadata>,
        textBuilder: StringBuilder
    ) {
        if (depth > maxDepth || nodes.size >= maxNodes) return

        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val nodeText = node.text?.toString()
        val contentDesc = node.contentDescription?.toString()
        val className = node.className?.toString() ?: ""

        if (!nodeText.isNullOrBlank()) {
            textBuilder.append(nodeText).append(" ")
        }
        if (!contentDesc.isNullOrBlank()) {
            textBuilder.append(contentDesc).append(" ")
        }

        val metadata = NodeMetadata(
            id = node.viewIdResourceName ?: "node_${nodes.size}",
            text = nodeText,
            contentDescription = contentDesc,
            className = className,
            bounds = bounds,
            isClickable = node.isClickable,
            isEditable = node.isEditable,
            isPassword = node.isPassword
        )
        nodes.add(metadata)

        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            try {
                traverseHierarchy(child, depth + 1, maxDepth, maxNodes, nodes, textBuilder)
            } finally {
                try {
                    @Suppress("DEPRECATION")
                    child.recycle()
                } catch (_: Exception) {
                    // Recycle protection
                }
            }
        }
    }

    private fun evaluateSnapshot(snapshot: UiNodeSnapshot) {
        val result = scamEngine.evaluate(snapshot)
        if (result is ScamEvaluationResult.ThreatDetected) {
            val alertData = buildAlertData(result)
            if (result.riskLevel == RiskLevel.HIGH) {
                audioHelper.triggerUrgencyPulse()
                overlayManager.showInterventionModal(alertData) {
                    // Handled dismiss
                }
            } else if (result.riskLevel == RiskLevel.MEDIUM || result.riskLevel == RiskLevel.LOW) {
                audioHelper.triggerGuidanceTick()
                overlayManager.showWarningBanner(alertData)
            }
        }
    }

    private fun buildAlertData(threat: ScamEvaluationResult.ThreatDetected): ScamAlertData {
        val (titleHi, titleEn) = when (threat.category) {
            ScamCategory.OTP_THEFT -> Pair("रुकिए! सावधान (ओटीपी खतरा)", "Warning: Secret OTP Request")
            ScamCategory.BANKING_KYC_FRAUD -> Pair("सावधान: बैंक/बिजली बिल धोखाधड़ी", "Warning: Fake Bank/Utility Notice")
            ScamCategory.DIGITAL_ARREST -> Pair("🛑 फर्जी पुलिस अरेस्ट नोटिस", "Warning: Fake Digital Arrest Extortion")
            ScamCategory.LOTTERY_PRIZE_SCAM -> Pair("सावधान: फर्जी लॉटरी इनाम", "Warning: Fake Lottery / Prize Scam")
            ScamCategory.REMOTE_ACCESS_COERCION -> Pair("🛑 रिमोट ऐप डाउनलोड खतरा", "Warning: Remote Access App Threat")
            ScamCategory.URGENT_TRANSFER -> Pair("सावधान: तत्काल पैसे ट्रांसफर", "Warning: Coerced Money Transfer")
            else -> Pair("सावधान: संदिग्ध गतिविधि", "Warning: Suspicious Activity Detected")
        }

        val (msgHi, msgEn) = when (threat.category) {
            ScamCategory.OTP_THEFT -> Pair(
                "यह ऐप आपसे आपका बैंक ओटीपी मांग रहा है। किसी को यह कोड कभी न बताएं!",
                "This app is requesting your confidential OTP. Never share this code!"
            )
            ScamCategory.BANKING_KYC_FRAUD -> Pair(
                "धोखेबाज खाता ब्लॉक होने या बिजली कटने का डर दिखा रहे हैं। किसी अनजान को पैसे न भेजें!",
                "Scammers are threatening account/power cutoff. Do not transfer funds!"
            )
            ScamCategory.DIGITAL_ARREST -> Pair(
                "पुलिस कभी वीडियो कॉल पर 'डिजिटल अरेस्ट' नहीं करती। डरें मत और तुरंत कॉल काटें!",
                "Law enforcement never performs 'digital arrest' calls. Hang up immediately!"
            )
            ScamCategory.REMOTE_ACCESS_COERCION -> Pair(
                "कोई अनजान व्यक्ति आपके फोन का पूरा कंट्रोल मांग रहा है। ऐप तुरंत बंद करें!",
                "A caller is attempting to gain remote control of your device. Close the app!"
            )
            else -> Pair(
                "स्क्रीन पर संदिग्ध वित्तीय धोखाधड़ी के संकेत मिले हैं। कृपया सावधान रहें।",
                "Suspicious financial patterns detected on screen. Please proceed with caution."
            )
        }

        return ScamAlertData(
            category = threat.category,
            riskLevel = threat.riskLevel,
            titleDevanagari = titleHi,
            titleEnglish = titleEn,
            messageDevanagari = msgHi,
            messageEnglish = msgEn,
            triggerSnippet = threat.triggerSnippet,
            targetBounds = threat.targetNodeBounds,
            timestampMs = System.currentTimeMillis()
        )
    }

    private fun pingWatchdog() {
        try {
            val intent = Intent(this, WatchdogService::class.java)
            startService(intent)
        } catch (_: Exception) {
            // Service ping handling
        }
    }
}
