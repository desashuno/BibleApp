package org.biblestudio.features.workspace.domain.model

import org.biblestudio.core.pane_registry.PaneType

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
        Default -> LayoutNode.Leaf(paneType = PaneType.DASHBOARD)

        Study -> LayoutNode.Split(
            axis = SplitAxis.Horizontal,
            ratio = 0.5f,
            first = LayoutNode.Leaf(paneType = PaneType.BIBLE_READER),
            second = LayoutNode.Tabs(
                children = listOf(
                    LayoutNode.Leaf(paneType = PaneType.CROSS_REFERENCES),
                    LayoutNode.Leaf(paneType = PaneType.WORD_STUDY)
                ),
                activeIndex = 0
            )
        )

        Exegesis -> LayoutNode.Split(
            axis = SplitAxis.Horizontal,
            ratio = 0.5f,
            first = LayoutNode.Leaf(paneType = PaneType.BIBLE_READER),
            second = LayoutNode.Tabs(
                children = listOf(
                    LayoutNode.Leaf(paneType = PaneType.MORPHOLOGY),
                    LayoutNode.Leaf(paneType = PaneType.PASSAGE_GUIDE)
                ),
                activeIndex = 0
            )
        )

        Writing -> LayoutNode.Split(
            axis = SplitAxis.Horizontal,
            ratio = 0.5f,
            first = LayoutNode.Leaf(paneType = PaneType.BIBLE_READER),
            second = LayoutNode.Tabs(
                children = listOf(
                    LayoutNode.Leaf(paneType = PaneType.NOTE_EDITOR),
                    LayoutNode.Leaf(paneType = PaneType.SERMON_EDITOR)
                ),
                activeIndex = 0
            )
        )

        Research -> LayoutNode.Split(
            axis = SplitAxis.Horizontal,
            ratio = 0.5f,
            first = LayoutNode.Leaf(paneType = PaneType.BIBLE_READER),
            second = LayoutNode.Split(
                axis = SplitAxis.Vertical,
                ratio = 0.5f,
                first = LayoutNode.Tabs(
                    children = listOf(
                        LayoutNode.Leaf(paneType = PaneType.SEARCH),
                        LayoutNode.Leaf(paneType = PaneType.RESOURCE_LIBRARY)
                    ),
                    activeIndex = 0
                ),
                second = LayoutNode.Leaf(
                    paneType = PaneType.KNOWLEDGE_GRAPH
                )
            )
        )
    }
}
