package org.biblestudio.features.bible_reader.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class BibleRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: BibleRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = BibleRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun insertBibleWithVerse(
        abbreviation: String = "KJV",
        bookNumber: Long = 1,
        chapterNumber: Long = 1,
        verseNumber: Long = 1,
        globalVerseId: Long = 1001001,
        text: String = "In the beginning God created the heaven and the earth."
    ) {
        val db = testDb.database
        db.bibleQueries.insertBible(abbreviation, "King James Version", "en", "ltr")
        val bibleId = db.bibleQueries.allBibles().executeAsList().first().id
        db.bibleQueries.insertBook(bibleId, bookNumber, "Genesis", "OT")
        val bookId = db.bibleQueries.allBooksForBible(bibleId).executeAsList().first().id
        db.bibleQueries.insertChapter(bookId, chapterNumber, 31)
        val chapterId = db.bibleQueries.chaptersForBook(bookId).executeAsList().first().id
        db.bibleQueries.insertVerse(chapterId, globalVerseId, verseNumber, text, null)
    }

    @Test
    fun `getVerses returns correct chapter content`() = runTest {
        insertBibleWithVerse()
        val db = testDb.database
        val bibleId = db.bibleQueries.allBibles().executeAsList().first().id
        val bookId = db.bibleQueries.allBooksForBible(bibleId).executeAsList().first().id

        val verses = repo.getVerses(bookId, 1).getOrThrow()
        assertEquals(1, verses.size)
        assertEquals("In the beginning God created the heaven and the earth.", verses[0].text)
        assertEquals(1L, verses[0].verseNumber)
    }

    @Test
    fun `getVerse returns single verse by global ID`() = runTest {
        insertBibleWithVerse(globalVerseId = 1001001)

        val verse = repo.getVerseByGlobalId(1001001).getOrThrow()
        assertNotNull(verse)
        assertEquals(1001001, verse.globalVerseId)
    }

    @Test
    fun `getVersesInRange returns inclusive range`() = runTest {
        val db = testDb.database
        db.bibleQueries.insertBible("KJV", "King James Version", "en", "ltr")
        val bibleId = db.bibleQueries.allBibles().executeAsList().first().id
        db.bibleQueries.insertBook(bibleId, 1, "Genesis", "OT")
        val bookId = db.bibleQueries.allBooksForBible(bibleId).executeAsList().first().id
        db.bibleQueries.insertChapter(bookId, 1, 3)
        val chapterId = db.bibleQueries.chaptersForBook(bookId).executeAsList().first().id
        db.bibleQueries.insertVerse(chapterId, 1001001, 1, "Verse 1", null)
        db.bibleQueries.insertVerse(chapterId, 1001002, 2, "Verse 2", null)
        db.bibleQueries.insertVerse(chapterId, 1001003, 3, "Verse 3", null)

        val range = repo.getVersesInRange(1001001, 1001003).getOrThrow()
        assertEquals(3, range.size)
    }

    @Test
    fun `getBooks returns all books for a bible`() = runTest {
        val db = testDb.database
        db.bibleQueries.insertBible("KJV", "King James Version", "en", "ltr")
        val bibleId = db.bibleQueries.allBibles().executeAsList().first().id
        db.bibleQueries.insertBook(bibleId, 1, "Genesis", "OT")
        db.bibleQueries.insertBook(bibleId, 2, "Exodus", "OT")

        val books = repo.getBooks(bibleId).getOrThrow()
        assertEquals(2, books.size)
        assertEquals("Genesis", books[0].name)
        assertEquals("Exodus", books[1].name)
    }

    @Test
    fun `getAvailableBibles returns all bibles`() = runTest {
        val db = testDb.database
        db.bibleQueries.insertBible("KJV", "King James Version", "en", "ltr")
        db.bibleQueries.insertBible("ASV", "American Standard Version", "en", "ltr")

        val bibles = repo.getAvailableBibles().getOrThrow()
        assertEquals(2, bibles.size)
    }

    @Test
    fun `searchVerses returns matching verses via FTS`() = runTest {
        insertBibleWithVerse(text = "In the beginning God created the heaven and the earth.")

        val results = repo.searchVerses("beginning", 10).getOrThrow()
        assertTrue(results.isNotEmpty())
        assertTrue(results.first().text.contains("beginning"))
    }
}
