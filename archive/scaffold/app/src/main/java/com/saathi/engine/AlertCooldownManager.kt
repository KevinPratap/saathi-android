package com.saathi.engine

import com.saathi.model.ScamCategory
import java.util.concurrent.ConcurrentHashMap

/**
 * Cooldown timer manager preventing alert fatigue.
 * Suppresses repeated alerts for the same pattern or category within a temporal cooldown window (default: 5 minutes).
 */
class AlertCooldownManager(private val cooldownDurationMs: Long = 300_000L) {

    private val cooldownMap = ConcurrentHashMap<String, Long>()

    /**
     * Checks if an alert key is currently on cooldown.
     */
    fun isOnCooldown(key: String, currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        val lastTrigger = cooldownMap[key] ?: return false
        return (currentTimeMs - lastTrigger) < cooldownDurationMs
    }

    /**
     * Checks if a category + package pair is currently on cooldown.
     */
    fun isCategoryOnCooldown(
        category: ScamCategory,
        packageName: String,
        currentTimeMs: Long = System.currentTimeMillis()
    ): Boolean {
        val key = buildCategoryKey(category, packageName)
        return isOnCooldown(key, currentTimeMs)
    }

    /**
     * Records an alert trigger for a specific key.
     */
    fun recordTrigger(key: String, currentTimeMs: Long = System.currentTimeMillis()) {
        cooldownMap[key] = currentTimeMs
    }

    /**
     * Records an alert trigger for a category + package pair.
     */
    fun recordCategoryTrigger(
        category: ScamCategory,
        packageName: String,
        currentTimeMs: Long = System.currentTimeMillis()
    ) {
        val key = buildCategoryKey(category, packageName)
        recordTrigger(key, currentTimeMs)
    }

    /**
     * Resets cooldown for a specific key.
     */
    fun resetCooldown(key: String) {
        cooldownMap.remove(key)
    }

    /**
     * Resets cooldown for a category + package pair.
     */
    fun resetCategoryCooldown(category: ScamCategory, packageName: String) {
        val key = buildCategoryKey(category, packageName)
        cooldownMap.remove(key)
    }

    /**
     * Cleans up expired cooldown entries to prevent memory growth.
     */
    fun pruneExpired(currentTimeMs: Long = System.currentTimeMillis()) {
        cooldownMap.entries.removeIf { (currentTimeMs - it.value) >= cooldownDurationMs }
    }

    /**
     * Clears all active cooldowns.
     */
    fun clear() {
        cooldownMap.clear()
    }

    private fun buildCategoryKey(category: ScamCategory, packageName: String): String {
        return "${packageName}_${category.name}"
    }
}
