package org.biblestudio.features.workspace.component

import org.biblestudio.test.testComponentContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.biblestudio.core.pane_registry.PaneType
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.workspace.domain.entities.Workspace
import org.biblestudio.features.workspace.domain.entities.WorkspaceLayout
import org.biblestudio.features.workspace.domain.model.LayoutNode
import org.biblestudio.features.workspace.domain.model.SplitAxis
import org.biblestudio.features.workspace.domain.repositories.WorkspaceRepository

class DefaultWorkspaceComponentTest {

    private val fakeRepo = object : WorkspaceRepository {
        override suspend fun getAll(): Result<List<Workspace>> = Result.success(emptyList())

        override suspend fun getActive(): Result<Workspace?> = Result.success(null)

        override suspend fun getByUuid(uuid: String): Result<Workspace?> = Result.success(null)

        override suspend fun create(workspace: Workspace): Result<Unit> = Result.success(Unit)

        override suspend fun update(workspace: Workspace): Result<Unit> = Result.success(Unit)

        override suspend fun setActive(uuid: String, updatedAt: String, deviceId: String): Result<Unit> =
            Result.success(Unit)

        override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> = Result.success(Unit)

        override suspend fun getLayout(workspaceId: String): Result<WorkspaceLayout?> = Result.success(null)

        override suspend fun saveLayout(layout: WorkspaceLayout): Result<Unit> = Result.success(Unit)

        override fun watchAll(): Flow<List<Workspace>> = emptyFlow()
    }

    private fun createComponent(): DefaultWorkspaceComponent {
        val context = testComponentContext()
        return DefaultWorkspaceComponent(
            componentContext = context,
            repository = fakeRepo,
            verseBus = VerseBus()
        )
    }

    @Test
    fun `containsPaneType returns true for single leaf`() {
        val component = createComponent()
        component.updateLayout(LayoutNode.Leaf(paneType = PaneType.BIBLE_READER))

        assertTrue(component.containsPaneType(PaneType.BIBLE_READER))
    }

    @Test
    fun `containsPaneType returns false for missing pane`() {
        val component = createComponent()
        component.updateLayout(LayoutNode.Leaf(paneType = PaneType.BIBLE_READER))

        assertFalse(component.containsPaneType(PaneType.CROSS_REFERENCES))
    }

    @Test
    fun `containsPaneType returns true for pane inside Split`() {
        val component = createComponent()
        component.updateLayout(
            LayoutNode.Split(
                axis = SplitAxis.Horizontal,
                ratio = 0.5f,
                first = LayoutNode.Leaf(paneType = PaneType.BIBLE_READER),
                second = LayoutNode.Leaf(paneType = PaneType.CROSS_REFERENCES)
            )
        )

        assertTrue(component.containsPaneType(PaneType.CROSS_REFERENCES))
        assertTrue(component.containsPaneType(PaneType.BIBLE_READER))
    }

    @Test
    fun `containsPaneType returns true for pane inside Tabs`() {
        val component = createComponent()
        component.updateLayout(
            LayoutNode.Tabs(
                children = listOf(
                    LayoutNode.Leaf(paneType = PaneType.BIBLE_READER),
                    LayoutNode.Leaf(paneType = PaneType.WORD_STUDY),
                    LayoutNode.Leaf(paneType = PaneType.CROSS_REFERENCES)
                ),
                activeIndex = 0
            )
        )

        assertTrue(component.containsPaneType(PaneType.WORD_STUDY))
        assertTrue(component.containsPaneType(PaneType.CROSS_REFERENCES))
    }

    @Test
    fun `containsPaneType returns true for deeply nested pane`() {
        val component = createComponent()
        component.updateLayout(
            LayoutNode.Split(
                axis = SplitAxis.Horizontal,
                ratio = 0.5f,
                first = LayoutNode.Leaf(paneType = PaneType.BIBLE_READER),
                second = LayoutNode.Split(
                    axis = SplitAxis.Vertical,
                    ratio = 0.5f,
                    first = LayoutNode.Leaf(paneType = PaneType.SEARCH),
                    second = LayoutNode.Tabs(
                        children = listOf(
                            LayoutNode.Leaf(paneType = PaneType.CROSS_REFERENCES),
                            LayoutNode.Leaf(paneType = PaneType.WORD_STUDY)
                        ),
                        activeIndex = 0
                    )
                )
            )
        )

        assertTrue(component.containsPaneType(PaneType.WORD_STUDY))
        assertTrue(component.containsPaneType(PaneType.SEARCH))
        assertFalse(component.containsPaneType(PaneType.TIMELINE))
    }

    @Test
    fun `focusPaneByType activates correct tab index`() {
        val component = createComponent()
        component.updateLayout(
            LayoutNode.Tabs(
                children = listOf(
                    LayoutNode.Leaf(paneType = PaneType.BIBLE_READER),
                    LayoutNode.Leaf(paneType = PaneType.WORD_STUDY),
                    LayoutNode.Leaf(paneType = PaneType.CROSS_REFERENCES)
                ),
                activeIndex = 0
            )
        )

        // Initially, word-study tab (index 1) is not active
        val tabsBefore = component.state.value.layout as LayoutNode.Tabs
        assertEquals(0, tabsBefore.activeIndex)

        // Focus on word-study
        component.focusPaneByType(PaneType.WORD_STUDY)

        val tabsAfter = component.state.value.layout as LayoutNode.Tabs
        assertEquals(1, tabsAfter.activeIndex)
    }

    @Test
    fun `focusPaneByType activates tab in nested split`() {
        val component = createComponent()
        component.updateLayout(
            LayoutNode.Split(
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
        )

        component.focusPaneByType(PaneType.WORD_STUDY)

        val split = component.state.value.layout as LayoutNode.Split
        val tabs = split.second as LayoutNode.Tabs
        assertEquals(1, tabs.activeIndex)
    }
}
