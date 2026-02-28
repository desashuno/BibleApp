package org.biblestudio.features.bible_reader.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.bible_reader.domain.entities.Bible
import org.biblestudio.features.bible_reader.domain.entities.VersionComparison

/**
 * Display mode for the text comparison view.
 */
enum class ComparisonViewMode {
    /** Side-by-side columns, one per translation. */
    PARALLEL,

    /** Verse-by-verse alternating translations. */
    INTERLEAVED
}

/**
 * Observable state for the text comparison pane.
 */
data class TextComparisonState(
    val comparison: VersionComparison? = null,
    val availableBibles: List<Bible> = emptyList(),
    val selectedVersions: List<String> = emptyList(),
    val viewMode: ComparisonViewMode = ComparisonViewMode.PARALLEL,
    val diffHighlights: List<DiffSegment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * A segment in a word-level diff between two translations.
 */
data class DiffSegment(
    val text: String,
    val type: DiffType
)

/**
 * Classification of a diff segment.
 */
enum class DiffType {
    EQUAL,
    ADDED,
    REMOVED
}

/**
 * Business-logic boundary for the Text Comparison pane.
 */
interface TextComparisonComponent {

    /** The current comparison state observable. */
    val state: StateFlow<TextComparisonState>

    /** Loads comparison data for a given global verse ID. */
    fun loadComparison(globalVerseId: Long)

    /** Switches between parallel and interleaved display. */
    fun setViewMode(mode: ComparisonViewMode)

    /** Selects which versions to display. */
    fun setSelectedVersions(versions: List<String>)

    /** Navigates to the next verse. */
    fun nextVerse()

    /** Navigates to the previous verse. */
    fun previousVerse()
}
