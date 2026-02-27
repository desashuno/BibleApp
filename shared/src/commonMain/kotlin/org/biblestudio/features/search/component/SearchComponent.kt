package org.biblestudio.features.search.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.search.domain.entities.SearchHistoryEntry

/**
 * Filters that can be applied to search queries.
 */
data class SearchFilters(
    val testament: String? = null,
    val bookRange: IntRange? = null,
    val modules: List<String> = emptyList()
)

/**
 * Tab mode for search result grouping.
 */
enum class SearchScope {
    VERSES,
    NOTES,
    RESOURCES,
    ALL
}

/**
 * Observable state for the Search pane.
 */
data class SearchState(
    val query: String = "",
    val results: List<Verse> = emptyList(),
    val scope: SearchScope = SearchScope.VERSES,
    val filters: SearchFilters = SearchFilters(),
    val resultCount: Int = 0,
    val isSearching: Boolean = false,
    val history: List<SearchHistoryEntry> = emptyList(),
    val error: String? = null
)

/**
 * Business-logic boundary for the Search pane.
 */
interface SearchComponent {

    /** The current search state observable. */
    val state: StateFlow<SearchState>

    /** Updates the search query (triggers debounced search). */
    fun onQueryChanged(query: String)

    /** Executes search immediately. */
    fun search()

    /** Sets the search scope. */
    fun setScope(scope: SearchScope)

    /** Updates search filters. */
    fun setFilters(filters: SearchFilters)

    /** Called when user taps a search result. */
    fun onResultTapped(verse: Verse)

    /** Clears search history. */
    fun clearHistory()
}
