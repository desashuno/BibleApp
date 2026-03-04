package org.biblestudio.features.passage_guide.data.repositories

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.biblestudio.features.bible_reader.domain.entities.Bible
import org.biblestudio.features.bible_reader.domain.entities.Book
import org.biblestudio.features.bible_reader.domain.entities.Chapter
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.bible_reader.domain.repositories.BibleRepository
import org.biblestudio.core.study.CrossReference
import org.biblestudio.features.cross_references.domain.repositories.CrossRefRepository
import org.biblestudio.features.morphology_interlinear.domain.entities.AlignmentEntry
import org.biblestudio.core.study.MorphWord
import org.biblestudio.features.morphology_interlinear.domain.entities.MorphologyData
import org.biblestudio.core.study.WordOccurrence
import org.biblestudio.features.morphology_interlinear.domain.repositories.MorphologyRepository
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.note_editor.domain.repositories.NoteRepository
import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry
import org.biblestudio.features.resource_library.domain.repositories.ResourceRepository
import org.biblestudio.core.study.LexiconEntry
import org.biblestudio.features.word_study.domain.repositories.WordStudyRepository
import org.biblestudio.test.TestDatabase

class PassageGuideRepositoryImplTest {

    private val testVerse = Verse(1, 1, 1_001_001, 1, "In the beginning God created the heavens and the earth.")

    private val testCrossRefs = listOf(
        CrossReference(1, 1_001_001, 43_001_001, "parallel", 0.95)
    )

    private val testMorphWords = listOf(
        MorphWord(1, 1_001_001, 1, "בְּרֵאשִׁית", "reshith", "H7225", "N-FSC", "beginning"),
        MorphWord(2, 1_001_001, 2, "בָּרָא", "bara", "H1254", "V-QAL-3MS", "created")
    )

    private val testLexicon = LexiconEntry("H1254", "בָּרָא", "bara", "to create", null)

    private val testNote =
        Note(
            "note-1",
            1_001_001,
            "My Note",
            "Content",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            deviceId = "dev1"
        )

    private val fakeBibleRepo = object : BibleRepository {
        override suspend fun getAvailableBibles(): Result<List<Bible>> = Result.success(emptyList())
        override suspend fun getBooks(bibleId: Long): Result<List<Book>> = Result.success(emptyList())
        override suspend fun getChapters(bookId: Long): Result<List<Chapter>> = Result.success(emptyList())
        override suspend fun getVerses(bookId: Long, chapter: Long): Result<List<Verse>> = Result.success(emptyList())
        override suspend fun getVersesForBook(bookId: Long): Result<List<Verse>> = Result.success(emptyList())

        override suspend fun getVerseByGlobalId(globalVerseId: Long): Result<Verse?> =
            if (globalVerseId == 1_001_001L) Result.success(testVerse) else Result.success(null)

        override suspend fun getVersesInRange(startId: Long, endId: Long): Result<List<Verse>> =
            Result.success(emptyList())

        override suspend fun searchVerses(query: String, maxResults: Long): Result<List<Verse>> =
            Result.success(emptyList())

        override suspend fun getActiveBibles(): Result<List<Bible>> = Result.success(emptyList())
        override suspend fun getAvailableBiblesByLanguage(languageCode: String): Result<List<Bible>> = Result.success(emptyList())
        override suspend fun getActiveBiblesByLanguage(languageCode: String): Result<List<Bible>> = Result.success(emptyList())
        override suspend fun getNextVerseId(currentId: Long): Result<Long?> = Result.success(null)
        override suspend fun getPreviousVerseId(currentId: Long): Result<Long?> = Result.success(null)

        override fun watchBibles(): Flow<List<Bible>> = emptyFlow()
    }

    private val fakeCrossRefRepo = object : CrossRefRepository {
        override suspend fun getRefsFromVerse(globalVerseId: Long): Result<List<CrossReference>> =
            Result.success(emptyList())

        override suspend fun getRefsToVerse(globalVerseId: Long): Result<List<CrossReference>> =
            Result.success(emptyList())

        override suspend fun getAllForVerse(globalVerseId: Long): Result<List<CrossReference>> =
            if (globalVerseId == 1_001_001L) Result.success(testCrossRefs) else Result.success(emptyList())

        override suspend fun loadTskData(): Result<Int> = Result.success(0)
    }

    private val fakeMorphRepo = object : MorphologyRepository {
        override suspend fun getMorphologyForVerse(globalVerseId: Long): Result<List<MorphologyData>> =
            Result.success(emptyList())

        override suspend fun getMorphWords(globalVerseId: Long): Result<List<MorphWord>> =
            if (globalVerseId == 1_001_001L) Result.success(testMorphWords) else Result.success(emptyList())

        override suspend fun getWordsByStrongs(strongsNumber: String): Result<List<MorphWord>> =
            Result.success(emptyList())

        override suspend fun getOccurrences(
            strongsNumber: String,
            limit: Long,
            offset: Long,
        ): Result<List<WordOccurrence>> = Result.success(emptyList())

        override suspend fun getOccurrenceCount(strongsNumber: String): Result<Long> = Result.success(0L)

        override suspend fun getAlignmentForVerse(globalVerseId: Long): Result<List<AlignmentEntry>> =
            Result.success(emptyList())
    }

