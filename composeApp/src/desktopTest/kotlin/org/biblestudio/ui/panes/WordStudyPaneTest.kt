package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.core.study.WordOccurrence
import org.biblestudio.features.word_study.component.WordStudyState
import org.biblestudio.core.study.LexiconEntry

@OptIn(ExperimentalTestApi::class)
class WordStudyPaneTest {

    private val testEntry = LexiconEntry(
        strongsNumber = "H1254",
        originalWord = "\u05D1\u05B8\u05BC\u05E8\u05B8\u05D0",
        transliteration = "bara",
        definition = "to create, shape, form",
        usageNotes = null
    )

    private val testOccurrences = listOf(
        WordOccurrence(1, "H1254", 1_001_001, 5),
        WordOccurrence(2, "H1254", 1_001_021, 3),
        WordOccurrence(3, "H1254", 1_001_027, 3)
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun WordStudyPane_rendersDefinition() = runComposeUiTest {
        val flow = MutableStateFlow(
            WordStudyState(
                entry = testEntry,
                occurrences = testOccurrences,
                occurrenceCount = 3,
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            WordStudyPane(
                stateFlow = flow,
                onOccurrenceSelected = {}
            )
        }

        onNodeWithText("bara", substring = true).assertIsDisplayed()
        onNodeWithText("to create, shape, form", substring = true).assertIsDisplayed()
        onNodeWithText("3 occurrences", substring = true).assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun WordStudyPane_emptyStateShowsPrompt() = runComposeUiTest {
        val flow = MutableStateFlow(WordStudyState())

        setContent {
            @Suppress("ktlint:standard:function-naming")
            WordStudyPane(
                stateFlow = flow,
                onOccurrenceSelected = {}
            )
        }

        onNodeWithText("Select a word to study").assertIsDisplayed()
    }
}
