package org.biblestudio.features.resource_library.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry
import org.biblestudio.features.resource_library.domain.repositories.ResourceRepository

class DefaultResourceLibraryComponentTest {

    private val testResource = Resource(
        uuid = "res-1",
        type = "commentary",
        title = "Test Commentary",
        author = "Author",
        version = "1.0",
        format = "json",
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z",
        deviceId = "dev-1"
    )

    private val testEntry = ResourceEntry(
        id = 1,
        resourceId = "res-1",
        globalVerseId = 1_001_001,
        content = "Commentary on Gen 1:1",
        sortOrder = 0
    )

    private val fakeRepo = object : ResourceRepository {
        override suspend fun getAllResources(): Result<List<Resource>> = Result.success(listOf(testResource))

        override suspend fun getByType(type: String): Result<List<Resource>> =
            Result.success(listOf(testResource).filter { it.type == type })

        override suspend fun getByUuid(uuid: String): Result<Resource?> =
            Result.success(if (uuid == "res-1") testResource else null)

        override suspend fun getEntriesForVerse(resourceId: String, globalVerseId: Long): Result<List<ResourceEntry>> =
            if (resourceId == "res-1" && globalVerseId == 1_001_001L) {
                Result.success(listOf(testEntry))
            } else {
                Result.success(emptyList())
            }

        override suspend fun getAllEntries(resourceId: String): Result<List<ResourceEntry>> =
            Result.success(listOf(testEntry))

        override suspend fun searchEntries(query: String, maxResults: Long): Result<List<ResourceEntry>> =
            Result.success(emptyList())
    }

    private fun createComponent(verseBus: VerseBus = VerseBus()): DefaultResourceLibraryComponent {
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle = lifecycle)
        return DefaultResourceLibraryComponent(
            componentContext = context,
            repository = fakeRepo,
            verseBus = verseBus
        )
    }

    @Test
    fun initialStateIsEmpty() {
        val component = createComponent()
        assertNull(component.state.value.activeResource)
        assertTrue(component.state.value.searchResults.isEmpty())
    }

    @Test
    fun entryVerseSelectedPublishesVerseBus() {
        val verseBus = VerseBus()
        val component = createComponent(verseBus)

        component.onEntryVerseSelected(1_001_001)

        val event = verseBus.current
        assertTrue(event is LinkEvent.VerseSelected)
    }

    @Test
    fun resourcesLoadOnInit() = runTest {
        val component = createComponent()

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.resources.isEmpty() &&
            System.currentTimeMillis() - start < timeout
        ) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertTrue(component.state.value.resources.isNotEmpty())
    }
}
