package org.biblestudio.features.morphology_interlinear.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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

class DefaultReverseInterlinearComponentTest {

    private val testAlignments = listOf(
        AlignmentEntry(1, 1_001_001, 1, "In", 1, "H7225"),
        AlignmentEntry(2, 1_001_001, 2, "the beginning", 1, "H7225"),
        AlignmentEntry(3, 1_001_001, 3, "God", 3, "H430"),
        AlignmentEntry(4, 1_001_001, 4, "created", 2, "H1254")
    )

    private val testMorphWords = listOf(
        MorphWord(1, 1_001_001, 1, "בְּרֵאשִׁית", "reshith", "H7225", "N-FSC", "beginning"),
        MorphWord(2, 1_001_001, 2, "בָּרָא", "bara", "H1254", "V-QAL-3MS", "created"),
        MorphWord(3, 1_001_001, 3, "אֱלֹהִים", "elohim", "H430", "N-MPC", "God")
    )

    private val fakeRepo = object : MorphologyRepository {
        override suspend fun getMorphologyForVerse(globalVerseId: Long): Result<List<MorphologyData>> =
            Result.success(emptyList())

        override suspend fun getMorphWords(globalVerseId: Long): Result<List<MorphWord>> =
            if (globalVerseId == 1_001_001L) Result.success(testMorphWords) else Result.success(emptyList())

        override suspend fun getWordsByStrongs(strongsNumber: String): Result<List<MorphWord>> =
            Result.success(emptyList())

        override suspend fun getOccurrences(strongsNumber: String): Result<List<WordOccurrence>> =
            Result.success(emptyList())

        override suspend fun getOccurrenceCount(strongsNumber: String): Result<Long> = Result.success(0L)

        override suspend fun getAlignmentForVerse(globalVerseId: Long): Result<List<AlignmentEntry>> =
            if (globalVerseId == 1_001_001L) Result.success(testAlignments) else Result.success(emptyList())
    }

    private fun createComponent(verseBus: VerseBus = VerseBus()): DefaultReverseInterlinearComponent {
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle = lifecycle)
        return DefaultReverseInterlinearComponent(
            componentContext = context,
            repository = fakeRepo,
            parsingDecoder = ParsingDecoder(),
            verseBus = verseBus
        )
    }

    @Test
    fun initialStateIsEmpty() {
        val component = createComponent()
        assertTrue(component.state.value.alignedTokens.isEmpty())
        assertNull(component.state.value.selectedToken)
    }

    @Test
    fun verseSelectedLoadsAlignedTokens() = runTest {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        verseBus.publish(LinkEvent.VerseSelected(1_001_001))

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (
            component.state.value.alignedTokens.isEmpty() &&
            System.currentTimeMillis() - start < timeout
        ) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        val tokens = component.state.value.alignedTokens
        assertEquals(4, tokens.size)
        assertEquals("In", tokens[0].englishToken)
        assertNotNull(tokens[0].morphWord)
        assertEquals("H7225", tokens[0].morphWord?.strongsNumber)
        assertEquals("created", tokens[3].englishToken)
        assertEquals("H1254", tokens[3].morphWord?.strongsNumber)
    }

    @Test
    fun tokenSelectedPublishesStrongsAndSetsSelection() {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        val token = AlignedToken(
            englishToken = "created",
            morphWord = testMorphWords[1],
            decodedParsing = "Verb, Qal"
        )

        component.onTokenSelected(token)

        val state = component.state.value
        assertNotNull(state.selectedToken)
        assertEquals("created", state.selectedToken?.englishToken)

        val event = verseBus.current
        assertTrue(event is LinkEvent.StrongsSelected)
        assertEquals("H1254", (event as LinkEvent.StrongsSelected).strongsNumber)
    }

    @Test
    fun clearSelectionRemovesSelectedToken() {
        val component = createComponent()
        val token = AlignedToken(englishToken = "test", morphWord = null)

        component.onTokenSelected(token)
        assertNotNull(component.state.value.selectedToken)

        component.clearSelection()
        assertNull(component.state.value.selectedToken)
    }
}
