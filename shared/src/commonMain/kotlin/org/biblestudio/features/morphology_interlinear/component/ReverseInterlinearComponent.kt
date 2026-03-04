package org.biblestudio.features.morphology_interlinear.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.study.MorphWord

/**
 * A single English token aligned to its original-language morphology.
 *
 * @param englishToken The English word.
 * @param morphWord The linked original-language word, or null if unaligned.
 * @param decodedParsing Human-readable morphology description, or null.
 */
data class AlignedToken(
    val englishToken: String,
    val morphWord: MorphWord? = null,
    val decodedParsing: String? = null
)

/**
 * Observable state for the Reverse Interlinear pane.
 */
data class ReverseInterlinearState(
    val isLoading: Boolean = false,
    val verse: Long? = null,
    val alignedTokens: List<AlignedToken> = emptyList(),
    val selectedToken: AlignedToken? = null,
    val error: String? = null
)

/**
 * Business-logic boundary for the Reverse Interlinear pane.
 *
 * Maps English translation words back to original-language morphology
 * using pre-computed alignment data.
 */
interface ReverseInterlinearComponent {

    /** The current reverse-interlinear state observable. */
    val state: StateFlow<ReverseInterlinearState>

    /** Called when user taps an English token — shows popover and publishes StrongsSelected. */
    fun onTokenSelected(token: AlignedToken)

    /** Clears the currently selected token popover. */
    fun clearSelection()
}
