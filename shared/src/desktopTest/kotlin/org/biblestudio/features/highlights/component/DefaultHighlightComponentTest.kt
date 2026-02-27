package org.biblestudio.features.highlights.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.features.highlights.domain.entities.HighlightColor
import org.biblestudio.features.highlights.domain.repositories.HighlightRepository

class DefaultHighlightComponentTest {

    private val storedHighlights = mutableListOf<Highlight>()

    @Suppress("TooManyFunctions")
    private val fakeRepo = object : HighlightRepository {
        override suspend fun getHighlightsForVerse(globalVerseId: Long): Result<List<Highlight>> =
            Result.success(storedHighlights.filter { it.globalVerseId == globalVerseId })

        override suspend fun getAll(): Result<List<Highlight>> = Result.success(storedHighlights.toList())

        override suspend fun getHighlightsForVerseRange(startVerseId: Long, endVerseId: Long): Result<List<Highlight>> =
            Result.success(
                storedHighlights.filter {
                    it.globalVerseId in startVerseId..endVerseId
                }
            )

        override suspend fun create(highlight: Highlight): Result<Unit> {
            storedHighlights.add(highlight)
            return Result.success(Unit)
        }

        override suspend fun update(highlight: Highlight): Result<Unit> {
            val idx = storedHighlights.indexOfFirst { it.uuid == highlight.uuid }
            if (idx >= 0) storedHighlights[idx] = highlight
            return Result.success(Unit)
        }

        override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> {
            storedHighlights.removeAll { it.uuid == uuid }
            return Result.success(Unit)
        }

        override fun watchHighlightsForVerse(globalVerseId: Long): Flow<List<Highlight>> = emptyFlow()
    }

    private fun createComponent(verseBus: VerseBus = VerseBus()): DefaultHighlightComponent {
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle = lifecycle)
        return DefaultHighlightComponent(
            componentContext = context,
            repository = fakeRepo,
            verseBus = verseBus
        )
    }

    @Test
    fun initialStateDefaultsToYellow() {
        val component = createComponent()
        assertEquals(HighlightColor.Yellow, component.state.value.selectedColor)
        assertTrue(component.state.value.highlights.isEmpty())
    }

    @Test
    fun colorSelectionUpdatesState() {
        val component = createComponent()
        component.onColorSelected(HighlightColor.Blue)
        assertEquals(HighlightColor.Blue, component.state.value.selectedColor)
    }

    @Test
    fun highlightVerseCreatesEntry() = runTest {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        // Simulate selecting a verse first
        verseBus.publish(LinkEvent.VerseSelected(1_001_001))

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.isLoading && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        component.onHighlightVerse(1_001_001)

        // Wait for creation
        val start2 = System.currentTimeMillis()
        while (storedHighlights.isEmpty() && System.currentTimeMillis() - start2 < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertEquals(1, storedHighlights.size)
        assertEquals(1_001_001L, storedHighlights.first().globalVerseId)
    }
}
