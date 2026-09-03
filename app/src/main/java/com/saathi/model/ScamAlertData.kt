package com.saathi.model

import android.graphics.Rect

/**
 * Data payload for high-contrast senior-friendly alert overlays.
 */
data class ScamAlertData(
    val category: ScamCategory,
    val riskLevel: RiskLevel,
    val titleDevanagari: String,
    val titleEnglish: String,
    val messageDevanagari: String,
    val messageEnglish: String,
    val triggerSnippet: String,
    val targetBounds: Rect? = null,
    val timestampMs: Long = System.currentTimeMillis()
)
