package org.biblestudio.features.passage_guide.component

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
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.passage_guide.domain.entities.Outline
import org.biblestudio.features.passage_guide.domain.entities.PassageReport
import org.biblestudio.features.passage_guide.domain.repositories.PassageGuideRepository
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry
import org.biblestudio.features.word_study.domain.entities.LexiconEntry

class DefaultPassageGuideComponentTest {

    private val testReport = PassageReport(
        verseId = 1_001_001,
        verseText = "In the beginning God created the heavens and the earth.",
        crossReferences = listOf(
            CrossReference(1, 1_001_001, 43_001_001, "parallel", 0.95)
        ),
        outlines = listOf(
            Outline(1, 1_001_001, 1_001_031, "Creation Account", "Gen 1:1-31", "Study Bible")
        ),
        keyWords = listOf(
            LexiconEntry("H1254", "בָּרָא", "bara", "to create", null)
        ),
        commentaryEntries = listOf(
            ResourceEntry(1, "res-1", 1_001_001, "Commentary on Gen 1:1", 1)
        ),
        userNotes = listOf(
            Note(
                "note-1",
                1_001_001,
                "My Note",
                "Content",
                createdAt = "2024-01-01",
                updatedAt = "2024-01-01",
                deviceId = "dev1"
            )
        ),
        morphologyWords = emptyList()
    )

    private val fakeRepo = object : PassageGuideRepository {
        override suspend fun buildReport(globalVerseId: Long): Result<PassageReport> =
            if (globalVerseId == 1_001_001L) {
                Result.success(testReport)
            } else {
                Result.success(
                    PassageReport(
                        globalVerseId,
                        "",
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList(),
                        emptyList()
                    )
                )
            }
    }

    private fun createComponent(verseBus: VerseBus = VerseBus()): DefaultPassageGuideComponent {
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle = lifecycle)
        return DefaultPassageGuideComponent(
            componentContext = context,
            repository = fakeRepo,
            verseBus = verseBus
        )
    }

    @Test
    fun initialStateHasNoReport() {
        val component = createComponent()
        assertNull(component.state.value.report)
    }

    @Test
    fun verseSelectedBuildsReport() = runTest {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        verseBus.publish(LinkEvent.VerseSelected(1_001_001))

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.report == null && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        val report = component.state.value.report
        assertNotNull(report)
        assertEquals(1_001_001L, report.verseId)
        assertEquals(1, report.crossReferences.size)
        assertEquals(1, report.outlines.size)
        assertEquals(1, report.keyWords.size)
        assertEquals(1, report.commentaryEntries.size)
        assertEquals(1, report.userNotes.size)
    }

    @Test
    fun refSelectedPublishesVerseSelected() {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        val crossRef = CrossReference(1, 1_001_001, 43_001_001, "parallel", 0.95)
        component.onRefSelected(crossRef)

        val event = verseBus.current
        assertTrue(event is LinkEvent.VerseSelected)
        assertEquals(43_001_001, (event as LinkEvent.VerseSelected).globalVerseId)
    }

    @Test
    fun wordSelectedPublishesStrongsSelected() {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        component.onWordSelected("H1254")

        val event = verseBus.current
        assertTrue(event is LinkEvent.StrongsSelected)
        assertEquals("H1254", (event as LinkEvent.StrongsSelected).strongsNumber)
    }

    @Test
    fun sectionToggleExpandsAndCollapses() {
        val component = createComponent()

        // "crossRefs" is expanded by default
        assertTrue(component.state.value.expandedSections.contains("crossRefs"))

        // Toggle off
        component.onSectionToggle("crossRefs")
        assertTrue(!component.state.value.expandedSections.contains("crossRefs"))

        // Toggle back on
        component.onSectionToggle("crossRefs")
        assertTrue(component.state.value.expandedSections.contains("crossRefs"))
    }
}
