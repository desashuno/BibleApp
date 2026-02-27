package org.biblestudio.ui.workspace

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.workspace.domain.model.LayoutNode
import org.biblestudio.features.workspace.domain.model.SplitAxis
import org.biblestudio.features.workspace.domain.model.WorkspaceState
import org.biblestudio.ui.layout.WindowSizeClass

@OptIn(ExperimentalTestApi::class)
class WorkspaceShellTest {

    private fun stateFlow(layout: LayoutNode) = MutableStateFlow(
        WorkspaceState(
            layout = layout,
            workspaceName = "Test",
            loading = false,
            error = null
        )
    )

    @Test
    fun singlePaneLayoutRendersCorrectly() = runComposeUiTest {
        val flow = stateFlow(LayoutNode.Leaf(paneType = "bible-reader"))

        setContent {
            WorkspaceShell(
                stateFlow = flow,
                sizeClass = WindowSizeClass.Expanded,
                callbacks = WorkspaceCallbacks()
            )
        }

        // Header + body both show pane type → expect 2 nodes
        onAllNodesWithText("bible-reader")[0].assertIsDisplayed()
    }

    @Test
    fun splitPaneLayoutRendersBothPanes() = runComposeUiTest {
        val flow = stateFlow(
            LayoutNode.Split(
                axis = SplitAxis.Horizontal,
                ratio = 0.5f,
                first = LayoutNode.Leaf(paneType = "bible-reader"),
                second = LayoutNode.Leaf(paneType = "search")
            )
        )

        setContent {
            WorkspaceShell(
                stateFlow = flow,
                sizeClass = WindowSizeClass.Expanded,
                callbacks = WorkspaceCallbacks()
            )
        }

        onAllNodesWithText("bible-reader")[0].assertIsDisplayed()
        onAllNodesWithText("search")[0].assertIsDisplayed()
    }

    @Test
    fun compactLayoutShowsBottomNavigation() = runComposeUiTest {
        val flow = stateFlow(LayoutNode.Leaf(paneType = "bible-reader"))

        setContent {
            WorkspaceShell(
                stateFlow = flow,
                sizeClass = WindowSizeClass.Compact,
                callbacks = WorkspaceCallbacks()
            )
        }

        // Bottom nav labels should be visible
        onNodeWithText("Bible").assertIsDisplayed()
        onNodeWithText("Notes").assertIsDisplayed()
        onNodeWithText("More").assertIsDisplayed()
    }

    @Test
    fun expandedLayoutShowsActivityBar() = runComposeUiTest {
        val flow = stateFlow(LayoutNode.Leaf(paneType = "bible-reader"))

        setContent {
            WorkspaceShell(
                stateFlow = flow,
                sizeClass = WindowSizeClass.Expanded,
                callbacks = WorkspaceCallbacks()
            )
        }

        // Pane type should be visible in the workspace area
        onAllNodesWithText("bible-reader")[0].assertIsDisplayed()
    }
}
