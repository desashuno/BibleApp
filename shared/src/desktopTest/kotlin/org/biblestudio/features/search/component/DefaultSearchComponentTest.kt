package org.biblestudio.features.search.component

import org.biblestudio.test.testComponentContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry
import org.biblestudio.features.search.domain.entities.SearchHistoryEntry
import org.biblestudio.features.search.domain.repositories.SearchRepository
import org.biblestudio.core.study.LexiconEntry

/**
 * Tests for [DefaultSearchComponent], specifically the debounce behaviour.
 */
class DefaultSearchComponentTest {

    private val fakeRepo = object : SearchRepository {
        var searchCallCount = 0

        override suspend fun searchVerses(query: String, maxResults: Long): Result<List<Verse>> {
            searchCallCount++
            return Result.success(emptyList())
        }

        override suspend fun searchVersesFiltered(
            query: String,
            testament: String?,
            bookRangeStart: Int?,
            bookRangeEnd: Int?,
            maxResults: Long
        ): Result<List<Verse>> {
            searchCallCount++
            return Result.success(emptyList())
        }

        override suspend fun searchNotes(query: String, maxResults: Long): Result<List<Note>> =
            Result.success(emptyList())

        override suspend fun searchResources(query: String, maxResults: Long): Result<List<ResourceEntry>> =
            Result.success(emptyList())

        override suspend fun searchLexicon(query: String, maxResults: Long): Result<List<LexiconEntry>> =
            Result.success(emptyList())

        override suspend fun getRecentSearches(limit: Long): Result<List<SearchHistoryEntry>> =
            Result.success(emptyList())

        override suspend fun recordSearch(entry: SearchHistoryEntry): Result<Unit> = Result.success(Unit)

        override suspend fun clearHistory(): Result<Unit> = Result.success(Unit)
    }

    private fun createComponent(): DefaultSearchComponent {
        val context = testComponentContext()
        return DefaultSearchComponent(
            componentContext = context,
            repository = fakeRepo,
            verseBus = VerseBus()
        )
    }

    @Test
    fun queryChangeUpdatesState() {
        val component = createComponent()
        component.onQueryChanged("hello")
        assertEquals("hello", component.state.value.query)
    }

    @Test
    fun clearQueryResetsResults() {
        val component = createComponent()
        component.onQueryChanged("test")
        component.onQueryChanged("")
        assertTrue(component.state.value.results.isEmpty())
        assertEquals(0, component.state.value.resultCount)
    }

    @Test
    fun setFiltersUpdatesState() {
        val component = createComponent()
        val filters = SearchFilters(testament = "NT")
        component.setFilters(filters)
        assertEquals("NT", component.state.value.filters.testament)
    }

    @Test
    fun setScopeUpdatesState() {
        val component = createComponent()
        component.setScope(SearchScope.NOTES)
        assertEquals(SearchScope.NOTES, component.state.value.scope)
    }

    @Test
    fun debounceDoesNotSearchImmediately() {
        val component = createComponent()
        fakeRepo.searchCallCount = 0
        component.onQueryChanged("delayed")
        // Query text should be updated immediately
        assertEquals("delayed", component.state.value.query)
        // But the search should NOT have fired synchronously
        assertEquals(0, fakeRepo.searchCallCount)
    }
}
