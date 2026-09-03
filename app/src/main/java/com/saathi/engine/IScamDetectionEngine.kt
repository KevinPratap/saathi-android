package com.saathi.engine

import com.saathi.data.entity.PatternEntity
import com.saathi.model.ScamEvaluationResult
import com.saathi.model.UiNodeSnapshot

/**
 * Interface contract for on-device threat evaluation engines.
 */
interface IScamDetectionEngine {
    /**
     * Evaluates a full UI node snapshot (structured node hierarchy with bounds).
     */
    fun evaluate(snapshot: UiNodeSnapshot): ScamEvaluationResult

    /**
     * Evaluates a plain text string in the context of an application package.
     */
    fun evaluateText(text: String, packageName: String): ScamEvaluationResult

    /**
     * Loads or reloads active scam detection patterns from local persistence.
     */
    fun loadPatterns(patterns: List<PatternEntity>)

    /**
     * Adjusts sensitivity mode ("LOW", "MEDIUM", "HIGH").
     */
    fun setSensitivity(level: String)
}
