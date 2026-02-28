package org.biblestudio.features.bible_reader.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.bible_reader.domain.entities.Bible
import org.biblestudio.features.bible_reader.domain.entities.Book
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.features.morphology_interlinear.domain.entities.MorphWord

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
    val highlights: Map<Long, List<Highlight>> = emptyMap(),
    val crossReferences: Map<Long, List<CrossReference>> = emptyMap(),
    val morphology: Map<Long, List<MorphWord>> = emptyMap(),
    val continuousScroll: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
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
}
