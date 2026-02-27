package org.biblestudio.features.morphology_interlinear.domain.entities

/**
 * Morphological analysis data for a single word in a verse.
 *
 * @param id Auto-generated database ID.
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param wordPosition 0-based position of the word within the verse.
 * @param strongsNumber Strong's reference linking to the lexicon.
 * @param parsingCode Morphological parsing code (e.g., "V-PAI-3S").
 * @param originalWord The original language word (from lexicon join).
 * @param transliteration Romanized form (from lexicon join).
 * @param definition Short definition (from lexicon join).
 */
data class MorphologyData(
    val id: Long,
    val globalVerseId: Long,
    val wordPosition: Long,
    val strongsNumber: String,
    val parsingCode: String,
    val originalWord: String?,
    val transliteration: String?,
    val definition: String?
)
