package org.biblestudio.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Box
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoadingIndicatorTest {

    @Test
    fun `LoadingIndicator renders without crashing`() = runComposeUiTest {
        setContent {
            Box(modifier = Modifier.testTag("wrapper")) {
                LoadingIndicator()
            }
        }
        onNodeWithTag("wrapper").assertIsDisplayed()
    }

    @Test
    fun `LoadingIndicator fullScreen renders without crashing`() = runComposeUiTest {
        setContent {
            Box(modifier = Modifier.testTag("wrapper")) {
                LoadingIndicator(fullScreen = true)
            }
        }
        onNodeWithTag("wrapper").assertIsDisplayed()
    }
}
