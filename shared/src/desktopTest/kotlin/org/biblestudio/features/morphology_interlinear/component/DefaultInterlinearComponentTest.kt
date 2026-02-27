package org.biblestudio.features.morphology_interlinear.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.morphology_interlinear.domain.ParsingDecoder
import org.biblestudio.features.morphology_interlinear.domain.entities.AlignmentEntry
import org.biblestudio.features.morphology_interlinear.domain.entities.MorphWord
import org.biblestudio.features.morphology_interlinear.domain.entities.MorphologyData
import org.biblestudio.features.morphology_interlinear.domain.entities.WordOccurrence
import org.biblestudio.features.morphology_interlinear.domain.repositories.MorphologyRepository

class DefaultInterlinearComponentTest {

    private val testWords = listOf(
        MorphWord(1, 1_001_001, 1, "בְּרֵאשִׁית", "reshith", "H7225", "N-FSC", "beginning"),
        MorphWord(2, 1_001_001, 2, "בָּרָא", "bara", "H1254", "V-QAL-3MS", "created")
    )

    private val fakeRepo = object : MorphologyRepository {
        override suspend fun getMorphologyForVerse(globalVerseId: Long): Result<List<MorphologyData>> =
            Result.success(emptyList())

        override suspend fun getMorphWords(globalVerseId: Long): Result<List<MorphWord>> =
            if (globalVerseId == 1_001_001L) Result.success(testWords) else Result.success(emptyList())

        override suspend fun getWordsByStrongs(strongsNumber: String): Result<List<MorphWord>> =
            Result.success(emptyList())

        override suspend fun getOccurrences(strongsNumber: String): Result<List<WordOccurrence>> =
            Result.success(emptyList())

        override suspend fun getOccurrenceCount(strongsNumber: String): Result<Long> = Result.success(0L)

        override suspend fun getAlignmentForVerse(globalVerseId: Long): Result<List<AlignmentEntry>> =
            Result.success(emptyList())
    }

    private fun createComponent(verseBus: VerseBus = VerseBus()): DefaultInterlinearComponent {
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle = lifecycle)
        return DefaultInterlinearComponent(
            componentContext = context,
            repository = fakeRepo,
            parsingDecoder = ParsingDecoder(),
            verseBus = verseBus
        )
    }

    @Test
    fun initialStateIsEmpty() {
        val component = createComponent()
        assertTrue(component.state.value.words.isEmpty())
    }

    @Test
    fun verseSelectedLoadsMorphology() = runTest {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        verseBus.publish(LinkEvent.VerseSelected(1_001_001))

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.words.isEmpty() && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        val state = component.state.value
        assertEquals(2, state.words.size)
        assertEquals("בְּרֵאשִׁית", state.words[0].surfaceForm)
        assertNotNull(state.decodedParsings["V-QAL-3MS"])
        assertTrue(state.decodedParsings["V-QAL-3MS"]!!.contains("Verb"))
    }

    @Test
    fun wordSelectedPublishesStrongsEvent() {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        component.onWordSelected(testWords[0])

        val event = verseBus.current
        assertTrue(event is LinkEvent.StrongsSelected)
        assertEquals("H7225", (event as LinkEvent.StrongsSelected).strongsNumber)
    }

    @Test
    fun displayModeChangesState() {
        val component = createComponent()
        assertEquals(InterlinearDisplayMode.Interlinear, component.state.value.displayMode)

        component.onDisplayModeChanged(InterlinearDisplayMode.Parallel)
        assertEquals(InterlinearDisplayMode.Parallel, component.state.value.displayMode)
    }
}
