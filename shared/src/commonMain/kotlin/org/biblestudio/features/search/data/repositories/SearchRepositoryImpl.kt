package org.biblestudio.features.search.data.repositories

import org.biblestudio.core.util.searchLexiconWithFallback
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.bible_reader.data.mappers.toVerse
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.note_editor.data.mappers.toNote
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.resource_library.data.mappers.toResourceEntry
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry
import org.biblestudio.features.search.data.mappers.toSearchHistoryEntry
import org.biblestudio.features.search.domain.entities.SearchHistoryEntry
import org.biblestudio.features.search.domain.repositories.SearchRepository
import org.biblestudio.core.study.LexiconEntry

internal class SearchRepositoryImpl(
    private val database: BibleStudioDatabase
) : SearchRepository {

    override suspend fun searchVerses(query: String, maxResults: Long): Result<List<Verse>> = runCatching {
        database.bibleQueries
            .searchVerses(query, maxResults)
            .executeAsList()
            .map { it.toVerse() }
    }

    override suspend fun searchVersesFiltered(
        query: String,
        testament: String?,
        bookRangeStart: Int?,
        bookRangeEnd: Int?,
        maxResults: Long
    ): Result<List<Verse>> = runCatching {
        // Start with full FTS5 results, then apply in-memory filters.
        // A production implementation would push these into SQL, but the current
        // SQLDelight schema does not expose a parameterized filtered-search query.
        val allResults = database.bibleQueries
            .searchVerses(query, maxResults)
            .executeAsList()
            .map { it.toVerse() }

        allResults.filter { verse ->
            val bookNum = verse.globalVerseId / BOOK_MULTIPLIER
            val passesTestament = when {
                testament == null -> true
                testament.equals("OT", ignoreCase = true) -> bookNum <= OT_MAX_BOOK
                testament.equals("NT", ignoreCase = true) -> bookNum > OT_MAX_BOOK
                else -> true
            }
            val passesRange = when {
                bookRangeStart != null && bookRangeEnd != null ->
                    bookNum in bookRangeStart..bookRangeEnd
                bookRangeStart != null -> bookNum >= bookRangeStart
                bookRangeEnd != null -> bookNum <= bookRangeEnd
                else -> true
            }
            passesTestament && passesRange
        }
    }

    override suspend fun searchNotes(query: String, maxResults: Long): Result<List<Note>> = runCatching {
        database.searchQueries
            .searchNotes(query, maxResults)
            .executeAsList()
            .map { it.toNote() }
    }

    override suspend fun searchResources(query: String, maxResults: Long): Result<List<ResourceEntry>> = runCatching {
        database.resourceQueries
            .searchResources(query, maxResults)
            .executeAsList()
            .map { it.toResourceEntry() }
    }

    override suspend fun searchLexicon(query: String, maxResults: Long): Result<List<LexiconEntry>> = runCatching {
        searchLexiconWithFallback(database, query, maxResults)
    }

    override suspend fun getRecentSearches(limit: Long): Result<List<SearchHistoryEntry>> = runCatching {
        database.searchQueries
            .recentSearches(limit)
            .executeAsList()
            .map { it.toSearchHistoryEntry() }
    }

    override suspend fun recordSearch(entry: SearchHistoryEntry): Result<Unit> = runCatching {
        database.searchQueries.insertSearch(
            query = entry.query,
            scope = entry.scope,
            resultCount = entry.resultCount,
            createdAt = entry.createdAt
        )
    }

    override suspend fun clearHistory(): Result<Unit> = runCatching {
        database.searchQueries.clearSearchHistory()
    }

    companion object {
        /** Divisor to extract the book number from a BBCCCVVV global verse ID. */
        private const val BOOK_MULTIPLIER = 1_000_000L

        /** Last book number in the Old Testament (Malachi = 39). */
        private const val OT_MAX_BOOK = 39

    }
}
