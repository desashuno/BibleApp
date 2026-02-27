package org.biblestudio.features.workspace.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Recursive tree that describes how panes are arranged in a workspace.
 *
 * The tree is serialized to JSON for persistence in the `workspace_layouts`
 * table. Each mutation produces a new immutable tree.
 */
@Serializable
sealed class LayoutNode {

    /**
     * A binary split that divides the workspace either horizontally or
     * vertically, with [ratio] (0..1) controlling the first child's share.
     */
    @Serializable
    @SerialName("split")
    data class Split(
        val axis: SplitAxis,
        val ratio: Float,
        val first: LayoutNode,
        val second: LayoutNode
    ) : LayoutNode()

    /**
     * A terminal node that hosts a single pane type.
     *
     * @param paneType Key registered in `PaneRegistry`.
     * @param config Arbitrary key-value configuration forwarded to the pane builder.
     */
    @Serializable
    @SerialName("leaf")
    data class Leaf(
        val paneType: String,
        val config: Map<String, String> = emptyMap()
    ) : LayoutNode()

    /**
     * A tab group that holds several [Leaf] panes with one active tab.
     */
    @Serializable
    @SerialName("tabs")
    data class Tabs(
        val children: List<Leaf>,
        val activeIndex: Int = 0
    ) : LayoutNode()
}
