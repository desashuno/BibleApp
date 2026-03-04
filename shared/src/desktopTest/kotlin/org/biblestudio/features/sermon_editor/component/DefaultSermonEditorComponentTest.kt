package org.biblestudio.features.sermon_editor.component

import org.biblestudio.test.testComponentContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.sermon_editor.domain.entities.Sermon
import org.biblestudio.features.sermon_editor.domain.entities.SermonSection
import org.biblestudio.features.sermon_editor.domain.repositories.SermonRepository

class DefaultSermonEditorComponentTest {

    private val testSermon = Sermon(
        uuid = "s1",
        title = "Test Sermon",
        scriptureRef = "John 3:16",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        status = "draft",
        deviceId = ""
    )

    private val testSections = listOf(
        SermonSection(1, "s1", "introduction", "Welcome to today's message", 0),
        SermonSection(2, "s1", "point", "The main point is very clear", 1)
    )

    private val fakeRepo = object : SermonRepository {
        private val sermons = mutableListOf(testSermon)
        private val sections = mutableListOf<SermonSection>().apply { addAll(testSections) }

        override suspend fun getAll(): Result<List<Sermon>> = Result.success(sermons.toList())
        override suspend fun getByStatus(status: String) = Result.success(sermons.filter { it.status == status })
        override suspend fun getByUuid(uuid: String) = Result.success(sermons.firstOrNull { it.uuid == uuid })
        override suspend fun create(sermon: Sermon) = Result.success(Unit).also { sermons.add(sermon) }
        override suspend fun update(sermon: Sermon) = Result.success(Unit).also { idx ->
            sermons.indexOfFirst { it.uuid == sermon.uuid }.let { i -> if (i >= 0) sermons[i] = sermon }
        }
        override suspend fun delete(uuid: String, deletedAt: String) = Result.success(Unit).also {
            sermons.removeAll { it.uuid == uuid }
        }
        override suspend fun getSections(sermonId: String) = Result.success(sections.filter { it.sermonId == sermonId })
        override suspend fun createSection(section: SermonSection) = Result.success(Unit).also { sections.add(section) }
        override suspend fun updateSection(section: SermonSection) = Result.success(Unit).also {
            val idx = sections.indexOfFirst { it.id == section.id }
            if (idx >= 0) sections[idx] = section
        }
        override suspend fun deleteSection(sectionId: Long) = Result.success(Unit).also {
            sections.removeAll { it.id == sectionId }
        }
        override suspend fun deleteAllSections(sermonId: String) = Result.success(Unit).also {
            sections.removeAll { it.sermonId == sermonId }
        }
        override fun watchAll(): Flow<List<Sermon>> = flowOf(sermons.toList())
    }

    private fun createComponent(): DefaultSermonEditorComponent {
        val context = testComponentContext()
        return DefaultSermonEditorComponent(
            componentContext = context,
            repository = fakeRepo,
            verseBus = VerseBus()
        )
    }

    @Test
    fun initialStateLoadsSermons() = runTest {
        val component = createComponent()

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.sermons.isEmpty() && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertEquals(1, component.state.value.sermons.size)
        assertNull(component.state.value.activeSermon)
    }

    @Test
    fun selectSermonLoadsSections() = runTest {
        val component = createComponent()

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.sermons.isEmpty() && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        component.onSermonSelected("s1")

        val start2 = System.currentTimeMillis()
        while (component.state.value.activeSermon == null && System.currentTimeMillis() - start2 < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        val state = component.state.value
        assertNotNull(state.activeSermon)
        assertEquals("Test Sermon", state.editTitle)
        assertEquals("John 3:16", state.editScriptureRef)
        assertEquals(2, state.sections.size)
        assertTrue(state.wordCount > 0)
    }

    @Test
    fun wordCountCalculation() {
        val sections = listOf(
            SermonSection(1, "s1", "point", "one two three", 0),
            SermonSection(2, "s1", "point", "four five", 1)
        )
        assertEquals(5, DefaultSermonEditorComponent.computeWordCount(sections))
    }

    @Test
    fun estimateMinutesCalculation() {
        assertEquals(0, DefaultSermonEditorComponent.estimateMinutes(0))
        assertEquals(1, DefaultSermonEditorComponent.estimateMinutes(100))
        assertEquals(1, DefaultSermonEditorComponent.estimateMinutes(150))
        assertEquals(2, DefaultSermonEditorComponent.estimateMinutes(151))
    }
}
