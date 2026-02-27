package org.biblestudio.features.exegetical_guide.domain.entities

/**
 * A commentary entry for a specific verse, from a commentary resource.
 *
 * @param id Auto-generated database ID.
 * @param resourceId FK to the parent commentary resource.
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param content Commentary content (HTML or plain text).
 * @param sortOrder Display ordering.
 */
data class CommentaryEntry(
    val id: Long,
    val resourceId: String,
    val globalVerseId: Long,
    val content: String,
    val sortOrder: Long
)
