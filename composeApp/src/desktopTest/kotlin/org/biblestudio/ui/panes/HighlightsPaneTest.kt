package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.highlights.component.HighlightState
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.features.highlights.domain.entities.HighlightColor

@OptIn(ExperimentalTestApi::class)
class HighlightsPaneTest {

    private val testHighlight = Highlight(
        uuid = "hl-1",
        globalVerseId = 1_001_001,
        colorIndex = 0,
        style = "background",
        startOffset = 0,
        endOffset = -1,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z",
        deviceId = "dev-1"
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun HighlightsPane_rendersHighlightList() = runComposeUiTest {
        val flow = MutableStateFlow(
            HighlightState(
                highlights = listOf(testHighlight),
                selectedColor = HighlightColor.Yellow,
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            HighlightsPane(
                stateFlow = flow,
                onColorSelected = {},
                onDeleteHighlight = {}
            )
        }

        onNodeWithText("Highlights").assertIsDisplayed()
        onNodeWithText("Genesis 1:1").assertIsDisplayed()
        onNodeWithText("Whole verse").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun HighlightsPane_showsEmptyMessage() = runComposeUiTest {
        val flow = MutableStateFlow(HighlightState())

        setContent {
            @Suppress("ktlint:standard:function-naming")
            HighlightsPane(
                stateFlow = flow,
                onColorSelected = {},
                onDeleteHighlight = {}
            )
        }

        onNodeWithText("Highlights").assertIsDisplayed()
        onNodeWithText("No highlights for this verse").assertIsDisplayed()
    }
}
