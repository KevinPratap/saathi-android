package com.saathi.engine

import com.saathi.model.ScamCategory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TrieTest {

    private lateinit var trie: Trie

    @Before
    fun setUp() {
        trie = Trie()
        trie.insert("otp", ScamCategory.OTP_THEFT, "RULE_OTP")
        trie.insert("verification code", ScamCategory.OTP_THEFT, "RULE_OTP")
        trie.insert("account blocked", ScamCategory.BANKING_KYC_FRAUD, "RULE_KYC")
        trie.insert("digital arrest", ScamCategory.DIGITAL_ARREST, "RULE_ARREST")
        trie.insert("anydesk", ScamCategory.REMOTE_ACCESS_COERCION, "RULE_RAT")
        trie.insert("ओटीपी", ScamCategory.OTP_THEFT, "RULE_OTP_HI")
        trie.buildFailureTransitions()
    }

    @Test
    fun testExactMatch_Success() {
        val match = trie.exactMatch("otp")
        assertNotNull("Exact match for 'otp' should succeed", match)
        assertEquals("otp", match?.keyword)
        assertEquals(ScamCategory.OTP_THEFT, match?.category)
        assertEquals("RULE_OTP", match?.ruleId)
    }

    @Test
    fun testExactMatch_CaseInsensitive() {
        val matchUpper = trie.exactMatch("OTP")
        assertNotNull("Exact match for 'OTP' should succeed", matchUpper)
        assertEquals("otp", matchUpper?.keyword)

        val matchMixed = trie.exactMatch("AcCoUnT BlOcKeD")
        assertNotNull(matchMixed)
        assertEquals(ScamCategory.BANKING_KYC_FRAUD, matchMixed?.category)
    }

    @Test
    fun testExactMatch_Failures() {
        assertNull("Partial match 'ot' should not be an exact match", trie.exactMatch("ot"))
        assertNull("Non-existent keyword should return null", trie.exactMatch("hello"))
        assertNull("Empty string should return null", trie.exactMatch(""))
        assertNull("Blank string should return null", trie.exactMatch("   "))
    }

    @Test
    fun testPrefixMatch_StartsWith() {
        assertTrue("Prefix 'ot' should match 'otp'", trie.startsWith("ot"))
        assertTrue("Prefix 'OT' should match 'otp' case-insensitively", trie.startsWith("OT"))
        assertTrue("Prefix 'account' should match 'account blocked'", trie.startsWith("account"))
        assertTrue("Prefix 'dig' should match 'digital arrest'", trie.startsWith("dig"))
        assertFalse("Prefix 'xyz' should return false", trie.startsWith("xyz"))
        assertFalse("Empty string should return false", trie.startsWith(""))
    }

    @Test
    fun testMultiPatternSearch_AhoCorasick() {
        val text = "Dear customer, your account blocked! Please share your OTP verification code or install AnyDesk."
        val matches = trie.search(text)

        assertTrue("Should detect multiple patterns in text", matches.size >= 4)
        val matchedKeywords = matches.map { it.keyword }
        assertTrue(matchedKeywords.contains("account blocked"))
        assertTrue(matchedKeywords.contains("otp"))
        assertTrue(matchedKeywords.contains("verification code"))
        assertTrue(matchedKeywords.contains("anydesk"))
    }

    @Test
    fun testMultiPatternSearch_Devanagari() {
        val text = "कृपया अपना बैंक ओटीपी किसी से शेयर न करें"
        val matches = trie.search(text)

        assertEquals(1, matches.size)
        assertEquals("ओटीपी", matches[0].keyword)
        assertEquals(ScamCategory.OTP_THEFT, matches[0].category)
    }

    @Test
    fun testSearch_EmptyAndWhitespace() {
        assertTrue(trie.search("").isEmpty())
        assertTrue(trie.search("   ").isEmpty())
        assertTrue(trie.search("Hello world, this is a completely benign sentence.").isEmpty())
    }

    @Test
    fun testClear_ResetsTrie() {
        trie.clear()
        assertNull(trie.exactMatch("otp"))
        assertFalse(trie.startsWith("ot"))
        assertTrue(trie.search("share otp").isEmpty())
    }
}
