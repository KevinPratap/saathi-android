package com.saathi.engine

import org.junit.Assert.*
import org.junit.Test

class TextNormalizerTest {

    @Test
    fun testZeroWidthCharacterRemoval() {
        // Scammer embeds zero-width spaces (\u200B) and BOM (\uFEFF) to break keyword matching
        val obfuscated = "O\u200BT\u200CP\uFEFF sh\u00ADare"
        val normalized = TextNormalizer.normalize(obfuscated)
        assertEquals("otp share", normalized)
    }

    @Test
    fun testUnicodeNFKDNormalization() {
        // Decompose compatibility characters (e.g. ligature ﬁ, superscript digits, accented letters)
        val input = "ﬁnancial fırst vërifÿ"
        val normalized = TextNormalizer.normalize(input)
        assertTrue(normalized.contains("financial"))
    }

    @Test
    fun testDevanagariPreservation() {
        val devanagariText = "प्रिय ग्राहक, आपका बैंक खाता ब्लॉक हो गया है"
        val normalized = TextNormalizer.normalize(devanagariText)

        assertTrue("Devanagari text must be preserved", normalized.contains("प्रिय ग्राहक"))
        assertTrue("Devanagari keywords preserved", normalized.contains("खाता ब्लॉक"))
    }

    @Test
    fun testPunctuationStripping_WithDevanagari() {
        val textWithPunct = "चेतावनी!! आपका खाता: ब्लॉक! तुरंत... संपर्क करें?"
        val stripped = TextNormalizer.normalizeAndStripPunctuation(textWithPunct)

        assertEquals("चेतावनी आपका खाता ब्लॉक तुरंत संपर्क करें", stripped)
    }

    @Test
    fun testWhitespaceCompaction() {
        val messyWhitespace = "   urgent    kyc  \n\t  expired   now   "
        val normalized = TextNormalizer.normalize(messyWhitespace)
        assertEquals("urgent kyc expired now", normalized)
    }

    @Test
    fun testEmptyAndBlankStrings() {
        assertEquals("", TextNormalizer.normalize(""))
        assertEquals("", TextNormalizer.normalize("   "))
        assertEquals("", TextNormalizer.normalizeAndStripPunctuation(""))
    }
}
