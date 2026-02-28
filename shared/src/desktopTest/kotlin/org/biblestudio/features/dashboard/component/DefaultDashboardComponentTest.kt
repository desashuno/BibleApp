package org.biblestudio.features.dashboard.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.entities.BookmarkFolder
import org.biblestudio.features.bookmarks_history.domain.entities.HistoryEntry
import org.biblestudio.features.bookmarks_history.domain.repositories.BookmarkRepository
import org.biblestudio.features.bookmarks_history.domain.repositories.HistoryRepository
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.features.highlights.domain.repositories.HighlightRepository
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.note_editor.domain.entities.NoteFormat
import org.biblestudio.features.note_editor.domain.repositories.NoteRepository
import org.biblestudio.features.reading_plans.domain.entities.PlanProgress
import org.biblestudio.features.reading_plans.domain.entities.ReadingPlan
import org.biblestudio.features.reading_plans.domain.repositories.ReadingPlanRepository
import org.biblestudio.features.sermon_editor.domain.entities.Sermon
import org.biblestudio.features.sermon_editor.domain.entities.SermonSection
import org.biblestudio.features.sermon_editor.domain.repositories.SermonRepository

@Suppress("TooManyFunctions")
class DefaultDashboardComponentTest {

    private val fakeNoteRepo = object : NoteRepository {
        override suspend fun getAllNotes(limit: Long, offset: Long) = Result.success(
            listOf(
                Note("n1", 1001001, "Note 1", "Content", NoteFormat.Markdown, "2024-01-01", "2024-01-01", ""),
                Note("n2", 1001002, "Note 2", "Content", NoteFormat.Markdown, "2024-01-01", "2024-01-01", "")
            )
        )
        override suspend fun getNotesForVerse(globalVerseId: Long) = Result.success(emptyList<Note>())
        override suspend fun getNoteByUuid(uuid: String) = Result.success(null as Note?)
        override suspend fun create(note: Note) = Result.success(Unit)
        override suspend fun update(note: Note) = Result.success(Unit)
        override suspend fun delete(uuid: String, deletedAt: String) = Result.success(Unit)
        override suspend fun searchNotes(query: String, maxResults: Long) = Result.success(emptyList<Note>())
        override fun watchNotesForVerse(globalVerseId: Long): Flow<List<Note>> = flowOf(emptyList())
    }

    private val fakeHighlightRepo = object : HighlightRepository {
        override suspend fun getAll() = Result.success(
            listOf(
                Highlight("h1", 1001001, 0, "background", 0, -1, "2024-01-01", "2024-01-01", "")
            )
        )
        override suspend fun getHighlightsForVerse(globalVerseId: Long) = Result.success(emptyList<Highlight>())
        override suspend fun getHighlightsForVerseRange(startVerseId: Long, endVerseId: Long) =
            Result.success(emptyList<Highlight>())
        override suspend fun create(highlight: Highlight) = Result.success(Unit)
        override suspend fun update(highlight: Highlight) = Result.success(Unit)
        override suspend fun delete(uuid: String, deletedAt: String) = Result.success(Unit)
        override fun watchHighlightsForVerse(globalVerseId: Long): Flow<List<Highlight>> = flowOf(emptyList())
    }

    private val fakeBookmarkRepo = object : BookmarkRepository {
        override suspend fun getBookmarksForVerse(globalVerseId: Long) = Result.success(emptyList<Bookmark>())
        override suspend fun getByFolder(folderId: String) = Result.success(emptyList<Bookmark>())
        override suspend fun getAll() = Result.success(
            listOf(
                Bookmark("b1", 1001001, "Label", null, 0, "2024-01-01", "2024-01-01", ""),
                Bookmark("b2", 1001002, "Label2", null, 1, "2024-01-01", "2024-01-01", ""),
                Bookmark("b3", 1001003, "Label3", null, 2, "2024-01-01", "2024-01-01", "")
            )
        )
        override suspend fun create(bookmark: Bookmark) = Result.success(Unit)
        override suspend fun update(bookmark: Bookmark) = Result.success(Unit)
        override suspend fun delete(uuid: String, deletedAt: String) = Result.success(Unit)
        override fun watchAll(): Flow<List<Bookmark>> = flowOf(emptyList())
        override suspend fun getAllFolders() = Result.success(emptyList<BookmarkFolder>())
        override suspend fun getFoldersByParent(parentId: String) = Result.success(emptyList<BookmarkFolder>())
        override suspend fun getRootFolders() = Result.success(emptyList<BookmarkFolder>())
        override suspend fun createFolder(folder: BookmarkFolder) = Result.success(Unit)
        override suspend fun updateFolder(folder: BookmarkFolder) = Result.success(Unit)
        override suspend fun deleteFolder(uuid: String, deletedAt: String) = Result.success(Unit)
    }

