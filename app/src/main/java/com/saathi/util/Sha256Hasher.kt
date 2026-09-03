package com.saathi.util

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Cryptographic salted SHA-256 hasher for tamper-evident, irreversible audit logging.
 */
object Sha256Hasher {

    /**
     * Hashes an audit log record with a unique device salt.
     * Formula: SHA-256(RuleID || PackageName || TimestampMs || Salt)
     */
    fun hashAuditRecord(
        ruleId: String,
        packageName: String,
        timestampMs: Long,
        deviceSalt: ByteArray
    ): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(deviceSalt)
        digest.update(ruleId.toByteArray(Charsets.UTF_8))
        digest.update(packageName.toByteArray(Charsets.UTF_8))
        digest.update(timestampMs.toString().toByteArray(Charsets.UTF_8))

        val hashBytes = digest.digest()
        return bytesToHex(hashBytes)
    }

    /**
     * Generic salted hash for strings.
     */
    fun hashString(input: String, salt: ByteArray? = null): String {
        val digest = MessageDigest.getInstance("SHA-256")
        if (salt != null) {
            digest.update(salt)
        }
        digest.update(input.toByteArray(Charsets.UTF_8))
        return bytesToHex(digest.digest())
    }

    /**
     * Generates a 256-bit cryptographically secure random salt.
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexString = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}
