package org.biblestudio.features.workspace.domain.model

/**
 * Pre-defined pane layouts that the user can apply with a single action.
 *
 * Each preset returns a fresh [LayoutNode] tree.
 */
enum class WorkspacePreset(val displayName: String) {
    /** Single Bible reader pane. */
    Default("Default"),

    /** Reader + Cross-References + Word Study. */
    Study("Study"),

    /** Reader + Morphology + Passage Guide. */
    Exegesis("Exegesis"),

    /** Reader + Notes + Sermon Editor. */
    Writing("Writing"),

    /** Reader + Search + Resources + Knowledge Graph. */
    Research("Research")
    ;

    /** Builds the [LayoutNode] tree for this preset. */
    @Suppress("MagicNumber")
    fun toLayout(): LayoutNode = when (this) {
        Default -> LayoutNode.Leaf(paneType = "dashboard")

        Study -> LayoutNode.Split(
            axis = SplitAxis.Horizontal,
            ratio = 0.5f,
            first = LayoutNode.Leaf(paneType = "bible-reader"),
            second = LayoutNode.Tabs(
                children = listOf(
                    LayoutNode.Leaf(paneType = "cross-references"),
                    LayoutNode.Leaf(paneType = "word-study")
                ),
                activeIndex = 0
            )
        )

        Exegesis -> LayoutNode.Split(
            axis = SplitAxis.Horizontal,
            ratio = 0.5f,
            first = LayoutNode.Leaf(paneType = "bible-reader"),
            second = LayoutNode.Tabs(
                children = listOf(
                    LayoutNode.Leaf(paneType = "morphology"),
                    LayoutNode.Leaf(paneType = "passage-guide")
                ),
                activeIndex = 0
            )
        )

        Writing -> LayoutNode.Split(
            axis = SplitAxis.Horizontal,
            ratio = 0.5f,
            first = LayoutNode.Leaf(paneType = "bible-reader"),
            second = LayoutNode.Tabs(
                children = listOf(
                    LayoutNode.Leaf(paneType = "note-editor"),
                    LayoutNode.Leaf(paneType = "sermon-editor")
                ),
                activeIndex = 0
            )
        )

        Research -> LayoutNode.Split(
            axis = SplitAxis.Horizontal,
            ratio = 0.5f,
            first = LayoutNode.Leaf(paneType = "bible-reader"),
            second = LayoutNode.Split(
                axis = SplitAxis.Vertical,
                ratio = 0.5f,
                first = LayoutNode.Tabs(
                    children = listOf(
                        LayoutNode.Leaf(paneType = "search"),
                        LayoutNode.Leaf(paneType = "resource-library")
                    ),
                    activeIndex = 0
                ),
                second = LayoutNode.Leaf(
                    paneType = "knowledge-graph"
                )
            )
        )
    }
}
