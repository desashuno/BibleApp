package org.biblestudio.features.highlights.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.features.highlights.domain.entities.HighlightColor

/**
 * Observable state for the Highlights manager pane.
 */
data class HighlightState(
    val highlights: List<Highlight> = emptyList(),
    val selectedColor: HighlightColor = HighlightColor.Yellow,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Business-logic boundary for the Highlights manager pane.
 *
 * Subscribes to [LinkEvent.VerseSelected] to load highlights for the
 * active verse and provides CRUD operations.
 */
interface HighlightComponent {

    /** The current highlight state observable. */
    val state: StateFlow<HighlightState>

    /** Select a colour for new highlights. */
    fun onColorSelected(color: HighlightColor)

    /** Create a highlight on a verse (or sub-range within it). */
    fun onHighlightVerse(globalVerseId: Long, startOffset: Long = 0, endOffset: Long = -1)

    /** Delete a highlight. */
    fun onDeleteHighlight(uuid: String)

    /** Update the colour of an existing highlight. */
    fun onChangeColor(uuid: String, color: HighlightColor)
}
