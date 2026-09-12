package com.saathi.engine

import org.junit.Assert.*
import org.junit.Test

class LeetspeakMapperTest {

    @Test
    fun testZeroToO_InOtp() {
        // Scammer writes '0TP' with zero
        val input = "0TP"
        val mapped = LeetspeakMapper.normalizeLeetspeak(input)
        assertEquals("oTP", mapped)

        val inSentence = "Please share your 0TP immediately"
        val normalized = TextNormalizer.normalize(inSentence)
        assertEquals("please share your otp immediately", normalized)
    }

    @Test
    fun testDottedAcronym_KycAndOtp() {
        // Scammers use K.Y.C or O.T.P to break single-token matching
        val kyc = "K.Y.C"
        assertEquals("KYC", LeetspeakMapper.normalizeLeetspeak(kyc))

        val otp = "O.T.P"
        assertEquals("OTP", LeetspeakMapper.normalizeLeetspeak(otp))

        val sentence = "Update your K.Y.C now or account blocked"
        val normalized = TextNormalizer.normalize(sentence)
        assertEquals("update your kyc now or account blocked", normalized)
    }

    @Test
    fun testSymbolSubstitutions_PasswordAndShare() {
        // P@ssw0rd -> Password / password
        val password = "P@ssw0rd"
        val mapped = LeetspeakMapper.normalizeLeetspeak(password)
        assertEquals("Password", mapped)

        // sh@re -> share
        val share = "sh@re"
        assertEquals("share", LeetspeakMapper.normalizeLeetspeak(share))

        // bl0cked -> blocked
        val blocked = "bl0cked"
        assertEquals("blocked", LeetspeakMapper.normalizeLeetspeak(blocked))
    }

    @Test
    fun testComplexEvasion_CombinedLeetAndHomoglyph() {
        // Mixed: Cyrillic 'О' + leet '@' + zero-width + dotted acronym
        val complex = "\u041E.T.P sh@re k@ro"
        val normalized = TextNormalizer.normalize(complex)
        assertEquals("otp share karo", normalized)
    }
}
