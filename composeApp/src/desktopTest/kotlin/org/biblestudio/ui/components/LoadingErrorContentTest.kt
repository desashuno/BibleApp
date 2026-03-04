package org.biblestudio.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoadingErrorContentTest {

    @Test
    fun `shows empty message when data is null`() = runComposeUiTest {
        setContent {
            LoadingErrorContent(
                isLoading = false,
                error = null,
                data = null as String?,
                emptyMessage = "Nothing here",
            ) { value ->
                Text(value)
            }
        }
        onNodeWithText("Nothing here").assertIsDisplayed()
    }

    @Test
    fun `shows error message when error is set`() = runComposeUiTest {
        setContent {
            LoadingErrorContent(
                isLoading = false,
                error = "Network failure",
                data = null as String?,
                emptyMessage = "Nothing",
            ) { value ->
                Text(value)
            }
        }
        onNodeWithText("Network failure").assertIsDisplayed()
    }

    @Test
    fun `shows content when data is present`() = runComposeUiTest {
        setContent {
            LoadingErrorContent(
                isLoading = false,
                error = null,
                data = "Hello world",
                emptyMessage = "Nothing",
            ) { value ->
                Text(value)
            }
        }
        onNodeWithText("Hello world").assertIsDisplayed()
    }

    @Test
    fun `shows empty when isEmpty predicate returns true`() = runComposeUiTest {
        setContent {
            LoadingErrorContent(
                isLoading = false,
                error = null,
                data = emptyList<String>(),
                emptyMessage = "No items",
                isEmpty = { it.isEmpty() },
            ) { items ->
                Text("Count: ${items.size}")
            }
        }
        onNodeWithText("No items").assertIsDisplayed()
    }
}
