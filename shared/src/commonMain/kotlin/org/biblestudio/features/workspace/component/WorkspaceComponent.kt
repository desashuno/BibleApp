package org.biblestudio.features.workspace.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.workspace.domain.model.LayoutNode
import org.biblestudio.features.workspace.domain.model.PanePlacement
import org.biblestudio.features.workspace.domain.model.SplitAxis
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

    /** Removes the single pane at a specific [path] in the layout tree. */
    fun removePaneAtPath(path: List<Int>)

    /** Adjusts the split ratio at [path] within the tree. */
    fun resizeSplit(path: List<Int>, newRatio: Float)

    /** Moves a pane from [from] path to [to] path in the tree. */
    fun movePane(from: List<Int>, to: List<Int>)

    /** Switches the active tab at [path] to [index]. */
    fun switchTab(path: List<Int>, index: Int)

    /** Reorders a tab within a Tabs node, moving the tab at [fromIndex] to [toIndex]. */
    fun reorderTab(path: List<Int>, fromIndex: Int, toIndex: Int)

    /** Moves a pane from [fromPath] and places it relative to [targetPath] according to [placement]. */
    fun rearrangePane(fromPath: List<Int>, targetPath: List<Int>, placement: PanePlacement)

    /** Replaces the current layout with a [preset] template. */
    fun applyPreset(preset: WorkspacePreset)

    /**
     * Splits the pane matching [existingPaneType], placing a new [newPaneType]
     * alongside it on the given [axis] (Horizontal = side-by-side, Vertical = stacked).
     */
    fun splitPane(existingPaneType: String, newPaneType: String, axis: SplitAxis)

    /** Persists the current layout immediately (bypassing debounce). */
    fun saveWorkspace()

    /** Loads the active workspace from DB, or creates a default one if none exists. */
    fun loadActiveWorkspace(onComplete: (() -> Unit)? = null)

    /** Creates a new workspace with the given [name] and switches to it. */
    fun createWorkspace(name: String, onComplete: (() -> Unit)? = null)

    /** Soft-deletes a workspace by [id]; switches away if it was the current one. */
    fun deleteWorkspace(id: String, onComplete: (() -> Unit)? = null)
}
