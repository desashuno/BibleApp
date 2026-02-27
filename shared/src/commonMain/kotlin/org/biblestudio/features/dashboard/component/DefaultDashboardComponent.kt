package org.biblestudio.features.dashboard.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.features.bookmarks_history.domain.repositories.BookmarkRepository
import org.biblestudio.features.bookmarks_history.domain.repositories.HistoryRepository
import org.biblestudio.features.highlights.domain.repositories.HighlightRepository
import org.biblestudio.features.note_editor.domain.repositories.NoteRepository
import org.biblestudio.features.reading_plans.domain.repositories.ReadingPlanRepository
import org.biblestudio.features.sermon_editor.domain.repositories.SermonRepository

/**
 * Default [DashboardComponent] aggregating counts from multiple repositories.
 */
class DefaultDashboardComponent(
    componentContext: ComponentContext,
    private val noteRepository: NoteRepository,
    private val highlightRepository: HighlightRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val sermonRepository: SermonRepository,
    private val readingPlanRepository: ReadingPlanRepository,
    private val historyRepository: HistoryRepository
) : DashboardComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(DashboardState())
    override val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        refresh()
    }

    @Suppress("TooGenericExceptionCaught")
    override fun refresh() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val notes = noteRepository.getAllNotes(STAT_LIMIT, 0).getOrDefault(emptyList()).size
                val highlights = highlightRepository.getAll().getOrDefault(emptyList()).size
                val bookmarks = bookmarkRepository.getAll().getOrDefault(emptyList()).size
                val sermons = sermonRepository.getAll().getOrDefault(emptyList()).size
                val plans = readingPlanRepository.getPlans().getOrDefault(emptyList()).size
                val history = historyRepository.getHistory(RECENT_HISTORY_LIMIT.toLong())
                    .getOrDefault(emptyList())
                    .map { "Verse ${it.globalVerseId}" }

                _state.update {
                    it.copy(
                        totalNotes = notes,
                        totalHighlights = highlights,
                        totalBookmarks = bookmarks,
                        totalSermons = sermons,
                        activePlans = plans,
                        recentHistory = history,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Napier.e("Dashboard refresh failed", e)
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        private const val RECENT_HISTORY_LIMIT = 10
        private const val STAT_LIMIT = 10_000L
    }
}
