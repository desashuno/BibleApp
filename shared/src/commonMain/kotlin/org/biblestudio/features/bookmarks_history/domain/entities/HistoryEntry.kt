package org.biblestudio.features.bookmarks_history.domain.entities

/**
 * A single navigation history entry recording when a verse was visited.
 *
 * @param id Auto-generated database ID.
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param visitedAt ISO 8601 timestamp of the visit.
 */
data class HistoryEntry(
    val id: Long,
    val globalVerseId: Long,
    val visitedAt: String
)
