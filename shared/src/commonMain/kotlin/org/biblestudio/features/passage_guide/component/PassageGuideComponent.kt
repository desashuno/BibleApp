package org.biblestudio.features.passage_guide.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.features.passage_guide.domain.entities.PassageReport

/**
 * Observable state for the Passage Guide pane.
 */
data class PassageGuideState(
    val isLoading: Boolean = false,
    val report: PassageReport? = null,
    val expandedSections: Set<String> = setOf("crossRefs", "commentary", "notes"),
    val error: String? = null
)

/**
 * Business-logic boundary for the Passage Guide pane.
 *
 * Subscribes to VerseBus to build an aggregated report from
 * cross-references, outlines, commentary, notes, and morphology.
 */
interface PassageGuideComponent {

    /** The current passage-guide state observable. */
    val state: StateFlow<PassageGuideState>

    /** Called when user taps a cross-reference — navigates to the target verse. */
    fun onRefSelected(crossRef: CrossReference)

    /** Called when user taps a key word — opens word study. */
    fun onWordSelected(strongsNumber: String)

    /** Toggles expand/collapse for a section. */
    fun onSectionToggle(sectionId: String)
}
