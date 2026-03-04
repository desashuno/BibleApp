package org.biblestudio.features.word_study.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.study.WordOccurrence
import org.biblestudio.core.study.LexiconEntry

/**
 * Observable state for the Word Study pane.
 */
data class WordStudyState(
    val isLoading: Boolean = false,
    val entry: LexiconEntry? = null,
    val occurrences: List<WordOccurrence> = emptyList(),
    val occurrenceCount: Int = 0,
    val occurrencePage: Int = 0,
    val hasMoreOccurrences: Boolean = false,
    val relatedWords: List<LexiconEntry> = emptyList(),
    val error: String? = null,
)

/**
 * Business-logic boundary for the Word Study pane.
 *
 * Subscribes to [LinkEvent.StrongsSelected] from the VerseBus and loads
 * lexicon data, occurrence lists, and related words.
 */
interface WordStudyComponent {

    /** The current word study state observable. */
    val state: StateFlow<WordStudyState>

    /** Called when user taps an occurrence to navigate to that verse. */
    fun onOccurrenceSelected(globalVerseId: Int)

    /** Searches the lexicon by keyword. */
    fun onSearchLexicon(query: String)

    /** Loads the next page of occurrences and appends them to the current list. */
    fun onLoadMoreOccurrences()
}
