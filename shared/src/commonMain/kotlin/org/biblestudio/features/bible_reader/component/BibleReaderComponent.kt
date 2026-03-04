package org.biblestudio.features.bible_reader.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.bible_reader.domain.entities.Bible
import org.biblestudio.features.bible_reader.domain.entities.Book
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.core.study.CrossReference
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.core.study.MorphWord

/**
 * A word tapped by the user inside a verse, with optional morphology info.
 */
data class SelectedWord(
    val word: String,
    val globalVerseId: Long,
    val wordIndex: Int,
    val morphWord: MorphWord? = null
)

/**
 * Observable state for the Bible Reader pane.
 */
data class BibleReaderState(
    val currentBible: Bible? = null,
    val currentBook: Book? = null,
    val currentChapter: Long = 1,
    val verses: List<Verse> = emptyList(),
    val books: List<Book> = emptyList(),
    val bibles: List<Bible> = emptyList(),
    val chapterCount: Long = 0,
    val scrollToVerseId: Long? = null,
    val showBookPicker: Boolean = false,
    val selectedVerseRange: VerseSelectionRange? = null,
    val selectedWord: SelectedWord? = null,
    val highlights: Map<Long, List<Highlight>> = emptyMap(),
    val crossReferences: Map<Long, List<CrossReference>> = emptyMap(),
    val morphology: Map<Long, List<MorphWord>> = emptyMap(),
    val continuousScroll: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Per-pane verse-number override; `null` = use global setting. */
    val showVerseNumbers: Boolean? = null,
    /** Per-pane red-letter override; `null` = use global setting. */
    val redLetter: Boolean? = null,
    /** Per-pane paragraph-mode override; `null` = use global setting. */
    val paragraphMode: Boolean? = null,
    /** Per-pane font-size override; `null` = use global setting. */
    val fontSize: Int? = null,
    /** Whether the in-pane reader toolbar is visible. */
    val showReaderToolbar: Boolean = true,
    /** Zero-based index of the currently focused verse within [verses]. */
    val currentVerseIndex: Int = 0,
)

/**
 * Range of selected verses for long-press multi-select.
 */
data class VerseSelectionRange(
    val startVerseId: Long,
    val endVerseId: Long
)

/**
 * Business-logic boundary for the Bible Reader pane.
 *
 * Manages chapter navigation, VerseBus integration, and verse display.
 */
@Suppress("TooManyFunctions")
interface BibleReaderComponent {

    /** The current reader state observable. */
    val state: StateFlow<BibleReaderState>

    /** Switches to a different Bible version. */
    fun selectBible(bibleId: Long)

    /** Navigates to a specific book and chapter. */
    fun goToChapter(bookId: Long, chapter: Long)

    /** Called when a user taps a verse — publishes to VerseBus. */
    fun onVerseTapped(verse: Verse)

    /** Navigates to the next chapter. */
    fun nextChapter()

    /** Navigates to the previous chapter. */
    fun previousChapter()

    /** Navigates to the next verse within the current chapter. */
    fun nextVerse()

    /** Navigates to the previous verse within the current chapter. */
    fun previousVerse()

    /** Toggles the book/chapter picker sheet. */
    fun toggleBookPicker()

    /** Called on long-press to start or extend a verse selection range. */
    fun onVerseLongPressed(verse: Verse)

    /** Clears the current verse selection. */
    fun clearSelection()

    /** Returns formatted text for the currently selected verse range, for copy/share. */
    fun getSelectedVerseText(): String

    /** Toggles continuous scroll mode (shows entire book with chapter dividers). */
    fun setContinuousScroll(enabled: Boolean)

    /** Creates a highlight for each verse in the current selection. */
    fun highlightSelection(colorIndex: Int)

    /** Bookmarks the first verse in the current selection. */
    fun bookmarkVerse()

    /** Publishes a SearchResult event to VerseBus so the Search pane picks it up. */
    fun searchWord(word: String)

    /** Publishes a StrongsSelected event to VerseBus so Word Study picks it up. */
    fun studyWord(strongsNumber: String)

    /** Called when a user taps a specific word inside a verse. */
    fun onWordTapped(verse: Verse, word: String, wordIndex: Int)

    /** Dismisses the word action popup. */
    fun dismissWordPopup()

    /** Toggles per-pane verse number visibility. */
    fun toggleShowVerseNumbers()

    /** Toggles per-pane red-letter display. */
    fun toggleRedLetter()

    /** Toggles per-pane paragraph mode. */
    fun toggleParagraphMode()

    /** Adjusts per-pane font size by [delta] (clamped to 10..32). */
    fun adjustFontSize(delta: Int)

    /** Shows or hides the in-pane reader toolbar. */
    fun toggleReaderToolbar()

    /** Called when a cross-reference badge is tapped — navigates reader to target verse. */
    fun onCrossReferenceTapped(targetVerseId: Long)
}
