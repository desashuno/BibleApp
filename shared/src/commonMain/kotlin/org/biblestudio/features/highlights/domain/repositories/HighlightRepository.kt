package org.biblestudio.features.highlights.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.biblestudio.features.highlights.domain.entities.Highlight

/**
 * CRUD operations for verse highlights.
 */
interface HighlightRepository {

    /** Returns highlights for a specific verse. */
    suspend fun getHighlightsForVerse(globalVerseId: Long): Result<List<Highlight>>

    /** Returns all highlights, ordered by global verse ID. */
    suspend fun getAll(): Result<List<Highlight>>

    /** Returns highlights for verses in a range (inclusive). */
    suspend fun getHighlightsForVerseRange(startVerseId: Long, endVerseId: Long): Result<List<Highlight>>

    /** Creates a new highlight. */
    suspend fun create(highlight: Highlight): Result<Unit>

    /** Updates an existing highlight. */
    suspend fun update(highlight: Highlight): Result<Unit>

    /** Soft-deletes a highlight by UUID. */
    suspend fun delete(uuid: String, deletedAt: String): Result<Unit>

    /** Reactive stream of highlights for a specific verse. */
    fun watchHighlightsForVerse(globalVerseId: Long): Flow<List<Highlight>>
}
