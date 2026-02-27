package org.biblestudio.features.morphology_interlinear.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.morphology_interlinear.domain.entities.MorphWord

/**
 * Display mode for the interlinear pane.
 */
enum class InterlinearDisplayMode {
    /** Stacked rows: original, transliteration, gloss, parsing. */
    Interlinear,

    /** Side-by-side original and translation text. */
    Parallel,

    /** Inline tooltip on hover/tap. */
    Inline
}

/**
 * Observable state for the Interlinear pane.
 */
data class InterlinearState(
    val isLoading: Boolean = false,
    val verse: Long? = null,
    val words: List<MorphWord> = emptyList(),
    val decodedParsings: Map<String, String> = emptyMap(),
    val displayMode: InterlinearDisplayMode = InterlinearDisplayMode.Interlinear,
    val error: String? = null
)

/**
 * Business-logic boundary for the Interlinear pane.
 *
 * Subscribes to [LinkEvent.VerseSelected] from VerseBus and loads
 * morphological word data for the selected verse.
 */
interface InterlinearComponent {

    /** The current interlinear state observable. */
    val state: StateFlow<InterlinearState>

    /** Called when user taps a word — publishes StrongsSelected to VerseBus. */
    fun onWordSelected(word: MorphWord)

    /** Switches the interlinear display mode. */
    fun onDisplayModeChanged(mode: InterlinearDisplayMode)
}
