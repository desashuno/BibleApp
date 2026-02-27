package org.biblestudio.features.bookmarks_history.domain.entities

/**
 * Represents a user bookmark on a verse, optionally organized into folders.
 *
 * @param uuid Unique identifier (UUID v4).
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param label User-provided label or auto-generated reference text.
 * @param folderId Optional folder grouping identifier.
 * @param sortOrder Display ordering within its folder.
 * @param createdAt ISO 8601 timestamp.
 * @param updatedAt ISO 8601 timestamp.
 * @param deviceId Identifier of the device that last modified this bookmark.
 */
data class Bookmark(
    val uuid: String,
    val globalVerseId: Long,
    val label: String,
    val folderId: String?,
    val sortOrder: Long,
    val createdAt: String,
    val updatedAt: String,
    val deviceId: String
)
