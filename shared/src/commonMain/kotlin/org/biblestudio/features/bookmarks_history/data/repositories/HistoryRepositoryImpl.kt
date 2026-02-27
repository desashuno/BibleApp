package org.biblestudio.features.bookmarks_history.data.repositories

import kotlinx.datetime.Clock
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.bookmarks_history.data.mappers.toHistoryEntry
import org.biblestudio.features.bookmarks_history.domain.entities.HistoryEntry
import org.biblestudio.features.bookmarks_history.domain.repositories.HistoryRepository

internal class HistoryRepositoryImpl(
    private val database: BibleStudioDatabase
) : HistoryRepository {

    override suspend fun getHistory(limit: Long): Result<List<HistoryEntry>> = runCatching {
        database.annotationQueries
            .recentHistory(limit)
            .executeAsList()
            .map { it.toHistoryEntry() }
    }

    override suspend fun addEntry(globalVerseId: Long): Result<Unit> = runCatching {
        val now = Clock.System.now().toString()
        database.annotationQueries.insertHistoryEntry(
            globalVerseId = globalVerseId,
            visitedAt = now
        )
        // Auto-prune if above cap
        val count = database.annotationQueries.historyCount().executeAsOne()
        if (count > HistoryRepository.HISTORY_CAP) {
            database.annotationQueries.pruneHistory(keepCount = HistoryRepository.HISTORY_CAP)
        }
    }

    override suspend fun prune(keepCount: Long): Result<Unit> = runCatching {
        database.annotationQueries.pruneHistory(keepCount = keepCount)
    }

    override suspend fun clear(): Result<Unit> = runCatching {
        database.annotationQueries.clearHistory()
    }
}
