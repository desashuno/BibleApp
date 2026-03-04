package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.morphology_interlinear.component.AlignedToken
import org.biblestudio.features.morphology_interlinear.component.ReverseInterlinearState
import org.biblestudio.core.study.MorphWord

@OptIn(ExperimentalTestApi::class)
class ReverseInterlinearPaneTest {

    private val testMorphWord = MorphWord(
        id = 1,
        globalVerseId = 1_001_001,
        wordPosition = 1,
        surfaceForm = "בְּרֵאשִׁית",
        lemma = "reshith",
        strongsNumber = "H7225",
        parsingCode = "N-FSC",
        gloss = "beginning"
    )

    private val testTokens = listOf(
        AlignedToken("In", testMorphWord, "Noun, Feminine, Singular"),
        AlignedToken("the", null, null),
        AlignedToken("beginning", testMorphWord, "Noun, Feminine, Singular")
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun ReverseInterlinearPane_rendersTokens() = runComposeUiTest {
        val flow = MutableStateFlow(
            ReverseInterlinearState(
                alignedTokens = testTokens
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ReverseInterlinearPane(
                stateFlow = flow,
                onTokenSelected = {},
                onClearSelection = {}
            )
        }

        onNodeWithText("Reverse Interlinear").assertIsDisplayed()
        onNodeWithText("In").assertIsDisplayed()
        onNodeWithText("the").assertIsDisplayed()
        onNodeWithText("beginning").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun ReverseInterlinearPane_emptyStateShowsPrompt() = runComposeUiTest {
        val flow = MutableStateFlow(ReverseInterlinearState())

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ReverseInterlinearPane(
                stateFlow = flow,
                onTokenSelected = {},
                onClearSelection = {}
            )
        }

        onNodeWithText("Select a verse to view reverse interlinear").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun ReverseInterlinearPane_selectedTokenShowsPopover() = runComposeUiTest {
        val flow = MutableStateFlow(
            ReverseInterlinearState(
                alignedTokens = testTokens,
                selectedToken = testTokens[0]
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            ReverseInterlinearPane(
                stateFlow = flow,
                onTokenSelected = {},
                onClearSelection = {}
            )
        }

        // Popover card should show original word details
        onNodeWithText("בְּרֵאשִׁית").assertIsDisplayed()
        onNodeWithText("reshith").assertIsDisplayed()
    }
}
