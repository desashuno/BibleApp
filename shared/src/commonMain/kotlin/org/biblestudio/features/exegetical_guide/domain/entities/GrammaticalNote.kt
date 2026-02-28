package org.biblestudio.features.exegetical_guide.domain.entities

/**
 * A grammatical/syntactic annotation for a word or phrase in a verse.
 *
 * @param id Unique identifier.
 * @param word The word or phrase being annotated.
 * @param partOfSpeech Grammatical part of speech (noun, verb, etc.).
 * @param parsing Full morphological parsing code.
 * @param syntacticRole Syntactic role in the clause (subject, object, etc.).
 * @param notes Additional grammatical commentary.
 */
data class GrammaticalNote(
    val id: Long,
    val word: String,
    val partOfSpeech: String,
    val parsing: String,
    val syntacticRole: String,
    val notes: String = ""
)
