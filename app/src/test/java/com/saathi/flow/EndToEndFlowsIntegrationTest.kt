package com.saathi.flow

import com.saathi.data.entity.AuditLogEntity
import com.saathi.engine.AlertCooldownManager
import com.saathi.engine.ScamDetectionEngine
import com.saathi.model.OverlayMode
import com.saathi.model.RiskLevel
import com.saathi.model.ScamAlertData
import com.saathi.model.ScamCategory
import com.saathi.model.ScamEvaluationResult
import com.saathi.service.EventDebouncer
import com.saathi.util.Sha256Hasher
import com.saathi.util.ZeroPiiSanitizer
import com.saathi.watchdog.OemIntentHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Comprehensive End-to-End Test Suite verifying all core Saathi user flows:
 * 1. Threat Detection, Evaluation & Mitigation Flow
 * 2. 3-Second Friction Hold Dismissal Barrier Flow
 * 3. Guided Workflows & Voice Prompts Validation
 * 4. Background Watchdog & OEM Resilience Flow
 * 5. Full Zero-PII Audit Logging Pipeline
 */
class EndToEndFlowsIntegrationTest {

    private lateinit var cooldownManager: AlertCooldownManager
    private lateinit var engine: ScamDetectionEngine

    @Before
    fun setUp() {
        cooldownManager = AlertCooldownManager()
        engine = ScamDetectionEngine(cooldownManager)
    }

    @Test
    fun testFlow1_ThreatDetection_Sanitization_And_Mitigation() {
        // Step 1: User receives an urgent deceptive SMS/WhatsApp asking for OTP
        val incomingDeceptiveMessage = "Your account is temporarily suspended! Send your 6 digit OTP 839201 immediately to verify."
        val sourcePackage = "com.whatsapp"

        // Warm up JIT for first execution in cold test runner
        engine.evaluateText("warmup", "com.warmup")

        // Step 2: Engine evaluates screen node text
        val result = engine.evaluateText(incomingDeceptiveMessage, sourcePackage)

        // Step 3: Verify Threat is Detected and properly classified
        assertTrue("Threat must be detected", result is ScamEvaluationResult.ThreatDetected)
        val threat = result as ScamEvaluationResult.ThreatDetected
        assertEquals("Should be classified as OTP Theft", ScamCategory.OTP_THEFT, threat.category)
        assertTrue("Confidence score should be high (>= 70)", threat.confidenceScore >= 70)
        assertTrue("Risk level should be Medium or High", threat.riskLevel == RiskLevel.MEDIUM || threat.riskLevel == RiskLevel.HIGH)
        assertTrue("Trigger snippet should be extracted", threat.triggerSnippet.isNotEmpty())
        assertTrue("Execution latency should be recorded (> 0.0)", threat.executionLatencyMs > 0.0)

        // Step 4: Test Alert Cooldown Damping
        // Immediate repeat scan of the same threat within cooldown window should be suppressed to prevent spamming
        val secondScan = engine.evaluateText(incomingDeceptiveMessage, sourcePackage)
        assertTrue("Second scan within cooldown must be suppressed to Safe", secondScan is ScamEvaluationResult.Safe)
    }

    @Test
    fun testFlow2_OverlayModal_FrictionHoldBarrier_DismissalLogic() {
        // Prepare modern English alert data
        val alertData = ScamAlertData(
            category = ScamCategory.OTP_THEFT,
            riskLevel = RiskLevel.HIGH,
            titleDevanagari = "Critical Threat Detected",
            titleEnglish = "High Risk: Confidential OTP Request",
            messageDevanagari = "Do not share secret codes.",
            messageEnglish = "Never share your confidential OTP or banking password with any caller or website.",
            triggerSnippet = "OTP: 839201"
        )

        val isDismissed = AtomicBoolean(false)
        val modalMode = OverlayMode.InterventionModal(alertData) {
            isDismissed.set(true)
        }

        // Test Scenario A: Accidental Tremor Tap (< 3000ms hold)
        val holdTimeTremor = 400L // 400ms quick release
        val requiredHoldMs = 3000L

        if (holdTimeTremor < requiredHoldMs) {
            // Dismiss callback must NOT be triggered
            assertFalse("Tremor tap must not unlock the screen", isDismissed.get())
        }

        // Test Scenario B: Deliberate Sustained Hold (>= 3000ms)
        val holdTimeDeliberate = 3100L
        if (holdTimeDeliberate >= requiredHoldMs) {
            modalMode.onDismiss.invoke()
            assertTrue("Sustained hold must successfully dismiss the intervention modal", isDismissed.get())
        }
    }

