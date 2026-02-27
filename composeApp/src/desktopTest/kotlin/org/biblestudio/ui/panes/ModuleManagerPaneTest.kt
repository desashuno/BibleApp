package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.module_system.component.ModuleManagerState
import org.biblestudio.features.module_system.domain.entities.InstalledModule

@OptIn(ExperimentalTestApi::class)
class ModuleManagerPaneTest {

    private val sampleModule = InstalledModule(
        id = 1,
        uuid = "abc-123",
        name = "King James Version",
        abbreviation = "KJV",
        language = "en",
        type = "bible",
        version = "1.0",
        sizeBytes = 4_500_000,
        description = "The KJV Bible",
        sourceType = "osis",
        installedAt = "2024-01-01",
        isActive = true
    )

    @Test
    fun moduleListRendersInstalledModules() = runComposeUiTest {
        val flow = MutableStateFlow(
            ModuleManagerState(
                installedModules = listOf(sampleModule),
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ModuleManagerPane(
                stateFlow = flow,
                onModuleSelected = {},
                onRemoveModule = {},
                onImportModule = {}
            )
        }

        onNodeWithText("King James Version").assertIsDisplayed()
        onNodeWithText("KJV • en").assertIsDisplayed()
    }

    @Test
    fun emptyStateShowsPlaceholder() = runComposeUiTest {
        val flow = MutableStateFlow(
            ModuleManagerState(installedModules = emptyList(), isLoading = false)
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ModuleManagerPane(
                stateFlow = flow,
                onModuleSelected = {},
                onRemoveModule = {},
                onImportModule = {}
            )
        }

        onNodeWithText("No modules installed. Import a module to get started.").assertIsDisplayed()
    }

    @Test
    fun importButtonIsVisible() = runComposeUiTest {
        val flow = MutableStateFlow(ModuleManagerState(isLoading = false))

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ModuleManagerPane(
                stateFlow = flow,
                onModuleSelected = {},
                onRemoveModule = {},
                onImportModule = {}
            )
        }

        onNodeWithText("Import Module").assertIsDisplayed()
    }
}
