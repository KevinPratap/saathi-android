package com.saathi.engine

import com.saathi.model.ScamCategory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AlertCooldownManagerTest {

    private lateinit var cooldownManager: AlertCooldownManager
    private val cooldownDurationMs = 5000L // 5 seconds for fast test execution

    @Before
    fun setUp() {
        cooldownManager = AlertCooldownManager(cooldownDurationMs)
    }

    @Test
    fun testCooldownTriggerAndSuppression() {
        val key = "com.whatsapp_RULE_OTP"
        val t0 = 1000000L

        assertFalse("Initially should not be on cooldown", cooldownManager.isOnCooldown(key, t0))

        cooldownManager.recordTrigger(key, t0)
        assertTrue("Immediately after trigger, should be on cooldown", cooldownManager.isOnCooldown(key, t0))
        assertTrue("During cooldown window, should still be on cooldown", cooldownManager.isOnCooldown(key, t0 + 2000L))
    }

    @Test
    fun testCooldownExpiry() {
        val key = "com.whatsapp_RULE_OTP"
        val t0 = 1000000L

        cooldownManager.recordTrigger(key, t0)
        // Check at t0 + 5001ms (beyond 5000ms cooldown)
        assertFalse("After cooldown duration elapses, should expire", cooldownManager.isOnCooldown(key, t0 + 5001L))
    }

    @Test
    fun testPerCategoryCooldownSeparation() {
        val pkg = "com.whatsapp"
        val t0 = 1000000L

        cooldownManager.recordCategoryTrigger(ScamCategory.OTP_THEFT, pkg, t0)

        assertTrue(
            "OTP_THEFT should be on cooldown",
            cooldownManager.isCategoryOnCooldown(ScamCategory.OTP_THEFT, pkg, t0 + 1000L)
        )
        assertFalse(
            "BANKING_KYC_FRAUD should NOT be on cooldown even for same package",
            cooldownManager.isCategoryOnCooldown(ScamCategory.BANKING_KYC_FRAUD, pkg, t0 + 1000L)
        )
        assertFalse(
            "OTP_THEFT on a different package should NOT be on cooldown",
            cooldownManager.isCategoryOnCooldown(ScamCategory.OTP_THEFT, "org.telegram.messenger", t0 + 1000L)
        )
    }

    @Test
    fun testResetAndClear() {
        val key1 = "alert_1"
        val key2 = "alert_2"
        val t0 = 1000000L

        cooldownManager.recordTrigger(key1, t0)
        cooldownManager.recordTrigger(key2, t0)

        cooldownManager.resetCooldown(key1)
        assertFalse(cooldownManager.isOnCooldown(key1, t0))
        assertTrue(cooldownManager.isOnCooldown(key2, t0))

        cooldownManager.clear()
        assertFalse(cooldownManager.isOnCooldown(key2, t0))
    }
}
