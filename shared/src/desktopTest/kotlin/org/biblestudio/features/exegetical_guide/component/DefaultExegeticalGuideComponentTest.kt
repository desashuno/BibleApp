package org.biblestudio.features.exegetical_guide.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.features.cross_references.domain.repositories.CrossRefRepository
import org.biblestudio.features.exegetical_guide.domain.entities.CommentaryEntry
import org.biblestudio.features.exegetical_guide.domain.repositories.CommentaryRepository
import org.biblestudio.features.morphology_interlinear.domain.entities.WordOccurrence
import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.features.word_study.domain.entities.LexiconEntry
import org.biblestudio.features.word_study.domain.repositories.WordStudyRepository

class DefaultExegeticalGuideComponentTest {

    private val testCommentary = CommentaryEntry(1, "res1", 1001001, "This verse teaches love", 0)

    private val testCrossRef = CrossReference(1, 1001001, 43003016, "direct", 0.95)

    private val fakeCommentaryRepo = object : CommentaryRepository {
        override suspend fun getCommentaries() = Result.success(emptyList<Resource>())
        override suspend fun getEntriesForVerse(resourceId: String, globalVerseId: Long) =
            if (globalVerseId == 1001001L) Result.success(listOf(testCommentary)) else Result.success(emptyList())
        override suspend fun getAllEntriesForVerse(globalVerseId: Long) =
            if (globalVerseId == 1001001L) Result.success(listOf(testCommentary)) else Result.success(emptyList())
        override suspend fun search(query: String, maxResults: Long) = Result.success(emptyList<CommentaryEntry>())
    }

    private val fakeCrossRefRepo = object : CrossRefRepository {
        override suspend fun getRefsFromVerse(globalVerseId: Long) = Result.success(emptyList<CrossReference>())
        override suspend fun getRefsToVerse(globalVerseId: Long) = Result.success(emptyList<CrossReference>())
        override suspend fun getAllForVerse(globalVerseId: Long) =
            if (globalVerseId == 1001001L) Result.success(listOf(testCrossRef)) else Result.success(emptyList())
        override suspend fun loadTskData() = Result.success(0)
    }

    private val fakeWordStudyRepo = object : WordStudyRepository {
        override suspend fun lookupByStrongs(strongsNumber: String) = Result.success(null as LexiconEntry?)
        override suspend fun getOccurrences(strongsNumber: String) = Result.success(emptyList<WordOccurrence>())
        override suspend fun getOccurrenceCount(strongsNumber: String) = Result.success(0L)
        override suspend fun getRelatedWords(strongsNumber: String) = Result.success(emptyList<LexiconEntry>())
        override suspend fun searchLexicon(query: String, maxResults: Long) = Result.success(emptyList<LexiconEntry>())
    }

    private fun createComponent(verseBus: VerseBus = VerseBus()): DefaultExegeticalGuideComponent {
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle = lifecycle)
        return DefaultExegeticalGuideComponent(
            componentContext = context,
            commentaryRepository = fakeCommentaryRepo,
            crossRefRepository = fakeCrossRefRepo,
            wordStudyRepository = fakeWordStudyRepo,
            verseBus = verseBus
        )
    }

    @Test
    fun initialStateIsEmpty() {
        val component = createComponent()
        assertNull(component.state.value.globalVerseId)
        assertTrue(component.state.value.commentaries.isEmpty())
        assertTrue(component.state.value.crossReferences.isEmpty())
    }

    @Test
    fun verseSelectedLoadsGuide() = runTest {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        verseBus.publish(LinkEvent.VerseSelected(1001001))

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.commentaries.isEmpty() && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        val state = component.state.value
        assertEquals(1001001L, state.globalVerseId)
        assertEquals(1, state.commentaries.size)
        assertEquals("This verse teaches love", state.commentaries[0].content)
        assertEquals(1, state.crossReferences.size)
        assertEquals(43003016L, state.crossReferences[0].targetVerseId)
    }

    @Test
    fun directVerseSelectionLoadsGuide() = runTest {
        val component = createComponent()

        component.onVerseSelected(1001001L)

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.commentaries.isEmpty() && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertEquals(1, component.state.value.commentaries.size)
        assertEquals(1, component.state.value.crossReferences.size)
    }
}
