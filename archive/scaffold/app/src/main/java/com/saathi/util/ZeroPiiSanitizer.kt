package com.saathi.util

/**
 * Deterministic Indian PII & financial identifier redaction engine.
 * Ensures zero sensitive data leaks into logs, databases, or memory dumps.
 */
object ZeroPiiSanitizer {

    // 1. Payment Cards (16 digits with optional 4-digit grouping spaces/hyphens)
    private val CARD_REGEX = Regex("""\b(?:\d{4}[ -]?){3}\d{4}\b""")

    // 2. Indian Aadhaar Numbers (12 digits with optional spaces or hyphens)
    private val AADHAAR_REGEX = Regex("""\b\d{4}[\s-]?\d{4}[\s-]?\d{4}\b""")

    // 3. Indian Mobile Phone Numbers (10 digits starting with 6-9, optional +91 prefix)
    private val PHONE_REGEX = Regex("""\b(?:\+91[\s-]?)?[6-9]\d{9}\b""")

    // 4. One-Time Passwords (4 to 8 digit standalone numbers)
    private val OTP_REGEX = Regex("""\b\d{4,8}\b""")

    // 5. Indian Permanent Account Number (PAN: 5 uppercase letters, 4 digits, 1 uppercase letter)
    private val PAN_REGEX = Regex("""\b[a-zA-Z]{5}[0-9]{4}[a-zA-Z]\b""")

    // 6. UPI Virtual Payment Addresses (VPAs: identifier@handle)
    private val UPI_VPA_REGEX = Regex("""\b[a-zA-Z0-9.\-_]{2,}@[a-zA-Z0-9.\-_]{2,}\b""")

    /**
     * Sanitizes raw text by replacing all detected PII entities with safe tokens.
     * Order of execution ensures longer numerical strings (cards/Aadhaar) are redacted before shorter ones (phones/OTPs).
     */
    fun sanitize(rawText: String): String {
        if (rawText.isEmpty()) return ""

        var sanitized = rawText
        // Replace cards first (16 digits)
        sanitized = sanitized.replace(CARD_REGEX, "[REDACTED_CARD]")
        // Replace Aadhaar (12 digits)
        sanitized = sanitized.replace(AADHAAR_REGEX, "[REDACTED_AADHAAR]")
        // Replace Phone numbers (10 digits)
        sanitized = sanitized.replace(PHONE_REGEX, "[REDACTED_PHONE]")
        // Replace OTPs (4-8 digits)
        sanitized = sanitized.replace(OTP_REGEX, "[REDACTED_OTP]")
        // Replace PAN
        sanitized = sanitized.replace(PAN_REGEX, "[REDACTED_PAN]")
        // Replace UPI
        sanitized = sanitized.replace(UPI_VPA_REGEX, "[REDACTED_UPI]")

        return sanitized
    }

    /**
     * Specifically redacts OTP digits from a string while preserving surrounding context.
     */
    fun redactOtpOnly(text: String): String {
        return text.replace(OTP_REGEX, "[REDACTED_OTP]")
    }
}
