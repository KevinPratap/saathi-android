package com.saathi.engine

/**
 * Maps Cyrillic, Greek, and other cross-script confusable homoglyphs to canonical Latin characters.
 * Defeats adversarial attacks attempting to bypass word filters via visual spoofing.
 */
object HomoglyphMapper {

    private val HOMOGLYPH_MAP: Map<Char, Char> = buildMap {
        // Cyrillic to Latin (Uppercase)
        put('\u0410', 'A') // А -> A
        put('\u0412', 'B') // В -> B
        put('\u0415', 'E') // Е -> E
        put('\u041A', 'K') // К -> K
        put('\u041C', 'M') // М -> M
        put('\u041D', 'H') // Н -> H
        put('\u041E', 'O') // О -> O
        put('\u0420', 'P') // Р -> P
        put('\u0421', 'C') // С -> C
        put('\u0422', 'T') // Т -> T
        put('\u0423', 'Y') // У -> Y
        put('\u0425', 'X') // Х -> X
        put('\u0406', 'I') // І -> I
        put('\u0408', 'J') // Ј -> J
        put('\u0405', 'S') // Ѕ -> S

        // Cyrillic to Latin (Lowercase)
        put('\u0430', 'a') // а -> a
        put('\u0435', 'e') // е -> e
        put('\u043A', 'k') // к -> k
        put('\u043E', 'o') // о -> o
        put('\u0440', 'p') // р -> p
        put('\u0441', 'c') // с -> c
        put('\u0442', 't') // т -> t
        put('\u0443', 'y') // у -> y
        put('\u0445', 'x') // х -> x
        put('\u0456', 'i') // і -> i
        put('\u0458', 'j') // ј -> j
        put('\u0455', 's') // ѕ -> s

        // Greek to Latin (Uppercase)
        put('\u0391', 'A') // Α -> A
        put('\u0392', 'B') // Β -> B
        put('\u0395', 'E') // Ε -> E
        put('\u0396', 'Z') // Ζ -> Z
        put('\u0397', 'H') // Η -> H
        put('\u0399', 'I') // Ι -> I
        put('\u039A', 'K') // Κ -> K
        put('\u039C', 'M') // Μ -> M
        put('\u039D', 'N') // Ν -> N
        put('\u039F', 'O') // Ο -> O
        put('\u03A1', 'P') // Ρ -> P
        put('\u03A4', 'T') // Τ -> T
        put('\u03A5', 'Y') // Υ -> Y
        put('\u03A7', 'X') // Χ -> X

        // Greek to Latin (Lowercase)
        put('\u03B1', 'a') // α -> a
        put('\u03B2', 'b') // β -> b
        put('\u03B5', 'e') // ε -> e
        put('\u03B9', 'i') // ι -> i
        put('\u03BA', 'k') // κ -> k
        put('\u03BF', 'o') // ο -> o
        put('\u03C1', 'p') // ρ -> p
        put('\u03C4', 't') // τ -> t
        put('\u03C5', 'u') // υ -> u
        put('\u03C7', 'x') // χ -> x

        // Devanagari Digits to Latin Digits (०-९ -> 0-9)
        put('\u0966', '0')
        put('\u0967', '1')
        put('\u0968', '2')
        put('\u0969', '3')
        put('\u096A', '4')
        put('\u096B', '5')
        put('\u096C', '6')
        put('\u096D', '7')
        put('\u096E', '8')
        put('\u096F', '9')
    }

    /**
     * Translates homoglyphs in the input text to standard Latin and Arabic digits.
     */
    fun mapHomoglyphs(input: CharSequence): String {
        if (input.isEmpty()) return ""
        val sb = java.lang.StringBuilder(input.length)
        for (i in 0 until input.length) {
            val c = input[i]
            sb.append(HOMOGLYPH_MAP[c] ?: c)
        }
        return sb.toString()
    }
}
