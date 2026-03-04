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

private val PUNCTUATION_REGEX = Regex("[.,;:!?\"'()\\[\\]]")

/** Strips punctuation for matching but preserves original words in output. */
private fun normalize(word: String): String = word.replace(PUNCTUATION_REGEX, "").lowercase()

/**
 * Punctuation-aware word-level diff using LCS (Longest Common Subsequence).
 * Compares normalized forms but outputs original words.
 */
@Suppress("NestedBlockDepth")
fun wordDiff(wordsA: List<String>, wordsB: List<String>): List<DiffSegment> {
    val normA = wordsA.map { normalize(it) }
    val normB = wordsB.map { normalize(it) }
    val m = wordsA.size
    val n = wordsB.size
    val dp = Array(m + 1) { IntArray(n + 1) }
    for (i in 1..m) {
        for (j in 1..n) {
            dp[i][j] = if (normA[i - 1] == normB[j - 1]) {
                dp[i - 1][j - 1] + 1
            } else {
                maxOf(dp[i - 1][j], dp[i][j - 1])
            }
        }
    }
    val stack = mutableListOf<DiffSegment>()
    var i = m
    var j = n
    while (i > 0 || j > 0) {
        when {
            i > 0 && j > 0 && normA[i - 1] == normB[j - 1] -> {
                stack.add(DiffSegment(wordsB[j - 1], DiffType.EQUAL))
                i--
                j--
            }
            j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j]) -> {
                stack.add(DiffSegment(wordsB[j - 1], DiffType.ADDED))
                j--
            }
            else -> {
                stack.add(DiffSegment(wordsA[i - 1], DiffType.REMOVED))
                i--
            }
        }
    }
    return stack.reversed()
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

    /** Returns formatted comparison text for clipboard. */
    fun copyComparisonText(): String
}
