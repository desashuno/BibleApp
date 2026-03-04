package org.biblestudio.features.word_study.component

import org.biblestudio.test.testComponentContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.core.study.WordOccurrence
import org.biblestudio.core.study.LexiconEntry
import org.biblestudio.features.word_study.domain.repositories.WordStudyRepository

class DefaultWordStudyComponentTest {

    private val testEntry = LexiconEntry(
        strongsNumber = "H1254",
        originalWord = "\u05D1\u05B8\u05BC\u05E8\u05B8\u05D0",
        transliteration = "bara",
        definition = "to create, shape, form",
        usageNotes = null
    )

    private val testOccurrences = listOf(
        WordOccurrence(1, "H1254", 1_001_001, 5),
        WordOccurrence(2, "H1254", 1_001_021, 3),
        WordOccurrence(3, "H1254", 1_001_027, 3)
    )

    private val fakeRepo = object : WordStudyRepository {
        override suspend fun lookupByStrongs(strongsNumber: String): Result<LexiconEntry?> =
            if (strongsNumber == "H1254") Result.success(testEntry) else Result.success(null)

        override suspend fun getOccurrences(
            strongsNumber: String,
            limit: Long,
            offset: Long,
        ): Result<List<WordOccurrence>> =
            if (strongsNumber == "H1254") Result.success(testOccurrences) else Result.success(emptyList())

        override suspend fun getOccurrenceCount(strongsNumber: String): Result<Long> =
            if (strongsNumber == "H1254") Result.success(3L) else Result.success(0L)

        override suspend fun getRelatedWords(strongsNumber: String): Result<List<LexiconEntry>> =
            Result.success(emptyList())

        override suspend fun searchLexicon(query: String, maxResults: Long): Result<List<LexiconEntry>> =
            Result.success(emptyList())
    }

    private fun createComponent(verseBus: VerseBus = VerseBus()): DefaultWordStudyComponent {
        val context = testComponentContext()
        return DefaultWordStudyComponent(
            componentContext = context,
            repository = fakeRepo,
            verseBus = verseBus
        )
    }

    @Test
    fun initialStateIsEmpty() {
        val component = createComponent()
        assertNull(component.state.value.entry)
        assertTrue(component.state.value.occurrences.isEmpty())
    }

    @Test
    fun strongsSelectedLoadsEntry() = runTest {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        verseBus.publish(LinkEvent.StrongsSelected("H1254"))

        // The component uses Dispatchers.Default internally, so we poll until loaded.
        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.entry == null && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        val state = component.state.value
        assertNotNull(state.entry)
        assertEquals("H1254", state.entry?.strongsNumber)
        assertEquals("bara", state.entry?.transliteration)
        assertEquals(3, state.occurrences.size)
        assertEquals(3, state.occurrenceCount)
    }

    @Test
    fun occurrenceSelectedPublishesVerseBus() {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        component.onOccurrenceSelected(1_001_001)

        val event = verseBus.current
        assertTrue(event is LinkEvent.VerseSelected)
        assertEquals(1_001_001, (event as LinkEvent.VerseSelected).globalVerseId)
    }
}
