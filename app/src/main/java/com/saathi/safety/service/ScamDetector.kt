package com.saathi.safety.service

/**
 * Lightweight on-device scam detection.
 * Uses keyword matching and regex patterns.
 */
object ScamDetector {
    
    // English scam keywords
    private val englishPatterns = listOf(
        Regex("you('ve| have) won", RegexOption.IGNORE_CASE),
        Regex("congratulations", RegexOption.IGNORE_CASE),
        Regex("urgent(ly)?", RegexOption.IGNORE_CASE),
        Regex("bank( )?alert", RegexOption.IGNORE_CASE),
        Regex("account( )?(blocked|suspended|locked)", RegexOption.IGNORE_CASE),
        Regex("KYC( )?(expired|required|update)", RegexOption.IGNORE_CASE),
        Regex("OTP( )?(verify|confirm|share)", RegexOption.IGNORE_CASE),
        Regex("send( )?money( )?now", RegexOption.IGNORE_CASE),
        Regex("claim( )?(your|prize|reward)", RegexOption.IGNORE_CASE),
        Regex("(lottery|prize|reward)( )?won", RegexOption.IGNORE_CASE)
    )
    
    // Hindi scam keywords (Devanagari)
    private val hindiKeywords = listOf(
        "जीत", "बधाई", "तुरंत", "खाता", "ब्लॉक", "ओटीपी",
        "भेजें", "पैसे", "अकाउंट", "सस्पेंड", "वेरिफाई"
    )
    
    // Minimum number of matches to trigger warning
    private const val ENGLISH_MATCH_THRESHOLD = 1
    private const val HINDI_MATCH_THRESHOLD = 2
    
    fun isScam(text: String): Boolean {
        if (text.isBlank()) return false
        
        val englishMatches = englishPatterns.count { it.containsMatchIn(text) }
        if (englishMatches >= ENGLISH_MATCH_THRESHOLD) return true
        
        val hindiMatches = hindiKeywords.count { text.contains(it) }
        if (hindiMatches >= HINDI_MATCH_THRESHOLD) return true
        
        return false
    }
}
