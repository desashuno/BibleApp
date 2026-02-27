package org.biblestudio.features.highlights.domain.entities

/**
 * Represents a verse highlight with a specific color and style.
 *
 * @param uuid Unique identifier (UUID v4).
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param colorIndex Index into the highlight color palette (0-based).
 * @param style Style type (e.g., "background", "underline").
 * @param startOffset Start character offset within the verse text (0 = start).
 * @param endOffset End character offset (-1 means whole verse).
 * @param createdAt ISO 8601 timestamp.
 * @param updatedAt ISO 8601 timestamp.
 * @param deviceId Identifier of the device that last modified this highlight.
 */
data class Highlight(
    val uuid: String,
    val globalVerseId: Long,
    val colorIndex: Long,
    val style: String,
    val startOffset: Long = 0,
    val endOffset: Long = -1,
    val createdAt: String,
    val updatedAt: String,
    val deviceId: String
)
