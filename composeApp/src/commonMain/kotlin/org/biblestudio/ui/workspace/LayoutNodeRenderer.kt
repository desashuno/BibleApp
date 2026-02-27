package org.biblestudio.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.biblestudio.features.workspace.domain.model.LayoutNode
import org.biblestudio.features.workspace.domain.model.SplitAxis
import org.biblestudio.ui.theme.Spacing

private val DRAG_HANDLE_SIZE = 6.dp
private val PANE_HEADER_ICON_SIZE = 20.dp

/**
 * Recursively renders a [LayoutNode] tree into Compose UI.
 *
 * - [LayoutNode.Leaf] → [PaneContainer] with header bar
 * - [LayoutNode.Split] → Row/Column with drag handle
 * - [LayoutNode.Tabs] → Column with clickable tab bar + active pane
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun LayoutNodeRenderer(
    node: LayoutNode,
    modifier: Modifier = Modifier,
    callbacks: PaneCallbacks = PaneCallbacks(),
    path: List<Int> = emptyList()
) {
    when (node) {
        is LayoutNode.Leaf -> PaneContainer(
            paneType = node.paneType,
            modifier = modifier,
            callbacks = callbacks
        )

        is LayoutNode.Split -> SplitContainer(
            split = node,
            modifier = modifier,
            callbacks = callbacks,
            path = path
        )

        is LayoutNode.Tabs -> TabContainer(
            tabs = node,
            modifier = modifier,
            callbacks = callbacks,
            path = path
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SplitContainer(
    split: LayoutNode.Split,
    modifier: Modifier = Modifier,
    callbacks: PaneCallbacks = PaneCallbacks(),
    path: List<Int> = emptyList()
) {
    when (split.axis) {
        SplitAxis.Horizontal -> Row(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(split.ratio).fillMaxHeight()) {
                LayoutNodeRenderer(split.first, Modifier.fillMaxSize(), callbacks, path + 0)
            }
            DragHandle(
                isVertical = true,
                onDrag = { delta -> callbacks.onResizeSplit(path, split.ratio + delta) }
            )
            Box(modifier = Modifier.weight(1f - split.ratio).fillMaxHeight()) {
                LayoutNodeRenderer(split.second, Modifier.fillMaxSize(), callbacks, path + 1)
            }
        }

        SplitAxis.Vertical -> Column(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(split.ratio).fillMaxWidth()) {
                LayoutNodeRenderer(split.first, Modifier.fillMaxSize(), callbacks, path + 0)
            }
            DragHandle(
                isVertical = false,
                onDrag = { delta -> callbacks.onResizeSplit(path, split.ratio + delta) }
            )
            Box(modifier = Modifier.weight(1f - split.ratio).fillMaxWidth()) {
                LayoutNodeRenderer(split.second, Modifier.fillMaxSize(), callbacks, path + 1)
            }
        }
    }
}

/** Draggable divider between split panes. */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun DragHandle(isVertical: Boolean, onDrag: (Float) -> Unit) {
    val dragModifier = Modifier.pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            val delta = if (isVertical) {
                dragAmount.x / size.width.toFloat()
            } else {
                dragAmount.y / size.height.toFloat()
            }
            onDrag(delta)
        }
    }

    if (isVertical) {
        Box(
            modifier = dragModifier
                .width(DRAG_HANDLE_SIZE)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    } else {
        Box(
            modifier = dragModifier
                .height(DRAG_HANDLE_SIZE)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TabContainer(
    tabs: LayoutNode.Tabs,
    modifier: Modifier = Modifier,
    callbacks: PaneCallbacks = PaneCallbacks(),
    path: List<Int> = emptyList()
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Tab bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = Spacing.Space8)
        ) {
            tabs.children.forEachIndexed { index, child ->
                val isActive = index == tabs.activeIndex
                Text(
                    text = child.paneType,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    modifier = Modifier
                        .clickable { callbacks.onSwitchTab(path, index) }
                        .padding(Spacing.Space8)
                )
            }
        }

        // Active pane content
        val activeChild = tabs.children.getOrNull(tabs.activeIndex)
        if (activeChild != null) {
            PaneContainer(
                paneType = activeChild.paneType,
                modifier = Modifier.fillMaxSize(),
                callbacks = callbacks
            )
        }
    }
}

/**
 * Container for a single pane with a header bar.
 *
 * The header displays the pane type, a close button, and an overflow
 * menu with split/move actions. The body renders placeholder content
 * that will be replaced by feature modules via `PaneRegistry`.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun PaneContainer(paneType: String, modifier: Modifier = Modifier, callbacks: PaneCallbacks = PaneCallbacks()) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        PaneHeaderBar(paneType = paneType, callbacks = callbacks)

        // Placeholder body — replaced by real content once feature modules ship
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.Space12),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = paneType,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Header bar for every pane: title · spacer · overflow · close.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun PaneHeaderBar(paneType: String, callbacks: PaneCallbacks) {
    var overflowExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.Space8, vertical = Spacing.Space4)
    ) {
        Text(
            text = paneType,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.weight(1f))

        // Overflow menu
        Box {
            IconButton(onClick = { overflowExpanded = true }, modifier = Modifier.size(PANE_HEADER_ICON_SIZE)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Pane options",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(PANE_HEADER_ICON_SIZE)
                )
            }
            DropdownMenu(expanded = overflowExpanded, onDismissRequest = { overflowExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Split Horizontal") },
                    onClick = {
                        overflowExpanded = false
                        callbacks.onSplitHorizontal(paneType)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Split Vertical") },
                    onClick = {
                        overflowExpanded = false
                        callbacks.onSplitVertical(paneType)
                    }
                )
            }
        }

        // Close button
        IconButton(onClick = { callbacks.onClose(paneType) }, modifier = Modifier.size(PANE_HEADER_ICON_SIZE)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close pane",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(PANE_HEADER_ICON_SIZE)
            )
        }
    }
}
