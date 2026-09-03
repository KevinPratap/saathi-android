package com.saathi.util

import org.junit.Assert.*
import org.junit.Test

class Sha256HasherTest {

    @Test
    fun testSaltedHashDeterministic() {
        val salt = "0123456789abcdef0123456789abcdef".toByteArray()
        val ruleId = "RULE_OTP_HARVEST"
        val pkg = "com.whatsapp"
        val timestamp = 1725321600000L

        val hash1 = Sha256Hasher.hashAuditRecord(ruleId, pkg, timestamp, salt)
        val hash2 = Sha256Hasher.hashAuditRecord(ruleId, pkg, timestamp, salt)

        assertEquals("Same inputs and salt must yield identical hash", hash1, hash2)
        assertEquals("SHA-256 hex string must be 64 characters long", 64, hash1.length)
        assertTrue("Hash must be valid hex characters", hash1.matches(Regex("^[0-9a-f]{64}$")))
    }

    @Test
    fun testSaltDifferentiatesHash() {
        val salt1 = ByteArray(32) { 1 }
        val salt2 = ByteArray(32) { 2 }
        val ruleId = "RULE_OTP_HARVEST"
        val pkg = "com.whatsapp"
        val timestamp = 1725321600000L

        val hash1 = Sha256Hasher.hashAuditRecord(ruleId, pkg, timestamp, salt1)
        val hash2 = Sha256Hasher.hashAuditRecord(ruleId, pkg, timestamp, salt2)

        assertNotEquals("Different salts must produce completely distinct hashes", hash1, hash2)
    }

    @Test
    fun testSaltGeneratorLength() {
        val salt = Sha256Hasher.generateSalt()
        assertEquals(32, salt.size)
    }

    @Test
    fun testGenericHashString() {
        val input = "SaathiSecureGuard"
        val hash = Sha256Hasher.hashString(input)
        assertEquals(64, hash.length)
    }
}
