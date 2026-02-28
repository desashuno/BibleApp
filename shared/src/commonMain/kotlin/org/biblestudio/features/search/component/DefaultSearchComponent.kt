package org.biblestudio.features.search.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.search.domain.entities.SearchHistoryEntry
import org.biblestudio.features.search.domain.repositories.SearchRepository

private const val DEBOUNCE_MS = 300L
private const val MAX_RESULTS = 100L

/**
 * Default [SearchComponent] with debounced FTS5 search and VerseBus integration.
 */
class DefaultSearchComponent(
    componentContext: ComponentContext,
    private val repository: SearchRepository,
    private val verseBus: VerseBus
) : SearchComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(SearchState())
    override val state: StateFlow<SearchState> = _state.asStateFlow()

    private var debounceJob: Job? = null

    init {
        loadHistory()
    }

    override fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
        debounceJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(results = emptyList(), resultCount = 0, isSearching = false) }
            return
        }
        debounceJob = scope.launch {
            delay(DEBOUNCE_MS)
            executeSearch(query)
        }
    }

    override fun search() {
        debounceJob?.cancel()
        val query = _state.value.query
        if (query.isBlank()) return
        scope.launch { executeSearch(query) }
    }

    override fun setScope(scope: SearchScope) {
        _state.update { it.copy(scope = scope) }
        val query = _state.value.query
        if (query.isNotBlank()) {
            this.scope.launch { executeSearch(query) }
        }
    }

    override fun setFilters(filters: SearchFilters) {
        _state.update { it.copy(filters = filters) }
    }

    override fun onResultTapped(verse: Verse) {
        verseBus.publish(LinkEvent.SearchResult(verse.globalVerseId.toInt(), _state.value.query))
    }

    override fun clearHistory() {
        scope.launch {
            repository.clearHistory()
            _state.update { it.copy(history = emptyList()) }
        }
    }

    private suspend fun executeSearch(query: String) {
        _state.update { it.copy(isSearching = true, error = null) }

        val filters = _state.value.filters
        val hasFilters = filters.testament != null || filters.bookRange != null

        val searchResult = if (hasFilters) {
            repository.searchVersesFiltered(
                query = query,
                testament = filters.testament,
                bookRangeStart = filters.bookRange?.first,
                bookRangeEnd = filters.bookRange?.last,
                maxResults = MAX_RESULTS
            )
        } else {
            repository.searchVerses(query, MAX_RESULTS)
        }

        searchResult
            .onSuccess { results ->
                _state.update {
                    it.copy(
                        results = results,
                        resultCount = results.size,
                        isSearching = false
                    )
                }
                recordSearchHistory(query, results.size)
                Napier.d("Search '$query' returned ${results.size} results")
            }
            .onFailure { e ->
                Napier.e("Search failed", e)
                _state.update {
                    it.copy(
                        isSearching = false,
                        error = "Search failed for '$query': ${e.message}"
                    )
                }
            }
    }

    @Suppress("MagicNumber")
    private suspend fun recordSearchHistory(query: String, count: Int) {
        val entry = SearchHistoryEntry(
            id = 0,
            query = query,
            scope = _state.value.scope.name.lowercase(),
            resultCount = count.toLong(),
            // DB default handles timestamp
            createdAt = ""
        )
        repository.recordSearch(entry)
    }

    private fun loadHistory() {
        scope.launch {
            repository.getRecentSearches()
                .onSuccess { entries ->
                    _state.update { it.copy(history = entries) }
                }
        }
    }
}
