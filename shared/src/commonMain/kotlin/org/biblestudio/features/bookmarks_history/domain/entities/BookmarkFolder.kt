package org.biblestudio.features.bookmarks_history.domain.entities

/**
 * Folder for organising bookmarks in a tree hierarchy.
 *
 * @param uuid Unique identifier (UUID v4).
 * @param name Display name of the folder.
 * @param parentId UUID of the parent folder, or null for root-level.
 * @param sortOrder Display ordering within its parent.
 * @param createdAt ISO 8601 timestamp.
 * @param updatedAt ISO 8601 timestamp.
 * @param deviceId Identifier of the device that last modified this folder.
 */
data class BookmarkFolder(
    val uuid: String,
    val name: String,
    val parentId: String?,
    val sortOrder: Long,
    val createdAt: String,
    val updatedAt: String,
    val deviceId: String
)
