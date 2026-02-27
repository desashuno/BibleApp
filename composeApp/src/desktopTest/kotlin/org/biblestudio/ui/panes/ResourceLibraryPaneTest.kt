package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.resource_library.component.ResourceLibraryState
import org.biblestudio.features.resource_library.domain.entities.Resource

@OptIn(ExperimentalTestApi::class)
class ResourceLibraryPaneTest {

    private val testResource = Resource(
        uuid = "res-1",
        type = "commentary",
        title = "Matthew Henry Commentary",
        author = "Matthew Henry",
        version = "1.0",
        format = "json",
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z",
        deviceId = "dev-1"
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun ResourceLibraryPane_rendersResourceList() = runComposeUiTest {
        val flow = MutableStateFlow(
            ResourceLibraryState(
                resources = listOf(testResource),
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ResourceLibraryPane(
                stateFlow = flow,
                onResourceSelected = {},
                onSearchQueryChanged = {},
                onEntryVerseSelected = {}
            )
        }

        onNodeWithText("Resources").assertIsDisplayed()
        onNodeWithText("Matthew Henry Commentary").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun ResourceLibraryPane_showsEmptyWhenNoResources() = runComposeUiTest {
        val flow = MutableStateFlow(ResourceLibraryState())

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ResourceLibraryPane(
                stateFlow = flow,
                onResourceSelected = {},
                onSearchQueryChanged = {},
                onEntryVerseSelected = {}
            )
        }

        onNodeWithText("Resources").assertIsDisplayed()
    }
}
