package org.biblestudio.features.resource_library.domain.entities

/**
 * A single content entry within a [Resource], typically scoped to a verse.
 *
 * @param id Auto-generated database ID.
 * @param resourceId FK to the parent [Resource].
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param content Entry content (HTML or plain text).
 * @param sortOrder Display ordering for entries on the same verse.
 */
data class ResourceEntry(
    val id: Long,
    val resourceId: String,
    val globalVerseId: Long,
    val content: String,
    val sortOrder: Long
)
