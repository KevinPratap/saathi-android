package com.saathi.util

import org.junit.Assert.*
import org.junit.Test

class ZeroPiiSanitizerTest {

    @Test
    fun testOtpRedaction() {
        val raw = "Your bank verification code is 482910. Do not share."
        val sanitized = ZeroPiiSanitizer.sanitize(raw)
        assertTrue(sanitized.contains("[REDACTED_OTP]"))
        assertFalse(sanitized.contains("482910"))
    }

    @Test
    fun testAadhaarMasking() {
        // Aadhaar format: 12 digits, grouped 4-4-4
        val raw = "Please submit Aadhaar card 2345 6789 0123 for KYC"
        val sanitized = ZeroPiiSanitizer.sanitize(raw)
        assertTrue(sanitized.contains("[REDACTED_AADHAAR]"))
        assertFalse(sanitized.contains("2345 6789 0123"))
    }

    @Test
    fun testPaymentCardMasking() {
        // 16-digit card number
        val raw = "Card ending in 4111 2222 3333 4444 has been compromised"
        val sanitized = ZeroPiiSanitizer.sanitize(raw)
        assertTrue(sanitized.contains("[REDACTED_CARD]"))
        assertFalse(sanitized.contains("4111 2222 3333 4444"))
    }

    @Test
    fun testPhoneMasking() {
        val raw = "Call bank helpline at +91 9876543210 or 8765432109 immediately"
        val sanitized = ZeroPiiSanitizer.sanitize(raw)
        assertTrue(sanitized.contains("[REDACTED_PHONE]"))
        assertFalse(sanitized.contains("9876543210"))
        assertFalse(sanitized.contains("8765432109"))
    }

    @Test
    fun testPanAndUpiMasking() {
        val raw = "PAN ABCDE1234F linked to upi vpa target@okaxis"
        val sanitized = ZeroPiiSanitizer.sanitize(raw)
        assertTrue(sanitized.contains("[REDACTED_PAN]"))
        assertTrue(sanitized.contains("[REDACTED_UPI]"))
        assertFalse(sanitized.contains("ABCDE1234F"))
        assertFalse(sanitized.contains("target@okaxis"))
    }

    @Test
    fun testEmptyInput() {
        assertEquals("", ZeroPiiSanitizer.sanitize(""))
    }
}
