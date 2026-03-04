package org.biblestudio.features.search.domain.repositories

import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry
import org.biblestudio.features.search.domain.entities.SearchHistoryEntry
import org.biblestudio.core.study.LexiconEntry

/**
 * Full-text search across all content types + search history management.
 */
interface SearchRepository {

    /** FTS5 search across Bible verse text. */
    suspend fun searchVerses(query: String, maxResults: Long = 100): Result<List<Verse>>

    /**
     * FTS5 search across Bible verse text with book-range and testament filters.
     *
     * @param testament Optional "OT" or "NT" to limit search scope.
     * @param bookRangeStart If non-null, the minimum book number (1-based) to include.
     * @param bookRangeEnd If non-null, the maximum book number (1-based) to include.
     */
    suspend fun searchVersesFiltered(
        query: String,
        testament: String? = null,
        bookRangeStart: Int? = null,
        bookRangeEnd: Int? = null,
        maxResults: Long = 100
    ): Result<List<Verse>>

    /** FTS5 search across user notes. */
    suspend fun searchNotes(query: String, maxResults: Long = 100): Result<List<Note>>

    /** FTS5 search across resource entries (commentaries, dictionaries). */
    suspend fun searchResources(query: String, maxResults: Long = 100): Result<List<ResourceEntry>>

    /** FTS5 search across lexicon definitions. */
    suspend fun searchLexicon(query: String, maxResults: Long = 100): Result<List<LexiconEntry>>

    /** Returns recent search history entries. */
    suspend fun getRecentSearches(limit: Long = 20): Result<List<SearchHistoryEntry>>

    /** Records a search query in the history. */
    suspend fun recordSearch(entry: SearchHistoryEntry): Result<Unit>

    /** Clears all search history. */
    suspend fun clearHistory(): Result<Unit>
}
