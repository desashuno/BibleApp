package org.biblestudio.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.settings.component.SavedLayout
import org.biblestudio.features.workspace.domain.model.LayoutNode
import org.biblestudio.features.workspace.domain.model.WorkspaceState
import org.biblestudio.ui.layout.WindowSizeClass
import org.biblestudio.ui.theme.PaneStyling
import org.biblestudio.ui.theme.Spacing

private val STATUS_BAR_HEIGHT = 24.dp

/**
 * Top-level workspace composable that combines the [ActivityBar] (desktop)
 * or [BottomNavBar] (mobile) with the [LayoutNodeRenderer] content area.
 *
 * Includes a status bar (desktop), keyboard shortcuts, and command palette.
 * Shows a [WelcomeScreen] when no panes are open.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun WorkspaceShell(
    stateFlow: StateFlow<WorkspaceState>,
    pinnedPanes: Set<String>,
    favoritePanes: Set<String>,
    savedLayouts: List<SavedLayout> = emptyList(),
    sizeClass: WindowSizeClass,
    callbacks: WorkspaceCallbacks,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    val dragState = remember { WorkspaceDragState() }
    val paneCallbacks = remember(callbacks) {
        PaneCallbacks(
            onClose = callbacks.onPaneClose,
            onCloseAtPath = callbacks.onPaneCloseAtPath,
            onSplitHorizontal = callbacks.onSplitHorizontal,
            onSplitVertical = callbacks.onSplitVertical,
            onResizeSplit = callbacks.onResizeSplit,
            onSwitchTab = callbacks.onSwitchTab,
            onReorderTab = callbacks.onReorderTab,
            onRearrangePane = callbacks.onRearrangePane
        )
    }

    // Determine the "active" pane type for navigation bar highlighting
    val activePaneType = remember(state.layout) { state.layout?.let { findFirstLeaf(it)?.paneType } }
    val paneCount = remember(state.layout) { state.layout?.let { countLeaves(it) } ?: 0 }

    // Command palette state
    var showCommandPalette by remember { mutableStateOf(false) }

    // Sidebar collapsed state
    var sidebarCollapsed by remember { mutableStateOf(false) }

    // Module picker state
    var showModulePicker by remember { mutableStateOf(false) }

    // Workspace switcher state
    var showWorkspaceSwitcher by remember { mutableStateOf(false) }

    // Keyboard shortcut handler
    val keyboardModifier = Modifier.onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when {
            // Ctrl+Shift+P → Command palette
            event.isCtrlPressed && event.isShiftPressed && event.key == Key.P -> {
                showCommandPalette = !showCommandPalette
                true
            }
            // Ctrl+W → Close active pane
            event.isCtrlPressed && event.key == Key.W -> {
                activePaneType?.let { callbacks.onPaneClose(it) }
                true
            }
            // Ctrl+\ → Split active pane horizontal
            event.isCtrlPressed && event.key == Key.Backslash -> {
                activePaneType?.let { callbacks.onSplitHorizontal(it) }
                true
            }
            // Ctrl+B → Toggle sidebar
            event.isCtrlPressed && !event.isShiftPressed && event.key == Key.B -> {
                sidebarCollapsed = !sidebarCollapsed
                true
            }
            else -> false
        }
    }

    // Command palette overlay
    if (showCommandPalette) {
        CommandPalette(
            isVisible = true,
            onDismiss = { showCommandPalette = false },
            onPaneSelected = { type ->
                callbacks.onPaneSelected(type)
                showCommandPalette = false
            },
            onSettingsClick = {
                callbacks.onSettingsClick()
                showCommandPalette = false
            }
        )
    }

    // Module picker overlay
    if (showModulePicker) {
        ModulePicker(
            pinnedPanes = pinnedPanes,
            favoritePanes = favoritePanes,
            onPaneSelected = { type ->
                callbacks.onPaneSelected(type)
                showModulePicker = false
            },
            onTogglePinned = callbacks.onTogglePinned,
            onToggleFavorite = callbacks.onToggleFavorite,
            onDismiss = { showModulePicker = false }
        )
    }

    Surface(modifier = modifier.fillMaxSize().then(keyboardModifier)) {
        when (sizeClass) {
            WindowSizeClass.Compact,
            WindowSizeClass.Medium
            -> {
                // Mobile / tablet — layout + bottom navigation
                Column(modifier = Modifier.fillMaxSize()) {
                    val layout = state.layout
                    if (layout != null) {
                        LayoutNodeRenderer(
                            node = layout,
                            modifier = Modifier.weight(1f),
                            callbacks = paneCallbacks,
                            dragState = dragState
                        )
                    } else {
                        WelcomeScreen(
                            onPaneSelected = callbacks.onPaneSelected,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BottomNavBar(
                        onPaneSelected = callbacks.onPaneSelected,
                        activePaneType = activePaneType
                    )
                }
            }

            WindowSizeClass.Expanded,
            WindowSizeClass.Large
            -> {
                // Desktop — activity bar + content + status bar
                Row(modifier = Modifier.fillMaxSize()) {
                    ActivityBar(
                        onPaneSelected = callbacks.onPaneSelected,
                        onSettingsClick = callbacks.onSettingsClick,
                        onShowModulePicker = { showModulePicker = true },
                        onToggleWorkspaceSwitcher = { showWorkspaceSwitcher = !showWorkspaceSwitcher },
                        activePaneType = activePaneType,
                        pinnedPanes = pinnedPanes,
                        isCollapsed = sidebarCollapsed,
                        onToggleCollapse = { sidebarCollapsed = !sidebarCollapsed }
                    )
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        // Wrap layout area in a Box for the drag preview overlay
                        var layoutAreaOffset by remember { mutableStateOf(Offset.Zero) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned { coords ->
                                    layoutAreaOffset = coords.positionInWindow()
                                }
                        ) {
                            if (showWorkspaceSwitcher) {
                                // Android-style desktop overview
                                DesktopOverview(
                                    savedLayouts = savedLayouts,
                                    activeLayout = state.layout,
                                    onLoadWorkspace = { id ->
                                        callbacks.onLoadWorkspace(id)
                                        showWorkspaceSwitcher = false
                                    },
                                    onCreateWorkspace = { name ->
                                        callbacks.onCreateWorkspace(name)
                                        showWorkspaceSwitcher = false
                                    },
                                    onDeleteWorkspace = callbacks.onDeleteWorkspace,
                                    onDismiss = { showWorkspaceSwitcher = false }
                                )
                            } else {
                                val layout = state.layout
                                if (layout != null) {
                                    LayoutNodeRenderer(
                                        node = layout,
                                        modifier = Modifier.fillMaxSize(),
                                        callbacks = paneCallbacks,
                                        dragState = dragState
                                    )
                                } else {
                                    WelcomeScreen(
                                        onPaneSelected = callbacks.onPaneSelected,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                // Floating drag preview
                                DragPreviewOverlay(
                                    dragState = dragState,
                                    layoutAreaOffset = layoutAreaOffset
                                )
                            }
                        }
                        // Status bar
                        StatusBar(
                            activePaneType = activePaneType,
                            paneCount = paneCount,
                            workspaceName = state.workspaceName
                        )
                    }
                }
            }
        }
    }
}

private val DRAG_PREVIEW_ICON_SIZE = 16.dp
private val DRAG_PREVIEW_OFFSET_X = -20f
private val DRAG_PREVIEW_OFFSET_Y = -20f

/**
 * Floating preview card that follows the cursor during a pane drag operation.
 * Renders a semi-transparent card showing the pane icon and display name.
 */
