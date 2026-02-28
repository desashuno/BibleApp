package org.biblestudio.features.exegetical_guide.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class CommentaryRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: CommentaryRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = CommentaryRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun insertCommentary(uuid: String, title: String, author: String) {
        testDb.database.resourceQueries.insertResource(
            uuid = uuid,
            type = "commentary",
            title = title,
            author = author,
            version = "1.0",
            format = "text",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            deviceId = ""
        )
    }

    private fun insertEntry(resourceId: String, globalVerseId: Long, content: String, sortOrder: Long = 0) {
        testDb.database.resourceQueries.insertEntry(
            resourceId = resourceId,
            globalVerseId = globalVerseId,
            content = content,
            sortOrder = sortOrder
        )
    }

    @Test
    fun `getCommentaries returns only commentary resources`() = runTest {
        insertCommentary("mhc", "Matthew Henry", "Matthew Henry")
        testDb.database.resourceQueries.insertResource(
            uuid = "dict",
            type = "dictionary",
            title = "Easton's Dictionary",
            author = "Easton",
            version = "1.0",
            format = "text",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            deviceId = ""
        )

        val result = repo.getCommentaries().getOrThrow()
        assertEquals(1, result.size)
        assertEquals("Matthew Henry", result[0].title)
    }

    @Test
    fun `getEntriesForVerse returns entries for specific resource`() = runTest {
        insertCommentary("mhc", "Matthew Henry", "Matthew Henry")
        insertCommentary("gill", "John Gill", "John Gill")
        insertEntry("mhc", 1001001, "MH commentary on Gen 1:1")
        insertEntry("gill", 1001001, "Gill commentary on Gen 1:1")

        val result = repo.getEntriesForVerse("mhc", 1001001).getOrThrow()
        assertEquals(1, result.size)
        assertEquals("MH commentary on Gen 1:1", result[0].content)
    }

    @Test
    fun `getAllEntriesForVerse returns entries from all commentaries`() = runTest {
        insertCommentary("mhc", "Matthew Henry", "Matthew Henry")
        insertCommentary("gill", "John Gill", "John Gill")
        insertEntry("mhc", 1001001, "MH commentary on Gen 1:1")
        insertEntry("gill", 1001001, "Gill commentary on Gen 1:1")

        val result = repo.getAllEntriesForVerse(1001001).getOrThrow()
        assertEquals(2, result.size)
        assertTrue(result.any { it.resourceTitle == "John Gill" })
        assertTrue(result.any { it.resourceTitle == "Matthew Henry" })
    }

    @Test
    fun `getAllEntriesForVerse excludes non-commentary resources`() = runTest {
        insertCommentary("mhc", "Matthew Henry", "Matthew Henry")
        testDb.database.resourceQueries.insertResource(
            uuid = "dict",
            type = "dictionary",
            title = "Easton's Dictionary",
            author = "Easton",
            version = "1.0",
            format = "text",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            deviceId = ""
        )
        insertEntry("mhc", 1001001, "MH commentary")
        insertEntry("dict", 1001001, "Dictionary entry")

        val result = repo.getAllEntriesForVerse(1001001).getOrThrow()
        assertEquals(1, result.size)
        assertEquals("MH commentary", result[0].content)
    }

    @Test
    fun `getAllEntriesForVerse returns empty when no commentaries exist`() = runTest {
        val result = repo.getAllEntriesForVerse(1001001).getOrThrow()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllEntriesForVerse excludes soft-deleted commentaries`() = runTest {
        insertCommentary("mhc", "Matthew Henry", "Matthew Henry")
        insertEntry("mhc", 1001001, "MH commentary")
        testDb.database.resourceQueries.softDeleteResource(
            deletedAt = "2024-06-01",
            uuid = "mhc"
        )

        val result = repo.getAllEntriesForVerse(1001001).getOrThrow()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllEntriesForVerse includes resource metadata`() = runTest {
        insertCommentary("mhc", "Matthew Henry Complete", "Matthew Henry")
        insertEntry("mhc", 1001001, "In the beginning...")

        val result = repo.getAllEntriesForVerse(1001001).getOrThrow()
        assertEquals(1, result.size)
        assertEquals("Matthew Henry Complete", result[0].resourceTitle)
        assertEquals("Matthew Henry", result[0].resourceAuthor)
    }

    @Test
    fun `search returns matching entries`() = runTest {
        insertCommentary("mhc", "Matthew Henry", "Matthew Henry")
        insertEntry("mhc", 1001001, "In the beginning God created the heavens")

        // FTS search — note: FTS triggers populate fts_resources on insert
        val result = repo.search("beginning", 10).getOrThrow()
        assertEquals(1, result.size)
    }
}
