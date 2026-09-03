package com.saathi.engine

import org.junit.Assert.*
import org.junit.Test

class HomoglyphMapperTest {

    @Test
    fun testCyrillicOtpAttack() {
        // Cyrillic 'О' (\u041E) and 'Т' (\u0422) with Latin 'P'
        val cyrillicOtp = "\u041E\u0422P"
        val mapped = HomoglyphMapper.mapHomoglyphs(cyrillicOtp)
        assertEquals("OTP", mapped)

        // Lowercase Cyrillic 'о' (\u043E) and 'р' (\u0440)
        val cyrillicLower = "\u043E\u0442\u0440"
        val mappedLower = HomoglyphMapper.mapHomoglyphs(cyrillicLower)
        assertEquals("otp", mappedLower)
    }

    @Test
    fun testGreekBankAttack() {
        // Greek 'Β' (\u0392), 'Α' (\u0391), 'Ν' (\u039D), 'Κ' (\u039A)
        val greekBank = "\u0392\u0391\u039D\u039A"
        val mapped = HomoglyphMapper.mapHomoglyphs(greekBank)
        assertEquals("BANK", mapped)
    }

    @Test
    fun testMixedScriptSbiAttack() {
        // Cyrillic Dze 'Ѕ' (\u0405), Cyrillic Ve 'В' (\u0412), Latin 'I'
        val mixedSbi = "\u0405\u0412I"
        val mapped = HomoglyphMapper.mapHomoglyphs(mixedSbi)
        assertEquals("SBI", mapped)
    }

    @Test
    fun testDevanagariDigitsTranslation() {
        // Devanagari numbers ९८७६५४३२१० -> 9876543210
        val devanagariPhone = "\u096F\u096E\u096D\u096C\u096B\u096A\u0969\u0968\u0967\u0966"
        val mapped = HomoglyphMapper.mapHomoglyphs(devanagariPhone)
        assertEquals("9876543210", mapped)
    }

    @Test
    fun testEndToEndNormalizerIntegration() {
        // Full attack: Cyrillic homoglyphs + zero-width non-joiner
        val attackString = "\u041E\u200C\u0422\u200CP \u0430\u0441\u0441ount"
        val normalized = TextNormalizer.normalize(attackString)
        assertEquals("otp account", normalized)
    }
}
