package org.biblestudio.features.word_study.domain.entities

/**
 * A dictionary entry for a specific verse, from a dictionary resource.
 *
 * @param id Auto-generated database ID.
 * @param resourceId FK to the parent dictionary resource.
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param content Dictionary content (HTML or plain text).
 * @param sortOrder Display ordering.
 */
data class DictionaryEntry(
    val id: Long,
    val resourceId: String,
    val globalVerseId: Long,
    val content: String,
    val sortOrder: Long
)
