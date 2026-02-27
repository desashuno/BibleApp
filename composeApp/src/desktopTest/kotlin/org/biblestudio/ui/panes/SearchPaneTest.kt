package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.search.component.SearchState

@OptIn(ExperimentalTestApi::class)
class SearchPaneTest {

    private val testVerse = Verse(
        id = 1,
        chapterId = 1,
        globalVerseId = 1_001_001,
        verseNumber = 1,
        text = "In the beginning God created the heavens and the earth."
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun searchResultsDisplayCorrectly() = runComposeUiTest {
        val flow = MutableStateFlow(
            SearchState(
                query = "beginning",
                results = listOf(testVerse),
                resultCount = 1,
                isSearching = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            SearchPane(
                stateFlow = flow,
                onQueryChanged = {},
                onSearch = {},
                onScopeChanged = {},
                onResultTapped = {},
                onClearHistory = {}
            )
        }

        onNodeWithText("1 result(s)").assertIsDisplayed()
        onNodeWithText("In the beginning God created the heavens and the earth.", substring = true)
            .assertIsDisplayed()
    }
}
