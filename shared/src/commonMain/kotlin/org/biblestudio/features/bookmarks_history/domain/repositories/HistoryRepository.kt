package org.biblestudio.features.bookmarks_history.domain.repositories

import org.biblestudio.features.bookmarks_history.domain.entities.HistoryEntry

/**
 * Navigation history tracking with automatic pruning.
 */
interface HistoryRepository {

    /** Returns the most recent history entries. */
    suspend fun getHistory(limit: Long = 100): Result<List<HistoryEntry>>

    /** Records a verse visit and auto-prunes if count exceeds the cap. */
    suspend fun addEntry(globalVerseId: Long): Result<Unit>

    /** Removes old entries keeping only the most recent [keepCount]. */
    suspend fun prune(keepCount: Long = HISTORY_CAP): Result<Unit>

    /** Clears all history entries. */
    suspend fun clear(): Result<Unit>

    companion object {
        const val HISTORY_CAP = 500L
    }
}
