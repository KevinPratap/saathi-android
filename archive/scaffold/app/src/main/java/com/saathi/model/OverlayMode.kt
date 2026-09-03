package com.saathi.model

/**
 * Overlay presentation modes: ambient warning banner vs 3s-hold intervention modal.
 */
sealed class OverlayMode {
    data class AmbientBanner(val alertData: ScamAlertData) : OverlayMode()
    data class InterventionModal(
        val alertData: ScamAlertData,
        val onDismiss: () -> Unit
    ) : OverlayMode()
}
