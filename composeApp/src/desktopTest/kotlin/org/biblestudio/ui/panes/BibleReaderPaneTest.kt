package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.bible_reader.component.BibleReaderState
import org.biblestudio.features.bible_reader.component.VerseSelectionRange
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

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_readerToolbarRendersToggleButtons() = runComposeUiTest {
        val flow = MutableStateFlow(
            BibleReaderState(
                currentBook = testBook,
                currentChapter = 1,
                verses = testVerses,
                isLoading = false,
                showReaderToolbar = true
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            BibleReaderPane(
                stateFlow = flow,
                onVerseTapped = {},
                onVerseLongPressed = {},
                onBookChapterSelected = { _, _ -> },
                onToggleBookPicker = {},
                onToggleShowVerseNumbers = {},
                onToggleRedLetter = {},
                onToggleParagraphMode = {},
                onToggleContinuousScroll = {},
                onAdjustFontSize = {}
            )
        }

        // Verify toolbar buttons are rendered
        onNodeWithContentDescription("Verse numbers").assertIsDisplayed()
        onNodeWithContentDescription("Paragraph mode").assertIsDisplayed()
        onNodeWithContentDescription("Continuous scroll").assertIsDisplayed()
        onNodeWithContentDescription("Decrease font").assertIsDisplayed()
        onNodeWithContentDescription("Increase font").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_clickingVerseNumberToggleFiresCallback() = runComposeUiTest {
        var toggleCalled = false

        val flow = MutableStateFlow(
            BibleReaderState(
                currentBook = testBook,
                currentChapter = 1,
                verses = testVerses,
                isLoading = false,
                showReaderToolbar = true
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            BibleReaderPane(
                stateFlow = flow,
                onVerseTapped = {},
                onVerseLongPressed = {},
                onBookChapterSelected = { _, _ -> },
                onToggleBookPicker = {},
                onToggleShowVerseNumbers = { toggleCalled = true },
                onToggleRedLetter = {},
                onToggleParagraphMode = {},
                onToggleContinuousScroll = {},
                onAdjustFontSize = {}
            )
        }

        onNodeWithContentDescription("Verse numbers").performClick()
        assertTrue(toggleCalled)
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_toolbarHiddenWhenShowReaderToolbarFalse() = runComposeUiTest {
        val flow = MutableStateFlow(
            BibleReaderState(
                currentBook = testBook,
                currentChapter = 1,
                verses = testVerses,
                isLoading = false,
                showReaderToolbar = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            BibleReaderPane(
                stateFlow = flow,
                onVerseTapped = {},
                onVerseLongPressed = {},
                onBookChapterSelected = { _, _ -> },
                onToggleBookPicker = {},
                onToggleShowVerseNumbers = {},
                onToggleRedLetter = {},
                onToggleParagraphMode = {},
                onToggleContinuousScroll = {},
                onAdjustFontSize = {}
            )
        }

        // When toolbar is hidden, the toggle buttons should not exist
        onNodeWithContentDescription("Verse numbers").assertDoesNotExist()
        onNodeWithContentDescription("Paragraph mode").assertDoesNotExist()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_fontSizeButtonsFireCallbacks() = runComposeUiTest {
        val fontSizeDeltas = mutableListOf<Int>()

        val flow = MutableStateFlow(
            BibleReaderState(
                currentBook = testBook,
                currentChapter = 1,
                verses = testVerses,
                isLoading = false,
                showReaderToolbar = true
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            BibleReaderPane(
                stateFlow = flow,
                onVerseTapped = {},
                onVerseLongPressed = {},
                onBookChapterSelected = { _, _ -> },
                onToggleBookPicker = {},
                onToggleShowVerseNumbers = {},
                onToggleRedLetter = {},
                onToggleParagraphMode = {},
                onToggleContinuousScroll = {},
                onAdjustFontSize = { fontSizeDeltas.add(it) }
            )
        }

        onNodeWithContentDescription("Decrease font").performClick()
        assertEquals(listOf(-1), fontSizeDeltas)

        onNodeWithContentDescription("Increase font").performClick()
        assertEquals(listOf(-1, 1), fontSizeDeltas)
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_selectionBarNotVisibleInitially() = runComposeUiTest {
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

        onNodeWithContentDescription("Highlight").assertDoesNotExist()
        onNodeWithContentDescription("Clear selection").assertDoesNotExist()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_selectionBottomBarAppearsWhenVerseRangeSelected() = runComposeUiTest {
        val range = VerseSelectionRange(startVerseId = 1_001_001, endVerseId = 1_001_001)
        val flow = MutableStateFlow(
            BibleReaderState(
                currentBook = testBook,
                currentChapter = 1,
                verses = testVerses,
                isLoading = false,
                selectedVerseRange = range
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            BibleReaderPane(
                stateFlow = flow,
                onVerseTapped = {},
                onVerseLongPressed = {},
                onBookChapterSelected = { _, _ -> },
                onToggleBookPicker = {},
                onHighlightSelection = {},
                onClearSelection = {}
            )
        }

        onNodeWithContentDescription("Highlight").assertIsDisplayed()
        onNodeWithContentDescription("Clear selection").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_clickingRedLetterToggleFiresCallback() = runComposeUiTest {
        var toggleCalled = false

        val flow = MutableStateFlow(
            BibleReaderState(
                currentBook = testBook,
                currentChapter = 1,
                verses = testVerses,
                isLoading = false,
                showReaderToolbar = true
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            BibleReaderPane(
                stateFlow = flow,
                onVerseTapped = {},
                onVerseLongPressed = {},
                onBookChapterSelected = { _, _ -> },
                onToggleBookPicker = {},
                onToggleShowVerseNumbers = {},
                onToggleRedLetter = { toggleCalled = true },
                onToggleParagraphMode = {},
                onToggleContinuousScroll = {},
                onAdjustFontSize = {}
            )
        }

        onNodeWithText("Red").performClick()
        assertTrue(toggleCalled)
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_verseCounterNotDisplayed() = runComposeUiTest {
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

        // The verse counter "v.X / N" should not appear in the UI
        onNodeWithText("v.1 / 2", substring = true).assertDoesNotExist()
        onNodeWithText("v.1 /", substring = true).assertDoesNotExist()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_oldTestamentTabFiltersCorrectly() = runComposeUiTest {
        val otBook = Book(id = 1, bibleId = 1, bookNumber = 1, name = "Genesis", testament = "OT", chapterCount = 50)
        val ntBook = Book(id = 40, bibleId = 1, bookNumber = 40, name = "Matthew", testament = "NT", chapterCount = 28)
        val flow = MutableStateFlow(
            BibleReaderState(
                currentBook = otBook,
                currentChapter = 1,
                verses = testVerses,
                isLoading = false,
                showBookPicker = true,
                books = listOf(otBook, ntBook)
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

        // Tap "Antiguo T." tab
        onNodeWithText("Antiguo T.").performClick()

        // OT book should be visible, NT should not
        onNodeWithText("Genesis").assertIsDisplayed()
        onNodeWithText("Matthew").assertDoesNotExist()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun BibleReaderPane_bookSearchFiltersBooks() = runComposeUiTest {
        val genesisBook = Book(id = 1, bibleId = 1, bookNumber = 1, name = "Genesis", testament = "OT", chapterCount = 50)
        val matthewBook = Book(id = 40, bibleId = 1, bookNumber = 40, name = "Matthew", testament = "NT", chapterCount = 28)
        val flow = MutableStateFlow(
            BibleReaderState(
                currentBook = genesisBook,
                currentChapter = 1,
                verses = testVerses,
                isLoading = false,
                showBookPicker = true,
                books = listOf(genesisBook, matthewBook)
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

        // Type "Mat" in the search field
        onNodeWithText("Buscar libro\u2026", substring = true).performTextInput("Mat")

        // Only Matthew should appear, Genesis should not
        onNodeWithText("Matthew").assertIsDisplayed()
        onNodeWithText("Genesis").assertDoesNotExist()
    }
}
