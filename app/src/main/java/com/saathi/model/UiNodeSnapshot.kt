package com.saathi.model

/**
 * Immutable snapshot of window hierarchy at a specific point in time.
 */
data class UiNodeSnapshot(
    val packageName: String,
    val timestampMs: Long,
    val nodes: List<NodeMetadata>,
    val flattenedText: String
)
