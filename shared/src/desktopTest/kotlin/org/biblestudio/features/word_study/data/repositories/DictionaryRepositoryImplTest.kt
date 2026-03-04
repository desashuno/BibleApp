package org.biblestudio.features.word_study.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class DictionaryRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: DictionaryRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = DictionaryRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun insertDictionaryResource(uuid: String, title: String, author: String) {
        testDb.database.resourceQueries.insertResource(
            uuid = uuid,
            type = "dictionary",
            title = title,
            author = author,
            version = "1.0",
            format = "text",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            deviceId = ""
        )
    }

    private fun insertEntry(
        resourceId: String,
        headword: String,
        content: String,
        relatedStrongs: String? = null
    ): Long {
        testDb.database.dictionaryQueries.insertEntry(
            resourceId = resourceId,
            headword = headword,
            content = content,
            relatedStrongs = relatedStrongs,
            sortOrder = 0
        )
        return testDb.database.dictionaryQueries.lastInsertEntryId().executeAsOne()
    }

    private fun linkEntryToVerse(entryId: Long, globalVerseId: Long) {
        testDb.database.dictionaryQueries.insertEntryVerse(
            entryId = entryId,
            globalVerseId = globalVerseId
        )
    }

    @Test
    fun `getDictionaries returns only dictionary resources`() = runTest {
        insertDictionaryResource("easton", "Easton's Bible Dictionary", "M.G. Easton")
        testDb.database.resourceQueries.insertResource(
            uuid = "mhc",
            type = "commentary",
            title = "Matthew Henry",
            author = "Matthew Henry",
            version = "1.0",
            format = "text",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            deviceId = ""
        )

        val result = repo.getDictionaries().getOrThrow()
        assertEquals(1, result.size)
        assertEquals("Easton's Bible Dictionary", result[0].title)
    }

    @Test
    fun `getEntriesForVerse returns entries linked to verse`() = runTest {
        insertDictionaryResource("easton", "Easton's", "Easton")
        val entryId = insertEntry("easton", "Abraham", "Father of many nations")
        linkEntryToVerse(entryId, 1001001)

        val result = repo.getEntriesForVerse(1001001).getOrThrow()
        assertEquals(1, result.size)
        assertEquals("Abraham", result[0].headword)
        assertEquals("Easton's", result[0].resourceTitle)
    }

    @Test
    fun `getEntriesForVerse returns entries from multiple dictionaries`() = runTest {
        insertDictionaryResource("easton", "Easton's", "Easton")
        insertDictionaryResource("smith", "Smith's", "Smith")
        val entry1 = insertEntry("easton", "Creation", "The act of God creating...")
        val entry2 = insertEntry("smith", "Creation", "Smith's view of creation...")
        linkEntryToVerse(entry1, 1001001)
        linkEntryToVerse(entry2, 1001001)

        val result = repo.getEntriesForVerse(1001001).getOrThrow()
        assertEquals(2, result.size)
    }

    @Test
    fun `getEntriesForVerse returns empty when no entries linked`() = runTest {
        val result = repo.getEntriesForVerse(1001001).getOrThrow()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getByHeadword returns matching entries`() = runTest {
        insertDictionaryResource("easton", "Easton's", "Easton")
        insertEntry("easton", "Abraham", "Father of many nations")
        insertEntry("easton", "Moses", "Leader of the Exodus")

        val result = repo.getByHeadword("easton", "Abraham").getOrThrow()
        assertEquals(1, result.size)
        assertEquals("Abraham", result[0].headword)
    }

    @Test
    fun `search returns matching entries via FTS`() = runTest {
        insertDictionaryResource("easton", "Easton's", "Easton")
        insertEntry("easton", "Abraham", "Father of many nations, patriarch of Israel")

        val result = repo.search("patriarch", 10).getOrThrow()
        assertEquals(1, result.size)
        assertEquals("Abraham", result[0].headword)
    }

    @Test
    fun `getEntryCount returns total entries`() = runTest {
        insertDictionaryResource("easton", "Easton's", "Easton")
        insertEntry("easton", "Abraham", "Entry 1")
        insertEntry("easton", "Moses", "Entry 2")
        insertEntry("easton", "David", "Entry 3")

        val count = repo.getEntryCount().getOrThrow()
        assertEquals(3, count)
    }

    @Test
    fun `getEntryCount returns zero on empty DB`() = runTest {
        val count = repo.getEntryCount().getOrThrow()
        assertEquals(0, count)
    }

    @Test
    fun `entry with related Strongs number is preserved`() = runTest {
        insertDictionaryResource("easton", "Easton's", "Easton")
        insertEntry("easton", "Love", "Definition of love", "G0026,H0157")

        val result = repo.getByHeadword("easton", "Love").getOrThrow()
        assertEquals(1, result.size)
        assertEquals("G0026,H0157", result[0].relatedStrongs)
    }
}
