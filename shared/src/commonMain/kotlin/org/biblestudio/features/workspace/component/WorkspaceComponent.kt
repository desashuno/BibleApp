package org.biblestudio.features.workspace.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.workspace.domain.model.LayoutNode
import org.biblestudio.features.workspace.domain.model.WorkspacePreset
import org.biblestudio.features.workspace.domain.model.WorkspaceState

/**
 * Business-logic boundary for workspace management.
 *
 * Implementations coordinate layout mutations, persistence (with debounced
 * auto-save), and preset application while exposing immutable [WorkspaceState]
 * to the UI layer.
 */
interface WorkspaceComponent {

    /** The current workspace state observable. */
    val state: StateFlow<WorkspaceState>

    /** Loads a workspace by [workspaceId] from the repository. */
    fun loadWorkspace(workspaceId: String)

    /** Replaces the current layout tree. */
    fun updateLayout(layout: LayoutNode)

    /** Appends a new pane to the tree (right-side split or new tab). */
    fun addPane(paneType: String)

    /** Removes all leaves that match [paneType] from the tree. */
    fun removePane(paneType: String)

    /** Adjusts the split ratio at [path] within the tree. */
    fun resizeSplit(path: List<Int>, newRatio: Float)

    /** Moves a pane from [from] path to [to] path in the tree. */
    fun movePane(from: List<Int>, to: List<Int>)

    /** Switches the active tab at [path] to [index]. */
    fun switchTab(path: List<Int>, index: Int)

    /** Replaces the current layout with a [preset] template. */
    fun applyPreset(preset: WorkspacePreset)

    /** Persists the current layout immediately (bypassing debounce). */
    fun saveWorkspace()
}
