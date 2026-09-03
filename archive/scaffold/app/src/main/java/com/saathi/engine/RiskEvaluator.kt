package com.saathi.engine

import com.saathi.model.RiskLevel
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Multi-factor heuristic risk evaluator.
 * Synthesizes pattern matches, package trust context, urgency, and spatial proximity into a risk score [0..100].
 */
object RiskEvaluator {

    // Whitelisted Indian payment and authentic banking packages
    private val WHITELISTED_BANKING_PACKAGES = setOf(
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user", // Google Pay
        "net.one97.paytm",
        "com.sbi.upi",
        "com.icicibank.mobile",
        "com.axis.mobile",
        "com.hdfcbank.android",
        "in.org.npci.upiapp"
    )

    // High-risk messaging and external vector packages
    private val MESSAGING_PACKAGES = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "org.telegram.messenger",
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.facebook.orca",
        "com.truecaller"
    )

    /**
     * Determines package trust multiplier M_pkg.
     */
    fun getPackageMultiplier(packageName: String): Double {
        return when {
            WHITELISTED_BANKING_PACKAGES.contains(packageName) -> 0.15
            MESSAGING_PACKAGES.contains(packageName) -> 1.35
            else -> 1.0
        }
    }

    /**
     * Determines sensitivity multiplier based on user preference.
     */
    fun getSensitivityMultiplier(sensitivity: String): Double {
        return when (sensitivity.uppercase()) {
            "LOW" -> 0.85
            "HIGH" -> 1.25
            else -> 1.0 // "MEDIUM"
        }
    }

    /**
     * Computes composite risk score S in [0..100].
     * Formula: S = min(100, BaseWeight * M_pkg * M_spatial * M_action * M_sensitivity)
     */
    fun computeScore(
        baseWeight: Int,
        packageName: String,
        hasActionableNode: Boolean = false,
        hasSpatialProximity: Boolean = false,
        sensitivity: String = "MEDIUM"
    ): Int {
        val mPkg = getPackageMultiplier(packageName)
        val mSpatial = if (hasSpatialProximity) 1.25 else 1.0
        val mAction = if (hasActionableNode) 1.30 else 1.0
        val mSensitivity = getSensitivityMultiplier(sensitivity)

        val rawScore = baseWeight * mPkg * mSpatial * mAction * mSensitivity
        return min(100, rawScore.roundToInt()).coerceAtLeast(0)
    }

    /**
     * Maps numerical composite score to discrete RiskLevel.
     */
    fun determineRiskLevel(score: Int): RiskLevel {
        return when {
            score >= 70 -> RiskLevel.HIGH
            score >= 40 -> RiskLevel.MEDIUM
            score >= 25 -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }
    }
}
