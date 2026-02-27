package org.biblestudio.features.highlights.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.test.TestDatabase

class HighlightRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: HighlightRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = HighlightRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun highlight(
        uuid: String = "hl-1",
        globalVerseId: Long = 1_001_001,
        colorIndex: Long = 0,
        startOffset: Long = 0,
        endOffset: Long = -1
    ) = Highlight(
        uuid = uuid,
        globalVerseId = globalVerseId,
        colorIndex = colorIndex,
        style = "background",
        startOffset = startOffset,
        endOffset = endOffset,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z",
        deviceId = "device-1"
    )

    @Test
    fun `create and getHighlightsForVerse round-trip`() = runTest {
        repo.create(highlight()).getOrThrow()

        val results = repo.getHighlightsForVerse(1_001_001).getOrThrow()
        assertEquals(1, results.size)
        assertEquals(0L, results.first().colorIndex)
    }

    @Test
    fun `getAll returns all non-deleted highlights`() = runTest {
        repo.create(highlight("hl-1", 1_001_001)).getOrThrow()
        repo.create(highlight("hl-2", 1_001_002)).getOrThrow()

        val all = repo.getAll().getOrThrow()
        assertEquals(2, all.size)
    }

    @Test
    fun `getHighlightsForVerseRange returns range`() = runTest {
        repo.create(highlight("hl-1", 1_001_001)).getOrThrow()
        repo.create(highlight("hl-2", 1_001_005)).getOrThrow()
        repo.create(highlight("hl-3", 2_001_001)).getOrThrow()

        val range = repo.getHighlightsForVerseRange(1_001_001, 1_001_010).getOrThrow()
        assertEquals(2, range.size)
    }

    @Test
    fun `update changes color index`() = runTest {
        repo.create(highlight()).getOrThrow()
        repo.update(highlight().copy(colorIndex = 3)).getOrThrow()

        val fetched = repo.getHighlightsForVerse(1_001_001).getOrThrow()
        assertEquals(3L, fetched.first().colorIndex)
    }

    @Test
    fun `delete soft-deletes highlight`() = runTest {
        repo.create(highlight()).getOrThrow()
        repo.delete("hl-1", "2025-06-01T00:00:00Z").getOrThrow()

        val all = repo.getAll().getOrThrow()
        assertTrue(all.isEmpty())
    }

    @Test
    fun `sub-verse offsets are persisted`() = runTest {
        repo.create(highlight(startOffset = 5, endOffset = 20)).getOrThrow()

        val hl = repo.getHighlightsForVerse(1_001_001).getOrThrow().first()
        assertEquals(5L, hl.startOffset)
        assertEquals(20L, hl.endOffset)
    }
}
