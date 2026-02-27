package org.biblestudio.ui.workspace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.workspace.domain.model.WorkspaceState
import org.biblestudio.ui.layout.WindowSizeClass

/**
 * Top-level workspace composable that combines the [ActivityBar] (desktop)
 * or [BottomNavBar] (mobile) with the [LayoutNodeRenderer] content area.
 *
 * All pane manipulation callbacks are threaded through [PaneCallbacks]
 * so the layout tree can trigger close, split, resize, and tab-switch
 * actions that bubble up to the `WorkspaceComponent`.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun WorkspaceShell(
    stateFlow: StateFlow<WorkspaceState>,
    sizeClass: WindowSizeClass,
    callbacks: WorkspaceCallbacks,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    val paneCallbacks = remember(callbacks) {
        PaneCallbacks(
            onClose = callbacks.onPaneClose,
            onSplitHorizontal = callbacks.onSplitHorizontal,
            onSplitVertical = callbacks.onSplitVertical,
            onResizeSplit = callbacks.onResizeSplit,
            onSwitchTab = callbacks.onSwitchTab
        )
    }

    Surface(modifier = modifier.fillMaxSize()) {
        when (sizeClass) {
            WindowSizeClass.Compact,
            WindowSizeClass.Medium
            -> {
                // Mobile / tablet — layout + bottom navigation
                Column(modifier = Modifier.fillMaxSize()) {
                    LayoutNodeRenderer(
                        node = state.layout,
                        modifier = Modifier.weight(1f),
                        callbacks = paneCallbacks
                    )
                    BottomNavBar(onPaneSelected = callbacks.onPaneSelected)
                }
            }

            WindowSizeClass.Expanded,
            WindowSizeClass.Large
            -> {
                // Desktop — activity bar + layout tree
                Row(modifier = Modifier.fillMaxSize()) {
                    ActivityBar(
                        onPaneSelected = callbacks.onPaneSelected,
                        onSettingsClick = callbacks.onSettingsClick
                    )
                    LayoutNodeRenderer(
                        node = state.layout,
                        modifier = Modifier.weight(1f),
                        callbacks = paneCallbacks
                    )
                }
            }
        }
    }
}
