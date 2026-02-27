package org.biblestudio.features.bookmarks_history.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class HistoryRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: HistoryRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = HistoryRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    @Test
    fun `addEntry and getHistory round-trip`() = runTest {
        repo.addEntry(1_001_001).getOrThrow()

        val history = repo.getHistory(10).getOrThrow()
        assertEquals(1, history.size)
        assertEquals(1_001_001L, history.first().globalVerseId)
    }

    @Test
    fun `getHistory returns most recent first`() = runTest {
        repo.addEntry(1_001_001).getOrThrow()
        repo.addEntry(1_001_002).getOrThrow()
        repo.addEntry(1_001_003).getOrThrow()

        val history = repo.getHistory(10).getOrThrow()
        assertEquals(3, history.size)
        // Most recent (last inserted) first
        assertEquals(1_001_003L, history.first().globalVerseId)
    }

    @Test
    fun `getHistory respects limit`() = runTest {
        repeat(5) { i ->
            repo.addEntry(1_001_001L + i).getOrThrow()
        }

        val history = repo.getHistory(2).getOrThrow()
        assertEquals(2, history.size)
    }

    @Test
    fun `clear removes all entries`() = runTest {
        repo.addEntry(1_001_001).getOrThrow()
        repo.addEntry(1_001_002).getOrThrow()
        repo.clear().getOrThrow()

        val history = repo.getHistory(10).getOrThrow()
        assertTrue(history.isEmpty())
    }

    @Test
    fun `prune keeps only specified count`() = runTest {
        repeat(5) { i ->
            repo.addEntry(1_001_001L + i).getOrThrow()
        }

        repo.prune(2).getOrThrow()

        val history = repo.getHistory(10).getOrThrow()
        assertEquals(2, history.size)
    }
}
