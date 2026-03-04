package org.biblestudio.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isToggleable
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SettingRowsTest {

    @Test
    fun `ToggleRow shows title and subtitle`() = runComposeUiTest {
        setContent {
            ToggleRow(
                title = "Dark mode",
                subtitle = "Use dark theme",
                checked = true,
                onCheckedChange = {},
            )
        }
        onNodeWithText("Dark mode").assertIsDisplayed()
        onNodeWithText("Use dark theme").assertIsDisplayed()
        onNode(isToggleable()).assertIsOn()
    }

    @Test
    fun `ToggleRow reflects unchecked state`() = runComposeUiTest {
        setContent {
            ToggleRow(
                title = "Dark mode",
                subtitle = "Use dark theme",
                checked = false,
                onCheckedChange = {},
            )
        }
        onNode(isToggleable()).assertIsOff()
    }

    @Test
    fun `SettingRow shows title and subtitle`() = runComposeUiTest {
        setContent {
            SettingRow(title = "Version", subtitle = "1.0.0")
        }
        onNodeWithText("Version").assertIsDisplayed()
        onNodeWithText("1.0.0").assertIsDisplayed()
    }
}
