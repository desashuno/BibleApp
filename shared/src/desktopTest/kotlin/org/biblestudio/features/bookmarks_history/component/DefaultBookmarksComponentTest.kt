package org.biblestudio.features.bookmarks_history.component

import org.biblestudio.test.testComponentContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.entities.BookmarkFolder
import org.biblestudio.features.bookmarks_history.domain.entities.HistoryEntry
import org.biblestudio.features.bookmarks_history.domain.repositories.BookmarkRepository
import org.biblestudio.features.bookmarks_history.domain.repositories.HistoryRepository

@Suppress("TooManyFunctions")
class DefaultBookmarksComponentTest {

    private val storedBookmarks = mutableListOf<Bookmark>()
    private val storedFolders = mutableListOf<BookmarkFolder>()
    private val storedHistory = mutableListOf<HistoryEntry>()
    private var historyIdSeq = 1L

    @Suppress("TooManyFunctions")
    private val fakeBookmarkRepo = object : BookmarkRepository {
        override suspend fun getBookmarksForVerse(globalVerseId: Long): Result<List<Bookmark>> =
            Result.success(storedBookmarks.filter { it.globalVerseId == globalVerseId })

        override suspend fun getByFolder(folderId: String): Result<List<Bookmark>> =
            Result.success(storedBookmarks.filter { it.folderId == folderId })

        override suspend fun getAll(): Result<List<Bookmark>> = Result.success(storedBookmarks.toList())

        override suspend fun create(bookmark: Bookmark): Result<Unit> {
            storedBookmarks.add(bookmark)
            return Result.success(Unit)
        }

        override suspend fun update(bookmark: Bookmark): Result<Unit> = Result.success(Unit)

        override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> {
            storedBookmarks.removeAll { it.uuid == uuid }
            return Result.success(Unit)
        }

        override fun watchAll(): Flow<List<Bookmark>> = emptyFlow()

        override suspend fun getAllFolders(): Result<List<BookmarkFolder>> = Result.success(storedFolders.toList())

        override suspend fun getFoldersByParent(parentId: String): Result<List<BookmarkFolder>> =
            Result.success(storedFolders.filter { it.parentId == parentId })

        override suspend fun getRootFolders(): Result<List<BookmarkFolder>> =
            Result.success(storedFolders.filter { it.parentId == null })

        override suspend fun createFolder(folder: BookmarkFolder): Result<Unit> {
            storedFolders.add(folder)
            return Result.success(Unit)
        }

        override suspend fun updateFolder(folder: BookmarkFolder): Result<Unit> = Result.success(Unit)

        override suspend fun deleteFolder(uuid: String, deletedAt: String): Result<Unit> {
            storedFolders.removeAll { it.uuid == uuid }
            return Result.success(Unit)
        }
    }

    private val fakeHistoryRepo = object : HistoryRepository {
        override suspend fun getHistory(limit: Long): Result<List<HistoryEntry>> =
            Result.success(storedHistory.takeLast(limit.toInt()).reversed())

        override suspend fun addEntry(globalVerseId: Long): Result<Unit> {
            storedHistory.add(
                HistoryEntry(id = historyIdSeq++, globalVerseId = globalVerseId, visitedAt = "now")
            )
            return Result.success(Unit)
        }

        override suspend fun prune(keepCount: Long): Result<Unit> = Result.success(Unit)
        override suspend fun clear(): Result<Unit> {
            storedHistory.clear()
            return Result.success(Unit)
        }
    }

    private fun createComponent(verseBus: VerseBus = VerseBus()): DefaultBookmarksComponent {
        val context = testComponentContext()
        return DefaultBookmarksComponent(
            componentContext = context,
            bookmarkRepository = fakeBookmarkRepo,
            historyRepository = fakeHistoryRepo,
            verseBus = verseBus
        )
    }

    @Test
    fun initialStateIsBookmarksMode() {
        val component = createComponent()
        assertEquals(BookmarksViewMode.Bookmarks, component.state.value.viewMode)
    }

    @Test
    fun viewModeChangeSwitchesToHistory() {
        val component = createComponent()
        component.onViewModeChanged(BookmarksViewMode.History)
        assertEquals(BookmarksViewMode.History, component.state.value.viewMode)
    }

    @Test
    fun bookmarkTappedPublishesVerseBus() {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        val bm = Bookmark(
            uuid = "bm-1",
            globalVerseId = 1_001_001,
            label = "Gen 1:1",
            folderId = null,
            sortOrder = 0,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z",
            deviceId = "dev-1"
        )
        component.onBookmarkTapped(bm)

        val event = verseBus.current
        assertTrue(event is LinkEvent.VerseSelected)
    }

    @Test
    fun historyTappedPublishesVerseBus() {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        val entry = HistoryEntry(id = 1, globalVerseId = 2_001_001, visitedAt = "now")
        component.onHistoryTapped(entry)

        val event = verseBus.current
        assertTrue(event is LinkEvent.VerseSelected)
    }

    @Test
    fun clearHistoryEmptiesEntries() = runTest {
        val component = createComponent()

        storedHistory.add(HistoryEntry(1, 1_001_001, "now"))
        component.onClearHistory()

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (storedHistory.isNotEmpty() && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertTrue(storedHistory.isEmpty())
    }
}
