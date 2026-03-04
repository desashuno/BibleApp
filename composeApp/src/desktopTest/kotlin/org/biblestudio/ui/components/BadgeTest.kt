package org.biblestudio.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class BadgeTest {

    @Test
    fun `Badge displays text`() = runComposeUiTest {
        setContent {
            Badge(text = "Bible", color = Color.Blue)
        }
        onNodeWithText("Bible").assertIsDisplayed()
    }

    @Test
    fun `StatusBadge displays text`() = runComposeUiTest {
        setContent {
            StatusBadge(text = "Active", statusColor = Color.Green)
        }
        onNodeWithText("Active").assertIsDisplayed()
    }
}
