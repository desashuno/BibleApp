package org.biblestudio.features.dashboard.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.bible_reader.domain.repositories.BibleRepository
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
    private val historyRepository: HistoryRepository,
    private val verseBus: VerseBus,
    private val bibleRepository: BibleRepository
) : DashboardComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(DashboardState())
    override val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        refresh()
        observeVerseBus()
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { refresh() }
        }
    }

    override fun onContinueReading(globalVerseId: Long) {
        scope.launch {
            // BBCCCVVV max is 66_167_176 — safely within Int range
            require(globalVerseId in 1_001_001..66_999_999) {
                "Invalid global verse ID: $globalVerseId"
            }
            verseBus.publish(LinkEvent.VerseSelected(globalVerseId.toInt()))
        }
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
                val dailyVerse = loadDailyVerse()

                // Continue reading — most recent history entry
                val lastHistory = historyRepository.getHistory(1)
                    .getOrDefault(emptyList())
                    .firstOrNull()
                val continueReading = lastHistory?.let { h ->
                    ContinueReading(
                        globalVerseId = h.globalVerseId,
                        reference = "Verse ${h.globalVerseId}",
                        timestamp = h.visitedAt
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

    @Suppress("MagicNumber")
    private suspend fun loadDailyVerse(): DailyVerse? {
        val dayHash = (Clock.System.now().toEpochMilliseconds() / 86_400_000).toInt()
        val selectedId = WELL_KNOWN_VERSE_IDS[dayHash.mod(WELL_KNOWN_VERSE_IDS.size)]
        val verse = bibleRepository.getVerseByGlobalId(selectedId).getOrNull()
        return verse?.let {
            DailyVerse(
                globalVerseId = selectedId,
                text = it.text,
                reference = decodeReference(selectedId)
            )
        }
    }

    companion object {
        private const val RECENT_HISTORY_LIMIT = 10
        private const val STAT_LIMIT = 10_000L
        private const val RECENT_NOTES_LIMIT = 3L
        private const val PREVIEW_LENGTH = 80

        /** Well-known verse global IDs (BBCCCVVV) used for daily verse rotation. */
        private val WELL_KNOWN_VERSE_IDS = listOf(
            43003016L, // John 3:16
            19023001L, // Psalm 23:1
            20003005L, // Proverbs 3:5
            45008028L, // Romans 8:28
            23041010L, // Isaiah 41:10
            50004013L, // Philippians 4:13
            24029011L  // Jeremiah 29:11
        )

        /** Book names indexed by book number (1-66). */
        private val BOOK_NAMES = listOf(
            "", // 0 placeholder
            "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy",
            "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel",
            "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
            "Nehemiah", "Esther", "Job", "Psalms", "Proverbs",
            "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations",
            "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
            "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk",
            "Zephaniah", "Haggai", "Zechariah", "Malachi",
            "Matthew", "Mark", "Luke", "John", "Acts",
            "Romans", "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians",
            "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy",
            "2 Timothy", "Titus", "Philemon", "Hebrews", "James",
            "1 Peter", "2 Peter", "1 John", "2 John", "3 John",
            "Jude", "Revelation"
        )

        /** Decodes a BBCCCVVV global verse ID to a human-readable reference. */
        @Suppress("MagicNumber")
        internal fun decodeReference(globalVerseId: Long): String {
            val book = (globalVerseId / 1_000_000).toInt()
            val chapter = ((globalVerseId % 1_000_000) / 1_000).toInt()
            val verse = (globalVerseId % 1_000).toInt()
            val bookName = if (book in 1..66) BOOK_NAMES[book] else "Book $book"
            return "$bookName $chapter:$verse"
        }
    }
}
