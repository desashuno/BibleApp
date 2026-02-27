package org.biblestudio.features.word_study.domain.entities

/**
 * A Strong's concordance lexicon entry.
 *
 * @param strongsNumber Strong's reference (e.g., "H1234", "G5678").
 * @param originalWord The original Hebrew/Greek word.
 * @param transliteration Romanized transliteration.
 * @param definition Short definition text.
 * @param usageNotes Extended usage and context notes.
 */
data class LexiconEntry(
    val strongsNumber: String,
    val originalWord: String,
    val transliteration: String,
    val definition: String,
    val usageNotes: String?
)
