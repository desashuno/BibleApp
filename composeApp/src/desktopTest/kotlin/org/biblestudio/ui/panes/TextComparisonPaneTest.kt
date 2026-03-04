package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.bible_reader.component.ComparisonViewMode
import org.biblestudio.features.bible_reader.component.TextComparisonState
import org.biblestudio.features.bible_reader.domain.entities.VersionComparison
import org.biblestudio.features.bible_reader.domain.entities.VersionVerse

@OptIn(ExperimentalTestApi::class)
class TextComparisonPaneTest {

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun parallelViewShowsMultipleColumns() = runComposeUiTest {
        val flow = MutableStateFlow(
            TextComparisonState(
                comparison = VersionComparison(
                    globalVerseId = 1_001_001,
                    versions = mapOf(
                        "KJV" to VersionVerse("In the beginning God created the heaven and the earth."),
                        "ASV" to VersionVerse("In the beginning God created the heavens and the earth.")
                    )
                ),
                viewMode = ComparisonViewMode.PARALLEL,
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            TextComparisonPane(
                stateFlow = flow,
                onViewModeChanged = {}
            )
        }

        onNodeWithText("KJV").assertIsDisplayed()
        onNodeWithText("ASV").assertIsDisplayed()
        // First column (KJV) renders plain text
        onNodeWithText("In the beginning God created the heaven and the earth.")
            .assertIsDisplayed()
        // Second column (ASV) renders as word diff vs baseline, including both removed and added words
        onNodeWithText("In the beginning God created the heaven heavens and the earth.")
            .assertIsDisplayed()
    }
}
