package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.cross_references.component.CrossReferenceState
import org.biblestudio.features.cross_references.domain.entities.CrossReference

@OptIn(ExperimentalTestApi::class)
class CrossReferencePaneTest {

    private val testRefs = listOf(
        CrossReference(
            id = 1,
            sourceVerseId = 1_001_001,
            targetVerseId = 43_001_001,
            type = "parallel",
            confidence = 0.95
        ),
        CrossReference(
            id = 2,
            sourceVerseId = 1_001_001,
            targetVerseId = 58_011_003,
            type = "quotation",
            confidence = 0.88
        )
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun CrossReferencePane_rendersReferenceList() = runComposeUiTest {
        val flow = MutableStateFlow(
            CrossReferenceState(
                sourceVerseId = 1_001_001,
                references = testRefs,
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            CrossReferencePane(
                stateFlow = flow,
                onReferenceTapped = {},
                onToggleExpansion = {}
            )
        }

        onNodeWithText("Cross-References for 1001001").assertIsDisplayed()
        onNodeWithText("Parallel").assertIsDisplayed()
        onNodeWithText("Quotation").assertIsDisplayed()
        onNodeWithText("Verse 43001001").assertIsDisplayed()
        onNodeWithText("Verse 58011003").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun CrossReferencePane_tapTriggersCallback() = runComposeUiTest {
        var tappedRef: CrossReference? = null

        val flow = MutableStateFlow(
            CrossReferenceState(
                sourceVerseId = 1_001_001,
                references = testRefs,
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            CrossReferencePane(
                stateFlow = flow,
                onReferenceTapped = { tappedRef = it },
                onToggleExpansion = {}
            )
        }

        onNodeWithText("Verse 43001001").performClick()
        assertEquals(43_001_001L, tappedRef?.targetVerseId)
    }
}
