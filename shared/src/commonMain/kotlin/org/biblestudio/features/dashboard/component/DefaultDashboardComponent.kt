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

                // Daily verse — deterministic pseudo-random based on day-of-year
                val dayHash = (System.currentTimeMillis() / 86_400_000).toInt()
                val dailyVerse = DAILY_VERSES[dayHash % DAILY_VERSES.size]

                // Continue reading — most recent history entry
                val lastHistory = historyRepository.getHistory(1)
                    .getOrDefault(emptyList())
                    .firstOrNull()
                val continueReading = lastHistory?.let { h ->
                    ContinueReading(
                        globalVerseId = h.globalVerseId,
                        reference = "Verse ${h.globalVerseId}",
                        timestamp = h.createdAt
                    )
                }

                // Reading plan progress — first active plan
                val planList = readingPlanRepository.getPlans().getOrDefault(emptyList())
                val planWidget = if (planList.isNotEmpty()) {
                    val p = planList.first()
                    val prog = readingPlanRepository.getProgress(p.uuid).getOrDefault(emptyList())
                    val completed = prog.count { it.completed }
                    val total = p.durationDays.toInt()
                    val pct = if (total > 0) completed.toFloat() / total else 0f
                    val days = prog.filter { it.completed }.map { it.day.toInt() }.sorted()
                    var streak = if (days.isEmpty()) 0 else 1
                    for (i in days.lastIndex downTo 1) {
                        if (days[i] - days[i - 1] == 1) streak++ else break
                    }
                    ReadingPlanWidget(
                        planTitle = p.title,
                        progressPercent = pct,
                        currentDay = if (completed < total) completed + 1 else total,
                        totalDays = total,
                        streak = streak
                    )
                } else null

                // Recent notes — up to 3
                val recentNotes = noteRepository.getAllNotes(RECENT_NOTES_LIMIT, 0)
                    .getOrDefault(emptyList())
                    .map { n ->
                        RecentNote(
                            uuid = n.uuid,
                            title = n.title,
                            preview = n.content.take(PREVIEW_LENGTH)
                        )
                    }

                _state.update {
                    it.copy(
                        totalNotes = notes,
                        totalHighlights = highlights,
                        totalBookmarks = bookmarks,
                        totalSermons = sermons,
                        activePlans = plans,
                        recentHistory = history,
                        dailyVerse = dailyVerse,
                        continueReading = continueReading,
                        readingPlanProgress = planWidget,
                        recentNotes = recentNotes,
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
        private const val RECENT_NOTES_LIMIT = 3L
        private const val PREVIEW_LENGTH = 80

        private val DAILY_VERSES = listOf(
            DailyVerse(43003016, "For God so loved the world, that he gave his only begotten Son.", "John 3:16"),
            DailyVerse(19023001, "The LORD is my shepherd; I shall not want.", "Psalm 23:1"),
            DailyVerse(20003005, "Trust in the LORD with all thine heart.", "Proverbs 3:5"),
            DailyVerse(45008028, "All things work together for good to them that love God.", "Romans 8:28"),
            DailyVerse(23041010, "Fear thou not; for I am with thee.", "Isaiah 41:10"),
            DailyVerse(50004013, "I can do all things through Christ which strengtheneth me.", "Philippians 4:13"),
            DailyVerse(24029011, "For I know the thoughts that I think toward you.", "Jeremiah 29:11")
        )
    }
}
