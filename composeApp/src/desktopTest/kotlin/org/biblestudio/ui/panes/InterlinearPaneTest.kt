package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.morphology_interlinear.component.InterlinearDisplayMode
import org.biblestudio.features.morphology_interlinear.component.InterlinearState
import org.biblestudio.core.study.MorphWord

@OptIn(ExperimentalTestApi::class)
class InterlinearPaneTest {

    private val testWords = listOf(
        MorphWord(1, 1_001_001, 1, "בְּרֵאשִׁית", "reshith", "H7225", "N-FSC", "beginning"),
        MorphWord(2, 1_001_001, 2, "בָּרָא", "bara", "H1254", "V-QAL-3MS", "created")
    )

    private val testParsings = mapOf(
        "N-FSC" to "Noun, Feminine, Singular, Construct",
        "V-QAL-3MS" to "Verb, Qal, 3rd Person, Masculine, Singular"
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun InterlinearPane_rendersWordGrid() = runComposeUiTest {
        val flow = MutableStateFlow(
            InterlinearState(
                words = testWords,
                decodedParsings = testParsings,
                displayMode = InterlinearDisplayMode.Interlinear
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            InterlinearPane(
                stateFlow = flow,
                onWordSelected = {},
                onDisplayModeChanged = {}
            )
        }

        onNodeWithText("בְּרֵאשִׁית").assertIsDisplayed()
        onNodeWithText("reshith").assertIsDisplayed()
        onNodeWithText("beginning").assertIsDisplayed()
        onNodeWithText("בָּרָא").assertIsDisplayed()
        onNodeWithText("bara").assertIsDisplayed()
        onNodeWithText("created").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun InterlinearPane_emptyStateShowsPrompt() = runComposeUiTest {
        val flow = MutableStateFlow(InterlinearState())

        setContent {
            @Suppress("ktlint:standard:function-naming")
            InterlinearPane(
                stateFlow = flow,
                onWordSelected = {},
                onDisplayModeChanged = {}
            )
        }

        onNodeWithText("Select a verse to view interlinear").assertIsDisplayed()
    }
}
