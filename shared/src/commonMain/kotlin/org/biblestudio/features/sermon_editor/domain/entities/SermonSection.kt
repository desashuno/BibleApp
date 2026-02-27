package org.biblestudio.features.sermon_editor.domain.entities

/**
 * A section within a [Sermon], ordered by [sortOrder].
 *
 * @param id Auto-generated database ID.
 * @param sermonId FK to the parent [Sermon].
 * @param type Section type (e.g., "introduction", "point", "illustration", "conclusion").
 * @param content Section content (rich text).
 * @param sortOrder Display ordering within the sermon.
 */
data class SermonSection(
    val id: Long,
    val sermonId: String,
    val type: String,
    val content: String,
    val sortOrder: Long
)