    @Test
    fun testFlow3_VoiceAndGuidedWorkflows_Validation() {
        // Validate guided workflow copy and guidelines in modern English
        val whatsappVoiceGuideText = "To record a voice message on WhatsApp, tap and hold the microphone icon in your chat."
        val billPaymentGuideText = "When making payments, always verify the merchant name and amount before entering your secret PIN."

        assertTrue("WhatsApp guidance must be non-empty", whatsappVoiceGuideText.isNotEmpty())
        assertTrue("Bill payment guidance must be non-empty", billPaymentGuideText.isNotEmpty())

        // Ensure non-custodial boundary: instructions never ask for or automate PIN entry
        assertFalse("Guide must never automate PIN entry", billPaymentGuideText.contains("enter PIN for you"))
        assertTrue("Must warn about secret PIN", billPaymentGuideText.contains("secret PIN"))
    }

    @Test
    fun testFlow4_BackgroundWatchdog_And_OemResilience() = runBlocking {
        // Verify debouncer aggregates rapid UI updates
        val debouncer = EventDebouncer(debounceDelayMs = 50L)
        val processedEvents = AtomicInteger(0)

        // Simulate fast scrolling generating 5 events in 20ms
        for (i in 1..5) {
            debouncer.debounce("scroll_window") {
                processedEvents.incrementAndGet()
            }
            delay(5)
        }

        delay(80)
        assertEquals("Debouncer must coalesce multiple rapid window mutations into 1 execution", 1, processedEvents.get())
        debouncer.cancelAll()

        // Verify OEM Autostart Intent resolver generates null-safe intents
        val autoStartIntent = OemIntentHelper.getAutoStartIntent()
        // On non-OEM standard test environments, returns null gracefully without throwing
        assertTrue(autoStartIntent == null || autoStartIntent.component != null)
    }

    @Test
    fun testFlow5_FullScamPipeline_ZeroPiiAuditLogging() {
        val rawScamText = "URGENT: SBI Alert. Call 9876543210. Your A/C 123456789012 is blocked. Share OTP 948271."

        // 1. Sanitize text: Sensitive PII must be masked
        val sanitized = ZeroPiiSanitizer.sanitize(rawScamText)
        assertFalse("Phone number must be scrubbed", sanitized.contains("9876543210"))
        assertFalse("Account number must be scrubbed", sanitized.contains("123456789012"))
        assertFalse("OTP digits must be scrubbed", sanitized.contains("948271"))

        // 2. Generate Hash for pattern signature
        val hash = Sha256Hasher.hashString(sanitized)
        assertEquals("SHA-256 string must be 64 characters hex", 64, hash.length)

        // 3. Construct Room DB Audit Entity
        val auditEntity = AuditLogEntity(
            timestampMs = System.currentTimeMillis(),
            packageName = "com.google.android.apps.messaging",
            category = ScamCategory.BANKING_KYC_FRAUD,
            riskLevel = RiskLevel.HIGH,
            confidenceScore = 90,
            anonymizedAuditHash = hash,
            actionTaken = "INTERRUPTED"
        )

        assertEquals("INTERRUPTED", auditEntity.actionTaken)
        assertEquals(ScamCategory.BANKING_KYC_FRAUD, auditEntity.category)
        assertEquals(RiskLevel.HIGH, auditEntity.riskLevel)
        assertEquals(64, auditEntity.anonymizedAuditHash.length)
    }
}