    private val fakeWordStudyRepo = object : WordStudyRepository {
        override suspend fun lookupByStrongs(strongsNumber: String): Result<LexiconEntry?> =
            if (strongsNumber == "H1254") Result.success(testLexicon) else Result.success(null)

        override suspend fun getOccurrences(
            strongsNumber: String,
            limit: Long,
            offset: Long,
        ): Result<List<WordOccurrence>> = Result.success(emptyList())

        override suspend fun getOccurrenceCount(strongsNumber: String): Result<Long> = Result.success(0L)

        override suspend fun getRelatedWords(strongsNumber: String): Result<List<LexiconEntry>> =
            Result.success(emptyList())

        override suspend fun searchLexicon(query: String, maxResults: Long): Result<List<LexiconEntry>> =
            Result.success(emptyList())
    }

    private val fakeResourceRepo = object : ResourceRepository {
        override suspend fun getAllResources(): Result<List<Resource>> = Result.success(emptyList())
        override suspend fun getByType(type: String): Result<List<Resource>> = Result.success(emptyList())
        override suspend fun getByUuid(uuid: String): Result<Resource?> = Result.success(null)
        override suspend fun getEntriesForVerse(resourceId: String, globalVerseId: Long): Result<List<ResourceEntry>> =
            Result.success(emptyList())
        override suspend fun getAllEntries(resourceId: String): Result<List<ResourceEntry>> =
            Result.success(emptyList())
        override suspend fun searchEntries(query: String, maxResults: Long): Result<List<ResourceEntry>> =
            Result.success(emptyList())
    }

    private val fakeNoteRepo = object : NoteRepository {
        override suspend fun getNotesForVerse(globalVerseId: Long): Result<List<Note>> =
            if (globalVerseId == 1_001_001L) Result.success(listOf(testNote)) else Result.success(emptyList())

        override suspend fun getAllNotes(limit: Long, offset: Long): Result<List<Note>> = Result.success(emptyList())

        override suspend fun getNoteByUuid(uuid: String): Result<Note?> = Result.success(null)
        override suspend fun create(note: Note): Result<Unit> = Result.success(Unit)
        override suspend fun update(note: Note): Result<Unit> = Result.success(Unit)
        override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> = Result.success(Unit)
        override fun watchNotesForVerse(globalVerseId: Long): Flow<List<Note>> = emptyFlow()
        override suspend fun searchNotes(query: String, maxResults: Long): Result<List<Note>> =
            Result.success(emptyList())
    }

    @Test
    fun buildReportAggregatesAllSources() = runTest {
        val testDb = TestDatabase()
        // Seed an outline in the database
        testDb.database.studyQueries.insertOutline(
            globalVerseStart = 1_001_001,
            globalVerseEnd = 1_001_031,
            title = "Creation Account",
            content = "Gen 1:1-31",
            source = "Study Bible"
        )

        val repo = PassageGuideRepositoryImpl(
            database = testDb.database,
            bibleRepository = fakeBibleRepo,
            crossRefRepository = fakeCrossRefRepo,
            morphologyRepository = fakeMorphRepo,
            wordStudyRepository = fakeWordStudyRepo,
            resourceRepository = fakeResourceRepo,
            noteRepository = fakeNoteRepo
        )

        val result = repo.buildReport(1_001_001)
        assertTrue(result.isSuccess)

        val report = result.getOrNull()!!
        assertEquals(1_001_001L, report.verseId)
        assertEquals("In the beginning God created the heavens and the earth.", report.verseText)
        assertEquals(1, report.crossReferences.size)
        assertEquals(1, report.outlines.size)
        assertEquals("Creation Account", report.outlines[0].title)
        assertEquals(1, report.keyWords.size)
        assertEquals("H1254", report.keyWords[0].strongsNumber)
        assertEquals(1, report.userNotes.size)
        assertEquals(2, report.morphologyWords.size)

        testDb.close()
    }

    @Test
    fun buildReportReturnsEmptyForUnknownVerse() = runTest {
        val testDb = TestDatabase()
        val repo = PassageGuideRepositoryImpl(
            database = testDb.database,
            bibleRepository = fakeBibleRepo,
            crossRefRepository = fakeCrossRefRepo,
            morphologyRepository = fakeMorphRepo,
            wordStudyRepository = fakeWordStudyRepo,
            resourceRepository = fakeResourceRepo,
            noteRepository = fakeNoteRepo
        )

        val result = repo.buildReport(99_999_999)
        assertTrue(result.isSuccess)

        val report = result.getOrNull()!!
        assertNotNull(report)
        assertTrue(report.crossReferences.isEmpty())
        assertTrue(report.outlines.isEmpty())
        assertTrue(report.userNotes.isEmpty())

        testDb.close()
    }
}
