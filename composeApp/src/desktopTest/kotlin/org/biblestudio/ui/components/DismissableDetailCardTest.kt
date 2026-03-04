package org.biblestudio.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DismissableDetailCardTest {

    @Test
    fun `DismissableDetailCard shows title and content`() = runComposeUiTest {
        setContent {
            DismissableDetailCard(title = "Entity", onDismiss = {}) {
                Text("Body text")
            }
        }
        onNodeWithText("Entity").assertIsDisplayed()
        onNodeWithText("Body text").assertIsDisplayed()
    }

    @Test
    fun `DismissableDetailCard invokes onDismiss`() = runComposeUiTest {
        var dismissed = false
        setContent {
            DismissableDetailCard(title = "Entity", onDismiss = { dismissed = true }) {
                Text("Body")
            }
        }
        onNodeWithText("✕").performClick()
        assertTrue(dismissed)
    }
}
