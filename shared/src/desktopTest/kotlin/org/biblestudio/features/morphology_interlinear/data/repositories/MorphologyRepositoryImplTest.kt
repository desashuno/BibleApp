package org.biblestudio.features.morphology_interlinear.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class MorphologyRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: MorphologyRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = MorphologyRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun seedMorphology() {
        val db = testDb.database
        db.studyQueries.insertMorphology(
            globalVerseId = 1_001_001,
            wordPosition = 1,
            strongsNumber = "H7225",
            parsingCode = "N-FSC",
            surfaceForm = "בְּרֵאשִׁית",
            lemma = "reshith",
            gloss = "beginning"
        )
        db.studyQueries.insertMorphology(
            globalVerseId = 1_001_001,
            wordPosition = 2,
            strongsNumber = "H1254",
            parsingCode = "V-QAL-3MS",
            surfaceForm = "בָּרָא",
            lemma = "bara",
            gloss = "created"
        )
        db.studyQueries.insertMorphology(
            globalVerseId = 1_001_001,
            wordPosition = 3,
            strongsNumber = "H430",
            parsingCode = "N-MPC",
            surfaceForm = "אֱלֹהִים",
            lemma = "elohim",
            gloss = "God"
        )
    }

    private fun seedAlignment() {
        val db = testDb.database
        db.studyQueries.insertAlignment(
            globalVerseId = 1_001_001,
            englishPosition = 1,
            englishToken = "In",
            originalPosition = 1,
            strongsNumber = "H7225"
        )
        db.studyQueries.insertAlignment(
            globalVerseId = 1_001_001,
            englishPosition = 2,
            englishToken = "the beginning",
            originalPosition = 1,
            strongsNumber = "H7225"
        )
        db.studyQueries.insertAlignment(
            globalVerseId = 1_001_001,
            englishPosition = 3,
            englishToken = "God",
            originalPosition = 3,
            strongsNumber = "H430"
        )
        db.studyQueries.insertAlignment(
            globalVerseId = 1_001_001,
            englishPosition = 4,
            englishToken = "created",
            originalPosition = 2,
            strongsNumber = "H1254"
        )
    }

    @Test
    fun morphologyReturnsWordsInPositionalOrder() = runTest {
        seedMorphology()

        val result = repo.getMorphWords(1_001_001)
        assertTrue(result.isSuccess)
        val words = result.getOrNull()!!
        assertEquals(3, words.size)
        assertEquals(1L, words[0].wordPosition)
        assertEquals(2L, words[1].wordPosition)
        assertEquals(3L, words[2].wordPosition)
        assertEquals("בְּרֵאשִׁית", words[0].surfaceForm)
        assertEquals("created", words[1].gloss)
        assertEquals("H430", words[2].strongsNumber)
    }

    @Test
    fun getWordsByStongsReturnsCrossVerseResults() = runTest {
        seedMorphology()
        // Add same strongs in a different verse
        testDb.database.studyQueries.insertMorphology(
            globalVerseId = 1_002_003,
            wordPosition = 1,
            strongsNumber = "H1254",
            parsingCode = "V-QAL-3MS",
            surfaceForm = "בָּרָא",
            lemma = "bara",
            gloss = "created"
        )

        val result = repo.getWordsByStrongs("H1254")
        assertTrue(result.isSuccess)
        val words = result.getOrNull()!!
        assertEquals(2, words.size)
        assertTrue(words.all { it.strongsNumber == "H1254" })
    }

    @Test
    fun alignmentForVerseReturnsOrderedTokens() = runTest {
        seedAlignment()

        val result = repo.getAlignmentForVerse(1_001_001)
        assertTrue(result.isSuccess)
        val entries = result.getOrNull()!!
        assertEquals(4, entries.size)
        assertEquals("In", entries[0].englishToken)
        assertEquals("created", entries[3].englishToken)
        assertEquals("H1254", entries[3].strongsNumber)
    }
}
