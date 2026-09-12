package com.saathi.engine

/**
 * Normalizes Leetspeak substitutions and delimiter-injected acronyms (e.g. '0TP', 'K.Y.C', 'P@ssw0rd').
 */
object LeetspeakMapper {

    private val LEET_SYMBOL_MAP = mapOf(
        '@' to 'a',
        '$' to 's'
    )

    // Dot-separated single letters: K.Y.C, O.T.P, S.B.I, etc.
    private val DOTTED_ACRONYM_REGEX = Regex("""\b([A-Za-z])(?:\.([A-Za-z]))+(?:\.)?\b""")

    /**
     * Unfolds leetspeak character substitutions and punctuation-padded acronyms.
     */
    fun normalizeLeetspeak(input: CharSequence): String {
        if (input.isEmpty()) return ""

        // 1. Collapse dotted acronyms (e.g., K.Y.C -> KYC, O.T.P -> OTP)
        var text = DOTTED_ACRONYM_REGEX.replace(input) { matchResult ->
            matchResult.value.replace(".", "")
        }

        // 2. Character-level leetspeak replacement
        val sb = StringBuilder(text.length)
        for (i in text.indices) {
            val c = text[i]
            when {
                LEET_SYMBOL_MAP.containsKey(c) -> {
                    sb.append(LEET_SYMBOL_MAP[c])
                }
                c == '!' -> {
                    // Only substitute ! with 'i' if surrounded by ASCII letters (e.g. w!n -> win)
                    val prevIsAscii = i > 0 && ((text[i - 1] in 'a'..'z') || (text[i - 1] in 'A'..'Z'))
                    val nextIsAscii = i < text.length - 1 && ((text[i + 1] in 'a'..'z') || (text[i + 1] in 'A'..'Z'))
                    if (prevIsAscii && nextIsAscii) {
                        sb.append('i')
                    } else {
                        sb.append(c)
                    }
                }
                c == '0' -> {
                    // Replace 0 with 'o' if adjacent to an alphabetical character or in short word
                    val prevIsLetter = i > 0 && text[i - 1].isLetter()
                    val nextIsLetter = i < text.length - 1 && text[i + 1].isLetter()
                    if (prevIsLetter || nextIsLetter) {
                        sb.append('o')
                    } else {
                        sb.append(c)
                    }
                }
                c == '3' -> {
                    val prevIsLetter = i > 0 && text[i - 1].isLetter()
                    val nextIsLetter = i < text.length - 1 && text[i + 1].isLetter()
                    if (prevIsLetter || nextIsLetter) {
                        sb.append('e')
                    } else {
                        sb.append(c)
                    }
                }
                c == '1' -> {
                    val prevIsLetter = i > 0 && text[i - 1].isLetter()
                    val nextIsLetter = i < text.length - 1 && text[i + 1].isLetter()
                    if (prevIsLetter || nextIsLetter) {
                        sb.append('i')
                    } else {
                        sb.append(c)
                    }
                }
                c == '5' -> {
                    val prevIsLetter = i > 0 && text[i - 1].isLetter()
                    val nextIsLetter = i < text.length - 1 && text[i + 1].isLetter()
                    if (prevIsLetter || nextIsLetter) {
                        sb.append('s')
                    } else {
                        sb.append(c)
                    }
                }
                c == '8' -> {
                    val prevIsLetter = i > 0 && text[i - 1].isLetter()
                    val nextIsLetter = i < text.length - 1 && text[i + 1].isLetter()
                    if (prevIsLetter || nextIsLetter) {
                        sb.append('b')
                    } else {
                        sb.append(c)
                    }
                }
                else -> sb.append(c)
            }
        }

        return sb.toString()
    }
}
