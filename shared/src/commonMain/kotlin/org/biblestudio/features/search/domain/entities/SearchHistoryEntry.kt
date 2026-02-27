package org.biblestudio.features.search.domain.entities

/**
 * A recorded entry from the search history.
 *
 * @param id Auto-generated database ID.
 * @param query The search query string.
 * @param scope Search scope (e.g., "verses", "notes", "resources", "all").
 * @param resultCount Number of results returned.
 * @param createdAt ISO 8601 timestamp of when the search was performed.
 */
data class SearchHistoryEntry(
    val id: Long,
    val query: String,
    val scope: String,
    val resultCount: Long,
    val createdAt: String
)
