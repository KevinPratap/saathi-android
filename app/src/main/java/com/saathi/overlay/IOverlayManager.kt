package com.saathi.overlay

import com.saathi.model.ScamAlertData

/**
 * Interface contract for system alert overlay presentation.
 */
interface IOverlayManager {
    /**
     * Shows a non-blocking ambient warning banner at the top of the screen.
     */
    fun showWarningBanner(alertData: ScamAlertData)

    /**
     * Shows a modal interdiction dialog with high contrast and 3-second hold-to-dismiss barrier.
     */
    fun showInterventionModal(alertData: ScamAlertData, onDismiss: () -> Unit)

    /**
     * Dismisses and detaches the currently displayed overlay view.
     */
    fun dismissOverlay()

    /**
     * Returns true if an overlay view is currently attached to WindowManager.
     */
    fun isOverlayVisible(): Boolean
}
