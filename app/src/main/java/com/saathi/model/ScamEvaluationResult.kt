package com.saathi.model

import android.graphics.Rect

/**
 * Result emitted by ScamDetectionEngine after evaluating UI node snapshot or text.
 */
sealed class ScamEvaluationResult {
    object Safe : ScamEvaluationResult()

    data class ThreatDetected(
        val category: ScamCategory,
        val riskLevel: RiskLevel,
        val confidenceScore: Int,
        val matchedRuleId: String,
        val triggerSnippet: String,
        val targetNodeBounds: Rect? = null,
        val executionLatencyMs: Double = 0.0
    ) : ScamEvaluationResult()
}
