package org.biblestudio.features.word_study.domain.entities

/**
 * A topic-based dictionary entry identified by headword.
 *
 * @param id Auto-generated database ID.
 * @param resourceId FK to the parent dictionary resource.
 * @param headword The dictionary term / topic heading.
 * @param content Dictionary article content (HTML or plain text).
 * @param relatedStrongs Comma-separated Strong's numbers, if applicable.
 * @param sortOrder Display ordering within a resource.
 * @param resourceTitle Display name of the dictionary source.
 * @param resourceAuthor Author for attribution.
 */
data class DictionaryEntry(
    val id: Long,
    val resourceId: String,
    val headword: String,
    val content: String,
    val relatedStrongs: String? = null,
    val sortOrder: Long = 0,
    val resourceTitle: String = "",
    val resourceAuthor: String = ""
)
