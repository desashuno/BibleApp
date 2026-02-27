package org.biblestudio.features.cross_references.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class CrossRefRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: CrossRefRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = CrossRefRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun insertCrossRef(sourceVerseId: Long, targetVerseId: Long, type: String = "parallel") {
        testDb.database.referenceQueries.insertCrossRef(
            sourceVerseId = sourceVerseId,
            targetVerseId = targetVerseId,
            type = type,
            confidence = 1.0
        )
    }

    @Test
    fun `getRefsFromVerse returns outgoing references`() = runTest {
        insertCrossRef(1001001, 2001001)
        insertCrossRef(1001001, 3001001)

        val refs = repo.getRefsFromVerse(1001001).getOrThrow()
        assertEquals(2, refs.size)
    }

    @Test
    fun `getRefsToVerse returns incoming references`() = runTest {
        insertCrossRef(2001001, 1001001)

        val refs = repo.getRefsToVerse(1001001).getOrThrow()
        assertEquals(1, refs.size)
    }

    @Test
    fun `getAllForVerse returns both directions`() = runTest {
        insertCrossRef(1001001, 2001001)
        insertCrossRef(3001001, 1001001)

        val refs = repo.getAllForVerse(1001001).getOrThrow()
        assertEquals(2, refs.size)
    }

    @Test
    fun `getRefsFromVerse returns empty for no references`() = runTest {
        val refs = repo.getRefsFromVerse(99999999).getOrThrow()
        assertTrue(refs.isEmpty())
    }

    @Test
    fun `loadTskData returns zero stub`() = runTest {
        val count = repo.loadTskData().getOrThrow()
        assertEquals(0, count)
    }
}
