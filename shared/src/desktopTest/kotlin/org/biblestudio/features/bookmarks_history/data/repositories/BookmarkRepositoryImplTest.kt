package org.biblestudio.features.bookmarks_history.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.entities.BookmarkFolder
import org.biblestudio.test.TestDatabase

class BookmarkRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: BookmarkRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = BookmarkRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun bookmark(uuid: String = "bm-1", globalVerseId: Long = 500L) = Bookmark(
        uuid = uuid,
        globalVerseId = globalVerseId,
        label = "My Bookmark",
        folderId = null,
        sortOrder = 0L,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z",
        deviceId = "device-1"
    )

    @Test
    fun `create and retrieve bookmark by verse`() = runTest {
        repo.create(bookmark()).getOrThrow()

        val results = repo.getBookmarksForVerse(500L).getOrThrow()
        assertEquals(1, results.size)
        assertEquals("My Bookmark", results.first().label)
        assertEquals(500L, results.first().globalVerseId)
    }

    @Test
    fun `getAll returns all non-deleted bookmarks`() = runTest {
        repo.create(bookmark("bm-1")).getOrThrow()
        repo.create(bookmark("bm-2", 600L)).getOrThrow()

        val all = repo.getAll().getOrThrow()
        assertEquals(2, all.size)
    }

    @Test
    fun `soft delete hides bookmark`() = runTest {
        repo.create(bookmark()).getOrThrow()
        repo.delete("bm-1", "2025-06-01T00:00:00Z").getOrThrow()

        val all = repo.getAll().getOrThrow()
        assertTrue(all.isEmpty())
    }

    // ─── Folder tests ───

    private fun folder(uuid: String = "folder-1", name: String = "Study") = BookmarkFolder(
        uuid = uuid,
        name = name,
        parentId = null,
        sortOrder = 0L,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z",
        deviceId = "device-1"
    )

    @Test
    fun `createFolder and getRootFolders round-trip`() = runTest {
        repo.createFolder(folder()).getOrThrow()
        val roots = repo.getRootFolders().getOrThrow()
        assertEquals(1, roots.size)
        assertEquals("Study", roots.first().name)
    }

    @Test
    fun `getFoldersByParent returns children`() = runTest {
        repo.createFolder(folder("parent", "Parent")).getOrThrow()
        repo.createFolder(folder("child", "Child").copy(parentId = "parent")).getOrThrow()

        val children = repo.getFoldersByParent("parent").getOrThrow()
        assertEquals(1, children.size)
        assertEquals("Child", children.first().name)
    }

    @Test
    fun `deleteFolder soft-deletes folder`() = runTest {
        repo.createFolder(folder()).getOrThrow()
        repo.deleteFolder("folder-1", "2025-06-01T00:00:00Z").getOrThrow()

        val all = repo.getAllFolders().getOrThrow()
        assertTrue(all.isEmpty())
    }
}
