package org.biblestudio.features.cross_references.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.study.CrossReference

/**
 * Observable state for the Cross-References pane.
 */
data class CrossReferenceState(
    val sourceVerseId: Long? = null,
    val references: List<CrossReference> = emptyList(),
    val expandedVerseTexts: Map<Long, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Business-logic boundary for the Cross-References pane.
 *
 * Automatically reacts to VerseBus events to load cross-references
 * for the currently selected verse.
 */
interface CrossReferenceComponent {

    /** The current cross-reference state observable. */
    val state: StateFlow<CrossReferenceState>

    /** Manually loads cross-references for a verse. */
    fun loadReferences(globalVerseId: Long)

    /** Called when user taps a cross-reference target. */
    fun onReferenceTapped(reference: CrossReference)

    /** Toggles inline expansion for a reference's target verse text. */
    fun toggleExpansion(reference: CrossReference)
}
