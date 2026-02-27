package org.biblestudio.features.morphology_interlinear.domain

/**
 * Decodes morphological parsing codes into human-readable descriptions.
 *
 * Supports both Hebrew (e.g. `V-QAL-3MS`) and Greek (e.g. `V-AAI-3S`)
 * morphology code formats. Unknown segments are returned as-is.
 */
class ParsingDecoder {

    /**
     * Decodes a full parsing code string into a human-readable description.
     *
     * Example: `"V-QAL-3MS"` → `"Verb, Qal, 3rd Person, Masculine, Singular"`
     */
    fun decode(parsingCode: String): String {
        if (parsingCode.isBlank()) return ""
        val segments = parsingCode.split("-")
        return segments.mapNotNull { segment ->
            decodeSegment(segment.trim())
        }.joinToString(", ")
    }

    @Suppress("ReturnCount")
    private fun decodeSegment(segment: String): String? {
        if (segment.isEmpty()) return null

        // Try exact match first
        LOOKUP[segment.uppercase()]?.let { return it }

        // Try compound person-number-gender codes like "3MS", "2FP", "1CS"
        if (segment.length >= 2 && segment[0].isDigit()) {
            return decodePersonNumberGender(segment.uppercase())
        }

        // Try multi-character composites
        return decodeComposite(segment.uppercase()) ?: segment
    }

    private fun decodePersonNumberGender(code: String): String {
        val parts = mutableListOf<String>()

        val person = PERSON[code.substring(0, 1)]
        if (person != null) parts.add(person)

        for (i in 1 until code.length) {
            val ch = code[i].toString()
            val decoded = GENDER[ch] ?: NUMBER[ch]
            if (decoded != null) parts.add(decoded)
        }

        return if (parts.isNotEmpty()) parts.joinToString(", ") else code
    }

    private fun decodeComposite(code: String): String? {
        // Try to decode as a sequence of single-char codes (e.g. "MPC" → "Masculine, Plural, Construct")
        val parts = code.mapNotNull { ch ->
            val s = ch.toString()
            GENDER[s] ?: NUMBER[s] ?: STATE[s] ?: CASE[s]
        }
        return if (parts.size == code.length && parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            null
        }
    }

    companion object {

        /** Part-of-speech tags */
        private val LOOKUP = mapOf(
            // Parts of speech
            "V" to "Verb",
            "N" to "Noun",
            "ADJ" to "Adjective",
            "ADV" to "Adverb",
            "PREP" to "Preposition",
            "CONJ" to "Conjunction",
            "PART" to "Particle",
            "PRON" to "Pronoun",
            "ART" to "Article",
            "INTJ" to "Interjection",
            "DET" to "Determiner",
            "NUM" to "Numeral",
            "ACC" to "Accusative particle",
            "NEG" to "Negative particle",

            // Hebrew verb stems
            "QAL" to "Qal",
            "NIPHAL" to "Niphal",
            "PIEL" to "Piel",
            "PUAL" to "Pual",
            "HIPHIL" to "Hiphil",
            "HOPHAL" to "Hophal",
            "HITHPAEL" to "Hithpael",

            // Greek tenses
            "AAI" to "Aorist Active Indicative",
            "PAI" to "Present Active Indicative",
            "PPI" to "Present Passive Indicative",
            "PMI" to "Present Middle Indicative",
            "FAI" to "Future Active Indicative",
            "IAI" to "Imperfect Active Indicative",
            "RAI" to "Perfect Active Indicative",
            "RPI" to "Perfect Passive Indicative",
            "API" to "Aorist Passive Indicative",
            "AAS" to "Aorist Active Subjunctive",
            "PAS" to "Present Active Subjunctive",
            "AAP" to "Aorist Active Participle",
            "PAP" to "Present Active Participle",
            "PPP" to "Present Passive Participle",
            "AAM" to "Aorist Active Imperative",
            "PAM" to "Present Active Imperative",
            "AAN" to "Aorist Active Infinitive",
            "PAN" to "Present Active Infinitive",

            // Hebrew conjugations
            "PERF" to "Perfect",
            "IMPF" to "Imperfect",
            "IMP" to "Imperative",
            "INF" to "Infinitive",
            "PTCP" to "Participle",
            "WCONS" to "Waw-consecutive"
        )

        private val PERSON = mapOf(
            "1" to "1st Person",
            "2" to "2nd Person",
            "3" to "3rd Person"
        )

        private val GENDER = mapOf(
            "M" to "Masculine",
            "F" to "Feminine",
            "C" to "Common",
            "N" to "Neuter"
        )

        private val NUMBER = mapOf(
            "S" to "Singular",
            "P" to "Plural",
            "D" to "Dual"
        )

        private val STATE = mapOf(
            "A" to "Absolute",
            "C" to "Construct"
        )

        private val CASE = mapOf(
            "N" to "Nominative",
            "G" to "Genitive",
            "D" to "Dative",
            "A" to "Accusative"
        )
    }
}
