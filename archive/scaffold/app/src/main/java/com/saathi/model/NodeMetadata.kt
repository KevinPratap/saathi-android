package com.saathi.model

import android.graphics.Rect

/**
 * Lightweight, immutable representation of an accessibility node.
 */
data class NodeMetadata(
    val id: String,
    val text: String?,
    val contentDescription: String?,
    val className: String,
    val bounds: Rect,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isPassword: Boolean = false,
    val parentId: String? = null
)
