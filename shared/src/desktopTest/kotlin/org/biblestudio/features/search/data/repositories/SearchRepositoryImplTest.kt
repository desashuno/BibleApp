package org.biblestudio.features.search.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class SearchRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: SearchRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = SearchRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun seedVerses() {
        val db = testDb.database
        db.bibleQueries.insertBible("KJV", "King James Version", "en", "ltr")
        val bibleId = db.bibleQueries.allBibles().executeAsList().first().id

        // OT book 1 (Genesis)
        db.bibleQueries.insertBook(bibleId, 1, "Genesis", "OT")
        val bookId = db.bibleQueries.allBooksForBible(bibleId).executeAsList().first().id
        db.bibleQueries.insertChapter(bookId, 1, 2)
        val chapterId = db.bibleQueries.chaptersForBook(bookId).executeAsList().first().id
        db.bibleQueries.insertVerse(chapterId, 1001001, 1, "In the beginning God created the heaven", null)
        db.bibleQueries.insertVerse(chapterId, 1001002, 2, "And the earth was without form", null)

        // NT book 40 (Matthew)
        db.bibleQueries.insertBook(bibleId, 40, "Matthew", "NT")
        val ntBookId = db.bibleQueries.allBooksForBible(bibleId).executeAsList().last().id
        db.bibleQueries.insertChapter(ntBookId, 1, 1)
        val ntChapterId = db.bibleQueries.chaptersForBook(ntBookId).executeAsList().first().id
        db.bibleQueries.insertVerse(ntChapterId, 40001001, 1, "The beginning of the gospel", null)
    }

    @Test
    fun `searchVerses returns matching results`() = runTest {
        seedVerses()
        val results = repo.searchVerses("beginning", 10).getOrThrow()
        assertEquals(2, results.size)
    }

    @Test
    fun `searchVersesFiltered filters by OT testament`() = runTest {
        seedVerses()
        val results = repo.searchVersesFiltered("beginning", testament = "OT", maxResults = 10).getOrThrow()
        assertTrue(results.all { it.globalVerseId < 40_000_000 })
    }

    @Test
    fun `searchVersesFiltered filters by NT testament`() = runTest {
        seedVerses()
        val results = repo.searchVersesFiltered("beginning", testament = "NT", maxResults = 10).getOrThrow()
        assertTrue(results.all { it.globalVerseId >= 40_000_000 })
    }

    @Test
    fun `searchVerses returns empty for no match`() = runTest {
        seedVerses()
        val results = repo.searchVerses("nonexistent", 10).getOrThrow()
        assertTrue(results.isEmpty())
    }
}
