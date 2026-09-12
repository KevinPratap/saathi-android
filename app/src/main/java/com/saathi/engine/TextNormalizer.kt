package com.saathi.engine

import java.text.Normalizer

/**
 * 5-stage linguistic normalization pipeline.
 * Deconstructs obfuscation, eliminates invisible evasion characters, maps homoglyphs,
 * and preserves Hindi Devanagari script integrity.
 */
object TextNormalizer {

    private val ZERO_WIDTH_REGEX = Regex("""[\u200B-\u200F\uFEFF\u00AD\u202A-\u202E\u2060-\u206F]""")
    private val WHITESPACE_REGEX = Regex("""\s+""")

    /**
     * Executes the complete normalization pipeline:
     * 1. Unicode NFKD decomposition
     * 2. Zero-width and control character stripping
     * 3. Confusable homoglyph translation
     * 4. Leetspeak & acronym unfolding
     * 5. Lowercasing and whitespace collapse
     */
    fun normalize(input: String): String {
        if (input.isEmpty()) return ""

        // Stage 1: Unicode NFKD Decomposition
        val nfkd = Normalizer.normalize(input, Normalizer.Form.NFKD)

        // Stage 2: Zero-Width, Invisible & Control Character Stripping
        val stripped = ZERO_WIDTH_REGEX.replace(nfkd, "")

        // Stage 3: Confusable Homoglyph Translation
        val deHomoglyph = HomoglyphMapper.mapHomoglyphs(stripped)

        // Stage 4: Leetspeak & Acronym Unfolding
        val deLeet = LeetspeakMapper.normalizeLeetspeak(deHomoglyph)

        // Stage 5: Lowercase and Whitespace Compaction
        val lowercased = deLeet.lowercase()
        return WHITESPACE_REGEX.replace(lowercased, " ").trim()
    }

    /**
     * Normalizes text and strips punctuation, preserving alphanumeric, whitespace, and Devanagari characters.
     */
    fun normalizeAndStripPunctuation(input: String): String {
        val normalized = normalize(input)
        val sb = StringBuilder(normalized.length)
        for (c in normalized) {
            if (c.isLetterOrDigit() || c.isWhitespace() || c in '\u0900'..'\u097F') {
                sb.append(c)
            } else {
                sb.append(' ')
            }
        }
        return WHITESPACE_REGEX.replace(sb.toString(), " ").trim()
    }
}
