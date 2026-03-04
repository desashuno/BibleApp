package org.biblestudio.features.word_study.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class WordStudyRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: WordStudyRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = WordStudyRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun seedLexicon() {
        val db = testDb.database
        db.studyQueries.insertLexiconEntry(
            "H1254",
            "\u05D1\u05B8\u05BC\u05E8\u05B8\u05D0",
            "bara",
            "to create, shape, form",
            null
        )
        db.studyQueries.insertLexiconEntry(
            "H1255",
            "\u05D1\u05B7\u05BC\u05DC\u05D0\u05D3\u05DF",
            "Baladan",
            "Baladan",
            null
        )
    }

    private fun seedOccurrences() {
        val db = testDb.database
        db.studyQueries.insertOccurrence("H1254", 1_001_001, 5)
        db.studyQueries.insertOccurrence("H1254", 1_001_021, 3)
        db.studyQueries.insertOccurrence("H1254", 1_001_027, 3)
    }

    @Test
    fun strongsEntryResolvesFromId() = runTest {
        seedLexicon()

        val result = repo.lookupByStrongs("H1254")
        assertTrue(result.isSuccess)
        val entry = result.getOrNull()
        assertNotNull(entry)
        assertEquals("H1254", entry.strongsNumber)
        assertEquals("bara", entry.transliteration)
        assertEquals("to create, shape, form", entry.definition)
    }

    @Test
    fun occurrenceCountMatchesExpected() = runTest {
        seedLexicon()
        seedOccurrences()

        val result = repo.getOccurrences("H1254")
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
    }

    @Test
    fun occurrenceCountQuery() = runTest {
        seedOccurrences()

        val result = repo.getOccurrenceCount("H1254")
        assertTrue(result.isSuccess)
        assertEquals(3L, result.getOrNull())
    }

    @Test
    fun `getRelatedWords uses transliteration prefix when available`() = runTest {
        val db = testDb.database
        // H1254 bara — the entry we look up (transliteration prefix = "bar")
        db.studyQueries.insertLexiconEntry("H1254", "בָּרָא", "bara", "to create", null)
        // H1288 barak — same transliteration prefix "bar" → should be returned
        db.studyQueries.insertLexiconEntry("H1288", "בָּרַךְ", "barak", "to bless", null)
        // H1255 Baladan — different prefix "Bal" → should NOT be returned
        db.studyQueries.insertLexiconEntry("H1255", "בַּלְאָדָן", "Baladan", "Baladan", null)

        val result = repo.getRelatedWords("H1254")
        assertTrue(result.isSuccess)
        val related = result.getOrNull() ?: emptyList()
        assertTrue(related.any { it.strongsNumber == "H1288" }, "H1288 (barak) should match 'bar' prefix")
        assertTrue(related.none { it.strongsNumber == "H1255" }, "H1255 (Baladan) should not match 'bar' prefix")
        assertTrue(related.none { it.strongsNumber == "H1254" }, "H1254 itself should be excluded")
    }

    @Test
    fun `getRelatedWords falls back to numeric prefix when transliteration is short`() = runTest {
        val db = testDb.database
        // Entry with very short transliteration (< 3 chars) → falls back to numeric prefix
        db.studyQueries.insertLexiconEntry("H430", "אֱלֹהִים", "el", "God", null)
        db.studyQueries.insertLexiconEntry("H431", "אֱלוּ", "elu", "behold", null)
        db.studyQueries.insertLexiconEntry("H1254", "בָּרָא", "bara", "to create", null)

        val result = repo.getRelatedWords("H430")
        assertTrue(result.isSuccess)
        val related = result.getOrNull() ?: emptyList()
        // H431 shares numeric prefix "H43" and should be returned via fallback
        assertTrue(related.any { it.strongsNumber == "H431" }, "H431 should be found via numeric prefix fallback")
        assertTrue(related.none { it.strongsNumber == "H430" }, "H430 itself should be excluded")
    }
}