@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun DragPreviewOverlay(
    dragState: WorkspaceDragState,
    layoutAreaOffset: Offset
) {
    val info = dragState.dragInfo ?: return
    val pos = dragState.pointerPosition
    if (pos == Offset.Unspecified) return

    val paneInfo = remember(info.paneType) { PaneStyling.paneInfo(info.paneType) }
    // Convert window coords to local layout area coords
    val localX = pos.x - layoutAreaOffset.x + DRAG_PREVIEW_OFFSET_X
    val localY = pos.y - layoutAreaOffset.y + DRAG_PREVIEW_OFFSET_Y

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
        shadowElevation = 8.dp,
        modifier = Modifier
            .graphicsLayer {
                translationX = localX
                translationY = localY
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.Space8, vertical = Spacing.Space4)
        ) {
            Icon(
                imageVector = paneInfo.icon,
                contentDescription = null,
                tint = paneInfo.accentColor,
                modifier = Modifier.size(DRAG_PREVIEW_ICON_SIZE)
            )
            Spacer(Modifier.width(Spacing.Space4))
            Text(
                text = paneInfo.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Status bar at the bottom of the desktop workspace.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun StatusBar(
    activePaneType: String?,
    paneCount: Int,
    workspaceName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(STATUS_BAR_HEIGHT)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = Spacing.Space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = activePaneType ?: "No pane",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Space16)) {
            Text(
                text = workspaceName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "$paneCount panes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Walks the layout tree and returns the first [LayoutNode.Leaf] found
 * (depth-first, left-first). Used to determine the "active" pane type
 * for navigation bar highlighting.
 */
private fun findFirstLeaf(node: LayoutNode): LayoutNode.Leaf? {
    return when (node) {
        is LayoutNode.Leaf -> node
        is LayoutNode.Split -> findFirstLeaf(node.first) ?: findFirstLeaf(node.second)
        is LayoutNode.Tabs -> {
            val active = node.children.getOrNull(node.activeIndex)
            active ?: node.children.firstOrNull()
        }
    }
}

/** Counts all leaf panes in the layout tree. */
private fun countLeaves(node: LayoutNode): Int = when (node) {
    is LayoutNode.Leaf -> 1
    is LayoutNode.Split -> countLeaves(node.first) + countLeaves(node.second)
    is LayoutNode.Tabs -> node.children.size
}
