package com.saathi.engine

import com.saathi.model.RiskLevel
import com.saathi.model.ScamCategory
import com.saathi.model.ScamEvaluationResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ScamDetectionEngineTest {

    private lateinit var engine: ScamDetectionEngine

    @Before
    fun setUp() {
        engine = ScamDetectionEngine(AlertCooldownManager(cooldownDurationMs = 1000L))
    }

    @Test
    fun testBankKycCoercion() {
        val message = "ALERT: Dear customer, your SBI YONO account blocked today. Update your KYC now or card will be suspended."
        val result = engine.evaluateText(message, "com.whatsapp")

        assertTrue("Should detect Bank KYC fraud", result is ScamEvaluationResult.ThreatDetected)
        val threat = result as ScamEvaluationResult.ThreatDetected
        assertEquals(ScamCategory.BANKING_KYC_FRAUD, threat.category)
        assertTrue("Confidence score should be high", threat.confidenceScore >= 70)
        assertEquals(RiskLevel.HIGH, threat.riskLevel)
    }

    @Test
    fun testElectricityBillCutOff() {
        val message = "Dear consumer, electricity power will be cut tonight at 9:30 PM due to unpaid electricity bill. Call officer immediately."
        val result = engine.evaluateText(message, "com.google.android.apps.messaging")

        assertTrue("Should detect electricity bill scam", result is ScamEvaluationResult.ThreatDetected)
        val threat = result as ScamEvaluationResult.ThreatDetected
        assertEquals(ScamCategory.BANKING_KYC_FRAUD, threat.category)
        assertEquals(RiskLevel.HIGH, threat.riskLevel)
    }

    @Test
    fun testApkDownloadAndRemoteAccess() {
        val message = "Please download and install AnyDesk APK and share 9-digit code for bank customer support."
        val result = engine.evaluateText(message, "com.whatsapp")

        assertTrue("Should detect remote access APK scam", result is ScamEvaluationResult.ThreatDetected)
        val threat = result as ScamEvaluationResult.ThreatDetected
        assertEquals(ScamCategory.REMOTE_ACCESS_COERCION, threat.category)
        assertEquals(RiskLevel.HIGH, threat.riskLevel)
    }

    @Test
    fun testLotteryAndKbcScam() {
        val message = "Congratulations! You won ₹25 lakh cash prize in KBC lottery lucky draw. Claim prize now."
        val result = engine.evaluateText(message, "com.whatsapp")

        assertTrue("Should detect lottery prize scam", result is ScamEvaluationResult.ThreatDetected)
        val threat = result as ScamEvaluationResult.ThreatDetected
        assertEquals(ScamCategory.LOTTERY_PRIZE_SCAM, threat.category)
        assertTrue(threat.confidenceScore >= 70)
    }

    @Test
    fun testCustomerSupportFakeNumberAndOtp() {
        val message = "Dear user, customer support executive needs your OTP verification code to resolve transaction."
        val result = engine.evaluateText(message, "com.whatsapp")

        assertTrue("Should detect OTP theft", result is ScamEvaluationResult.ThreatDetected)
        val threat = result as ScamEvaluationResult.ThreatDetected
        assertEquals(ScamCategory.OTP_THEFT, threat.category)
        assertEquals(RiskLevel.HIGH, threat.riskLevel)
    }

    @Test
    fun testSafeBenignMessage() {
        val benign = "Hello dadi, I will visit you this Sunday afternoon. Take care!"
        val result = engine.evaluateText(benign, "com.whatsapp")

        assertTrue("Benign family message must return Safe", result is ScamEvaluationResult.Safe)
    }

    @Test
    fun testWhitelistedAppSuppression() {
        // In PhonePe or Google Pay, standard display of OTP or account does not cause panic
        val text = "Enter 6-digit OTP to authenticate transaction"
        val result = engine.evaluateText(text, "com.phonepe.app")

        // Because PhonePe is whitelisted (M_pkg = 0.15), the composite score is reduced
        if (result is ScamEvaluationResult.ThreatDetected) {
            assertTrue("Whitelisted app score should be heavily dampened", result.confidenceScore < 40)
        } else {
            assertTrue(result is ScamEvaluationResult.Safe)
        }
    }
}
