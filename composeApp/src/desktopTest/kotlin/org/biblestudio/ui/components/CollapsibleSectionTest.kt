package org.biblestudio.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CollapsibleSectionTest {

    @Test
    fun `CollapsibleSectionHeader shows expanded indicator when expanded`() = runComposeUiTest {
        setContent {
            CollapsibleSectionHeader(title = "Notes", expanded = true, onClick = {})
        }
        onNodeWithText("▼ Notes").assertIsDisplayed()
    }

    @Test
    fun `CollapsibleSectionHeader shows collapsed indicator when collapsed`() = runComposeUiTest {
        setContent {
            CollapsibleSectionHeader(title = "Notes", expanded = false, onClick = {})
        }
        onNodeWithText("▶ Notes").assertIsDisplayed()
    }

    @Test
    fun `CollapsibleSectionHeader invokes onClick`() = runComposeUiTest {
        var clicked = false
        setContent {
            CollapsibleSectionHeader(title = "Notes", expanded = false, onClick = { clicked = true })
        }
        onNodeWithText("▶ Notes").performClick()
        assertTrue(clicked)
    }
}
