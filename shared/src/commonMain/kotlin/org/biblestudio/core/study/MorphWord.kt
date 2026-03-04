package org.biblestudio.core.study

/**
 * Rich morphological data for a single word in a verse.
 *
 * @param id Auto-generated database ID.
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param wordPosition 1-based position of the word within the verse.
 * @param surfaceForm Original-language surface form.
 * @param lemma Dictionary form (lemma).
 * @param strongsNumber Strong's reference (e.g. "H1254", "G3056").
 * @param parsingCode Morphological parsing code (e.g. "V-QAL-3MS").
 * @param gloss Short English translation.
 */
data class MorphWord(
    val id: Long,
    val globalVerseId: Long,
    val wordPosition: Long,
    val surfaceForm: String,
    val lemma: String,
    val strongsNumber: String,
    val parsingCode: String,
    val gloss: String
)
