package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.bible_reader.component.BibleReaderState
import org.biblestudio.features.bible_reader.domain.entities.Book
import org.biblestudio.features.bible_reader.domain.entities.Verse

@OptIn(ExperimentalTestApi::class)
class BibleReaderPaneTest {

    private val testBook = Book(
        id = 1,
        bibleId = 1,
        bookNumber = 1,
        name = "Genesis",
        testament = "OT",
        chapterCount = 50
    )

    private val testVerses = listOf(
        Verse(
            id = 1,
            chapterId = 1,
            globalVerseId = 1_001_001,
            verseNumber = 1,
            text = "In the beginning God created the heavens and the earth."
        ),
        Verse(
            id = 2,
            chapterId = 1,
            globalVerseId = 1_001_002,
            verseNumber = 2,
            text = "And the earth was without form, and void."
        )
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_rendersVerses() = runComposeUiTest {
        val flow = MutableStateFlow(
            BibleReaderState(
                currentBook = testBook,
                currentChapter = 1,
                verses = testVerses,
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            BibleReaderPane(
                stateFlow = flow,
                onVerseTapped = {},
                onVerseLongPressed = {},
                onBookChapterSelected = { _, _ -> },
                onToggleBookPicker = {}
            )
        }

        onNodeWithText("Genesis 1").assertIsDisplayed()
        onNodeWithText("In the beginning God created the heavens and the earth.", substring = true)
            .assertIsDisplayed()
        onNodeWithText("And the earth was without form, and void.", substring = true)
            .assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_verseTapTriggersCallback() = runComposeUiTest {
        var tappedVerse: Verse? = null

        val flow = MutableStateFlow(
            BibleReaderState(
                currentBook = testBook,
                currentChapter = 1,
                verses = testVerses,
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            BibleReaderPane(
                stateFlow = flow,
                onVerseTapped = { tappedVerse = it },
                onVerseLongPressed = {},
                onBookChapterSelected = { _, _ -> },
                onToggleBookPicker = {}
            )
        }

        onNodeWithText("In the beginning God created the heavens and the earth.", substring = true)
            .performClick()

        assertEquals(1_001_001L, tappedVerse?.globalVerseId)
    }
}
