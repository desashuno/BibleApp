package org.biblestudio.features.exegetical_guide.domain.repositories

import org.biblestudio.features.exegetical_guide.domain.entities.CommentaryEntry
import org.biblestudio.features.resource_library.domain.entities.Resource

/**
 * Access to commentary-typed resources and their per-verse entries.
 */
interface CommentaryRepository {

    /** Returns all commentary resources. */
    suspend fun getCommentaries(): Result<List<Resource>>

    /** Returns commentary entries for a specific resource and verse. */
    suspend fun getEntriesForVerse(resourceId: String, globalVerseId: Long): Result<List<CommentaryEntry>>

    /** Returns commentary entries from ALL commentary resources for a verse. */
    suspend fun getAllEntriesForVerse(globalVerseId: Long): Result<List<CommentaryEntry>>

    /** Full-text search across commentary entries. */
    suspend fun search(query: String, maxResults: Long = 100): Result<List<CommentaryEntry>>
}
