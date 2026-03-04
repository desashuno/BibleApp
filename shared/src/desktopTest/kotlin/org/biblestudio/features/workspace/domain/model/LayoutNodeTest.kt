package org.biblestudio.features.workspace.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.biblestudio.core.pane_registry.PaneType

class LayoutNodeTest {

    private val json = Json { prettyPrint = false }

    @Test
    fun `LayoutNode serializes and deserializes round-trip`() {
        val tree: LayoutNode = LayoutNode.Split(
            axis = SplitAxis.Horizontal,
            ratio = 0.5f,
            first = LayoutNode.Leaf(paneType = PaneType.BIBLE_READER),
            second = LayoutNode.Tabs(
                children = listOf(
                    LayoutNode.Leaf(paneType = PaneType.CROSS_REFERENCES),
                    LayoutNode.Leaf(paneType = PaneType.WORD_STUDY)
                ),
                activeIndex = 1
            )
        )

        val encoded = json.encodeToString(LayoutNode.serializer(), tree)
        val decoded = json.decodeFromString(LayoutNode.serializer(), encoded)
        assertEquals(tree, decoded)
    }

    @Test
    fun `WorkspacePreset Study returns expected Split layout`() {
        val layout = WorkspacePreset.Study.toLayout()

        assertIs<LayoutNode.Split>(layout)
        assertIs<LayoutNode.Leaf>((layout as LayoutNode.Split).first)
        assertEquals(PaneType.BIBLE_READER, (layout.first as LayoutNode.Leaf).paneType)
        assertIs<LayoutNode.Tabs>(layout.second)
    }

    @Test
    fun `WorkspacePreset Research contains nested split`() {
        val layout = WorkspacePreset.Research.toLayout()

        assertIs<LayoutNode.Split>(layout)
        val split = layout as LayoutNode.Split
        assertIs<LayoutNode.Leaf>(split.first)
        assertIs<LayoutNode.Split>(split.second)
    }

    @Test
    fun `Default preset is a single leaf`() {
        val layout = WorkspacePreset.Default.toLayout()
        assertIs<LayoutNode.Leaf>(layout)
        assertEquals(PaneType.DASHBOARD, (layout as LayoutNode.Leaf).paneType)
    }
}