    private val fakeSermonRepo = object : SermonRepository {
        override suspend fun getAll() = Result.success(emptyList<Sermon>())
        override suspend fun getByStatus(status: String) = Result.success(emptyList<Sermon>())
        override suspend fun getByUuid(uuid: String) = Result.success(null as Sermon?)
        override suspend fun create(sermon: Sermon) = Result.success(Unit)
        override suspend fun update(sermon: Sermon) = Result.success(Unit)
        override suspend fun delete(uuid: String, deletedAt: String) = Result.success(Unit)
        override suspend fun getSections(sermonId: String) = Result.success(emptyList<SermonSection>())
        override suspend fun createSection(section: SermonSection) = Result.success(Unit)
        override suspend fun updateSection(section: SermonSection) = Result.success(Unit)
        override suspend fun deleteSection(sectionId: Long) = Result.success(Unit)
        override suspend fun deleteAllSections(sermonId: String) = Result.success(Unit)
        override fun watchAll(): Flow<List<Sermon>> = flowOf(emptyList())
    }

    private val fakeReadingPlanRepo = object : ReadingPlanRepository {
        override suspend fun getPlans() = Result.success(
            listOf(ReadingPlan("rp1", "Plan 1", "Desc", 30, "sequential"))
        )
        override suspend fun getPlanByUuid(uuid: String) = Result.success(null as ReadingPlan?)
        override suspend fun getPlansByType(type: String) = Result.success(emptyList<ReadingPlan>())
        override suspend fun getProgress(planId: String) = Result.success(emptyList<PlanProgress>())
        override suspend fun getCompletedDays(planId: String) = Result.success(emptyList<PlanProgress>())
        override suspend fun markDayCompleted(planId: String, day: Long, completedAt: String) = Result.success(Unit)
        override suspend fun createPlan(plan: ReadingPlan) = Result.success(Unit)
        override suspend fun createProgress(progress: PlanProgress) = Result.success(Unit)
        override suspend fun deletePlan(uuid: String) = Result.success(Unit)
    }

    private val fakeHistoryRepo = object : HistoryRepository {
        override suspend fun getHistory(limit: Long) = Result.success(
            listOf(
                HistoryEntry(1, 1001001, "2024-01-01T12:00:00Z"),
                HistoryEntry(2, 1001002, "2024-01-01T11:00:00Z")
            )
        )
        override suspend fun addEntry(globalVerseId: Long) = Result.success(Unit)
        override suspend fun prune(keepCount: Long) = Result.success(Unit)
        override suspend fun clear() = Result.success(Unit)
    }

    private fun createComponent(): DefaultDashboardComponent {
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle = lifecycle)
        return DefaultDashboardComponent(
            componentContext = context,
            noteRepository = fakeNoteRepo,
            highlightRepository = fakeHighlightRepo,
            bookmarkRepository = fakeBookmarkRepo,
            sermonRepository = fakeSermonRepo,
            readingPlanRepository = fakeReadingPlanRepo,
            historyRepository = fakeHistoryRepo
        )
    }

    @Test
    fun refreshAggregatesStats() = runTest {
        val component = createComponent()

        // Wait until loading is complete and data is populated.
        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (
            (component.state.value.isLoading || component.state.value.totalNotes == 0) &&
            System.currentTimeMillis() - start < timeout
        ) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        val state = component.state.value
        assertEquals(2, state.totalNotes)
        assertEquals(1, state.totalHighlights)
        assertEquals(3, state.totalBookmarks)
        assertEquals(0, state.totalSermons)
        assertEquals(1, state.activePlans)
        assertEquals(2, state.recentHistory.size)
        assertTrue(state.recentHistory[0].contains("1001001"))

        // Widget assertions
        assertNotNull(state.dailyVerse)
        assertTrue(state.dailyVerse!!.text.isNotBlank())

        assertNotNull(state.continueReading)
        assertEquals(1001001L, state.continueReading!!.globalVerseId)

        assertNotNull(state.readingPlanProgress)
        assertEquals("Plan 1", state.readingPlanProgress!!.planTitle)
        assertEquals(30, state.readingPlanProgress!!.totalDays)

        assertEquals(2, state.recentNotes.size)
        assertEquals("Note 1", state.recentNotes[0].title)
    }
}
