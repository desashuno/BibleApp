package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleStatus
import org.biblestudio.core.data_manager.model.DataModuleType
import org.biblestudio.features.resource_library.component.ResourceLibraryState

@OptIn(ExperimentalTestApi::class)
class ResourceLibraryPaneTest {

    private val testModule = DataModuleDescriptor(
        moduleId = "bible-kjv",
        name = "King James Version",
        description = "KJV Bible",
        type = DataModuleType.Bible,
        status = DataModuleStatus.Installed,
        language = "en"
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun ResourceLibraryPane_rendersModuleList() = runComposeUiTest {
        val flow = MutableStateFlow(
            ResourceLibraryState(
                modules = listOf(testModule),
                filteredModules = listOf(testModule),
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ResourceLibraryPane(
                stateFlow = flow,
                onModuleSelected = {},
                onInstallModule = {},
                onRemoveModule = {},
                onCancelDownload = {},
                onFilterTypeChanged = {},
                onSearchQueryChanged = {},
                onToggleModuleActive = {}
            )
        }

        onNodeWithText("Resource Library").assertIsDisplayed()
        onNodeWithText("King James Version").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun ResourceLibraryPane_showsEmptyWhenNoModules() = runComposeUiTest {
        val flow = MutableStateFlow(ResourceLibraryState())

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ResourceLibraryPane(
                stateFlow = flow,
                onModuleSelected = {},
                onInstallModule = {},
                onRemoveModule = {},
                onCancelDownload = {},
                onFilterTypeChanged = {},
                onSearchQueryChanged = {},
                onToggleModuleActive = {}
            )
        }

        onNodeWithText("Resource Library").assertIsDisplayed()
        onNodeWithText("No modules found").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun ResourceLibraryPane_showsFilterChips() = runComposeUiTest {
        val flow = MutableStateFlow(ResourceLibraryState())

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ResourceLibraryPane(
                stateFlow = flow,
                onModuleSelected = {},
                onInstallModule = {},
                onRemoveModule = {},
                onCancelDownload = {},
                onFilterTypeChanged = {},
                onSearchQueryChanged = {},
                onToggleModuleActive = {}
            )
        }

        onNodeWithText("All").assertIsDisplayed()
        onNodeWithText("Bible").assertIsDisplayed()
        onNodeWithText("Commentary").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun ResourceLibraryPane_showsActiveToggle() = runComposeUiTest {
        val activeModule = testModule.copy(isActive = true)
        val flow = MutableStateFlow(
            ResourceLibraryState(
                modules = listOf(activeModule),
                filteredModules = listOf(activeModule),
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ResourceLibraryPane(
                stateFlow = flow,
                onModuleSelected = {},
                onInstallModule = {},
                onRemoveModule = {},
                onCancelDownload = {},
                onFilterTypeChanged = {},
                onSearchQueryChanged = {},
                onToggleModuleActive = {}
            )
        }

        onNodeWithText("King James Version").assertIsDisplayed()
        onNodeWithText("Active").assertIsDisplayed()
    }
}
