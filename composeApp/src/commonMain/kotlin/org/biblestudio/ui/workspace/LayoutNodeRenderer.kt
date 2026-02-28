package org.biblestudio.ui.workspace

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.biblestudio.features.workspace.domain.model.LayoutNode
import org.biblestudio.features.workspace.domain.model.SplitAxis
import org.biblestudio.ui.theme.PaneStyling
import org.biblestudio.ui.theme.Spacing
import kotlin.math.abs
import kotlin.math.roundToInt

private val DRAG_HANDLE_SIZE = 6.dp
private val DRAG_HANDLE_HIT_SIZE = 16.dp
private val PANE_HEADER_ICON_SIZE = 20.dp
private val TAB_CLOSE_SIZE = 16.dp
private val TAB_CLOSE_ICON_SIZE = 12.dp

/**
 * CompositionLocal that individual panes can provide to inject
 * toolbar content into the [PaneHeaderBar].
 */
val LocalPaneToolbar = compositionLocalOf<(@Composable () -> Unit)?> { null }

/**
 * Recursively renders a [LayoutNode] tree into Compose UI.
 *
 * - [LayoutNode.Leaf] → [PaneContainer] with header bar
 * - [LayoutNode.Split] → Row/Column with drag handle
 * - [LayoutNode.Tabs] → Column with clickable tab bar + active pane
 *
 * Pane content is dispatched by [PaneContent] based on the pane type string.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun LayoutNodeRenderer(
    node: LayoutNode,
    modifier: Modifier = Modifier,
    callbacks: PaneCallbacks = PaneCallbacks(),
    path: List<Int> = emptyList(),
    dragState: WorkspaceDragState = remember { WorkspaceDragState() }
) {
    when (node) {
        is LayoutNode.Leaf -> PaneContainer(
            paneType = node.paneType,
            modifier = modifier,
            callbacks = callbacks,
            path = path,
            dragState = dragState
        )

        is LayoutNode.Split -> SplitContainer(
            split = node,
            modifier = modifier,
            callbacks = callbacks,
            path = path,
            dragState = dragState
        )

        is LayoutNode.Tabs -> TabContainer(
            tabs = node,
            modifier = modifier,
            callbacks = callbacks,
            path = path,
            dragState = dragState
        )
    }
}

@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun SplitContainer(
    split: LayoutNode.Split,
    modifier: Modifier = Modifier,
    callbacks: PaneCallbacks = PaneCallbacks(),
    path: List<Int> = emptyList(),
    dragState: WorkspaceDragState = remember { WorkspaceDragState() }
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    // Local ratio for immediate visual feedback during drag.
    // Re-syncs from the model ratio whenever it changes externally.
    var localRatio by remember { mutableFloatStateOf(split.ratio) }
    var isDraggingHandle by remember { mutableStateOf(false) }
    if (!isDraggingHandle && localRatio != split.ratio) {
        localRatio = split.ratio
    }

    when (split.axis) {
        SplitAxis.Horizontal -> Row(
            modifier = modifier.fillMaxSize().onSizeChanged { containerSize = it }
        ) {
            Box(modifier = Modifier.weight(localRatio.coerceIn(0.1f, 0.9f)).fillMaxHeight()) {
                LayoutNodeRenderer(split.first, Modifier.fillMaxSize(), callbacks, path + 0, dragState)
            }
            DragHandle(
                isVertical = true,
                containerSize = containerSize,
                currentRatio = localRatio,
                onDragStart = { isDraggingHandle = true },
                onRatioChange = { newRatio ->
                    localRatio = newRatio
                    callbacks.onResizeSplit(path, newRatio)
                },
                onDragEnd = { isDraggingHandle = false }
            )
            Box(modifier = Modifier.weight((1f - localRatio).coerceIn(0.1f, 0.9f)).fillMaxHeight()) {
                LayoutNodeRenderer(split.second, Modifier.fillMaxSize(), callbacks, path + 1, dragState)
            }
        }

        SplitAxis.Vertical -> Column(
            modifier = modifier.fillMaxSize().onSizeChanged { containerSize = it }
        ) {
            Box(modifier = Modifier.weight(localRatio.coerceIn(0.1f, 0.9f)).fillMaxWidth()) {
                LayoutNodeRenderer(split.first, Modifier.fillMaxSize(), callbacks, path + 0, dragState)
            }
            DragHandle(
                isVertical = false,
                containerSize = containerSize,
                currentRatio = localRatio,
                onDragStart = { isDraggingHandle = true },
                onRatioChange = { newRatio ->
                    localRatio = newRatio
                    callbacks.onResizeSplit(path, newRatio)
                },
                onDragEnd = { isDraggingHandle = false }
            )
            Box(modifier = Modifier.weight((1f - localRatio).coerceIn(0.1f, 0.9f)).fillMaxWidth()) {
                LayoutNodeRenderer(split.second, Modifier.fillMaxSize(), callbacks, path + 1, dragState)
            }
        }
    }
}

/**
 * Draggable divider between split panes.
 *
 * Uses [rememberUpdatedState] so the [pointerInput] coroutine always reads
 * the latest [currentRatio] on drag start and the latest [onRatioChange]
 * callback — no stale closures.
 */
@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun DragHandle(
    isVertical: Boolean,
    containerSize: IntSize,
    currentRatio: Float,
    onDragStart: () -> Unit,
    onRatioChange: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgColor = if (isHovered) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    // rememberUpdatedState keeps the coroutine seeing the latest values
    val latestRatio by rememberUpdatedState(currentRatio)
    val latestOnChange by rememberUpdatedState(onRatioChange)
    val latestOnStart by rememberUpdatedState(onDragStart)
    val latestOnEnd by rememberUpdatedState(onDragEnd)

    val cursorIcon = PointerIcon.Crosshair

    val gestureModifier = Modifier
        .hoverable(interactionSource)
        .pointerHoverIcon(cursorIcon)
        .pointerInput(isVertical) {
            var ratio = 0f
            detectDragGestures(
                onDragStart = {
                    ratio = latestRatio
                    latestOnStart()
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val total = if (isVertical) containerSize.width.toFloat()
                    else containerSize.height.toFloat()
                    if (total > 0f) {
                        val delta = if (isVertical) dragAmount.x / total
                        else dragAmount.y / total
                        ratio = (ratio + delta).coerceIn(0.1f, 0.9f)
                        latestOnChange(ratio)
                    }
                },
                onDragEnd = { latestOnEnd() },
                onDragCancel = { latestOnEnd() }
            )
        }

    // Outer Box = wide hit area; inner Box = thin visible bar
    if (isVertical) {
        Box(contentAlignment = Alignment.Center, modifier = gestureModifier.width(DRAG_HANDLE_HIT_SIZE).fillMaxHeight()) {
            Box(modifier = Modifier.width(DRAG_HANDLE_SIZE).fillMaxHeight().background(bgColor))
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = gestureModifier.height(DRAG_HANDLE_HIT_SIZE).fillMaxWidth()) {
            Box(modifier = Modifier.height(DRAG_HANDLE_SIZE).fillMaxWidth().background(bgColor))
        }
    }
}

// ── Tab Container with manual drag-to-reorder + detach ──

private data class TabDragInfo(
    val sourceIndex: Int,
    val offsetX: Float,
    val offsetY: Float = 0f,
    val detached: Boolean = false
)

private const val TAB_DETACH_THRESHOLD = 40f

@Suppress("ktlint:standard:function-naming", "LongMethod", "MagicNumber")
@Composable
private fun TabContainer(
    tabs: LayoutNode.Tabs,
    modifier: Modifier = Modifier,
    callbacks: PaneCallbacks = PaneCallbacks(),
    path: List<Int> = emptyList(),
    dragState: WorkspaceDragState = remember { WorkspaceDragState() }
) {
    var tabDrag by remember { mutableStateOf<TabDragInfo?>(null) }
    var tabBarCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val lazyListState = rememberLazyListState()

    // Keep callbacks fresh for pointerInput coroutines
    val latestCallbacks by rememberUpdatedState(callbacks)
    val latestDragState by rememberUpdatedState(dragState)
    val latestTabs by rememberUpdatedState(tabs)

    Column(modifier = modifier.fillMaxSize()) {
        // Tab bar — LazyRow with manual drag gestures
        LazyRow(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = Spacing.Space4)
                .onGloballyPositioned { tabBarCoords = it },
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(tabs.children, key = { _, child -> child.paneType }) { index, child ->
                val drag = tabDrag
                val isDragging = drag != null && drag.sourceIndex == index && !drag.detached
                val isActive = index == tabs.activeIndex
                val info = remember(child.paneType) { PaneStyling.paneInfo(child.paneType) }
                val tabPath = path + index
                var showContextMenu by remember { mutableStateOf(false) }
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                Surface(
                    shadowElevation = elevation,
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        .animateItem()
                        .graphicsLayer {
                            if (isDragging) {
                                translationX = drag!!.offsetX
                                alpha = 0.85f
                            }
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .pointerInput(index, tabs.children.size) {
                                detectDragGestures(
                                    onDragStart = {
                                        tabDrag = TabDragInfo(sourceIndex = index, offsetX = 0f, offsetY = 0f)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val current = tabDrag ?: return@detectDragGestures
                                        if (current.detached) {
                                            // Forward pointer to workspace drag overlay
                                            tabBarCoords?.let { coords ->
                                                val windowPos = coords.localToWindow(change.position)
                                                latestDragState.updatePointer(windowPos)
                                            }
                                            return@detectDragGestures
                                        }
                                        val newX = current.offsetX + dragAmount.x
                                        val newY = current.offsetY + dragAmount.y
                                        if (abs(newY) > TAB_DETACH_THRESHOLD && latestTabs.children.size > 1) {
                                            // Detach: start workspace drag
                                            tabDrag = current.copy(offsetX = newX, offsetY = newY, detached = true)
                                            latestDragState.startDrag(child.paneType, path + current.sourceIndex)
                                            tabBarCoords?.let { coords ->
                                                val windowPos = coords.localToWindow(change.position)
                                                latestDragState.updatePointer(windowPos)
                                            }
                                        } else {
                                            tabDrag = current.copy(offsetX = newX, offsetY = newY)
                                        }
                                    },
                                    onDragEnd = {
                                        val final = tabDrag
                                        tabDrag = null
                                        if (final == null) return@detectDragGestures
                                        if (final.detached) {
                                            val result = latestDragState.endDrag()
                                            if (result != null) {
                                                val (dragInfo, target) = result
                                                latestCallbacks.onRearrangePane(
                                                    dragInfo.sourcePath,
                                                    target.path,
                                                    target.zone.toPanePlacement()
                                                )
                                            }
                                        } else {
                                            // Calculate target index from horizontal offset
                                            val avgWidth = lazyListState.layoutInfo.visibleItemsInfo
                                                .map { it.size }.average().toFloat()
                                                .takeIf { it > 0f } ?: 100f
                                            val targetIndex = (final.sourceIndex + (final.offsetX / avgWidth).roundToInt())
                                                .coerceIn(0, latestTabs.children.lastIndex)
                                            if (targetIndex != final.sourceIndex) {
                                                latestCallbacks.onReorderTab(path, final.sourceIndex, targetIndex)
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        val final = tabDrag
                                        tabDrag = null
                                        if (final?.detached == true) {
                                            latestDragState.cancelDrag()
                                        }
                                    }
                                )
                            }
                            .clickable { latestCallbacks.onSwitchTab(path, index) }
                            .padding(horizontal = Spacing.Space8, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = info.icon,
                            contentDescription = null,
                            tint = if (isActive) info.accentColor
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(TAB_CLOSE_SIZE)
                        )
                        Spacer(Modifier.width(Spacing.Space4))
                        Text(
                            text = info.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                        // Per-tab close button (only if more than 1 tab)
                        if (tabs.children.size > 1) {
                            IconButton(
                                onClick = { latestCallbacks.onCloseAtPath(tabPath) },
                                modifier = Modifier.size(TAB_CLOSE_SIZE)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close ${info.displayName}",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(TAB_CLOSE_ICON_SIZE)
                                )
                            }
                        }
                    }

                    // Context menu for right-click
                    TabContextMenu(
                        expanded = showContextMenu,
                        onDismiss = { showContextMenu = false },
                        tabPath = tabPath,
                        tabIndex = index,
                        totalTabs = tabs.children.size,
                        paneType = child.paneType,
                        callbacks = callbacks
                    )
                }
            }
        }

        // Active pane content
        val activeChild = tabs.children.getOrNull(tabs.activeIndex)
        if (activeChild != null) {
            PaneContainer(
                paneType = activeChild.paneType,
                modifier = Modifier.fillMaxSize(),
                callbacks = callbacks,
                path = path + tabs.activeIndex,
                dragState = dragState
            )
        }
    }
}

/**
 * Context menu dropdown for a tab in the tab bar.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun TabContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    tabPath: List<Int>,
    tabIndex: Int,
    totalTabs: Int,
    paneType: String,
    callbacks: PaneCallbacks
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Close") },
            onClick = { onDismiss(); callbacks.onCloseAtPath(tabPath) }
        )
        if (totalTabs > 1) {
            DropdownMenuItem(
                text = { Text("Close Others") },
                onClick = {
                    onDismiss()
                    val parentPath = tabPath.dropLast(1)
                    for (i in (totalTabs - 1) downTo 0) {
                        if (i != tabIndex) callbacks.onCloseAtPath(parentPath + i)
                    }
                }
            )
            DropdownMenuItem(
                text = { Text("Close All") },
                onClick = {
                    onDismiss()
                    val parentPath = tabPath.dropLast(1)
                    for (i in (totalTabs - 1) downTo 0) {
                        callbacks.onCloseAtPath(parentPath + i)
                    }
                }
            )
        }
        DropdownMenuItem(
            text = { Text("Split Right") },
            onClick = { onDismiss(); callbacks.onSplitHorizontal(paneType) }
        )
        DropdownMenuItem(
            text = { Text("Split Down") },
            onClick = { onDismiss(); callbacks.onSplitVertical(paneType) }
        )
    }
}

// ── Pane Container with drag source + drop target ───────────

/**
 * Container for a single pane with a header bar, drag-to-move, and drop zone overlays.
 *
 * The header is a drag source for pane rearrangement. When another pane is
 * being dragged, this container shows coloured overlay zones indicating where
 * the dragged pane would be placed (left, right, top, bottom, center=tab).
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun PaneContainer(
    paneType: String,
    modifier: Modifier = Modifier,
    callbacks: PaneCallbacks = PaneCallbacks(),
    path: List<Int> = emptyList(),
    dragState: WorkspaceDragState = remember { WorkspaceDragState() }
) {
    // Register this pane's bounds for drop-target detection
    DisposableEffect(path) {
        onDispose { dragState.unregisterPane(path) }
    }

    // Check if this pane is the current drop target
    val isDropTarget = dragState.dropTarget?.path == path
    val activeZone = if (isDropTarget) dragState.dropTarget?.zone else null
    // Dim the source pane while it is being dragged
    val isBeingDragged = dragState.isDragging && dragState.dragInfo?.sourcePath == path

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = if (isBeingDragged) 0.4f else 1f }
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            .onGloballyPositioned { coords ->
                val pos = coords.positionInWindow()
                dragState.registerPane(
                    path,
                    Rect(
                        pos.x, pos.y,
                        pos.x + coords.size.width.toFloat(),
                        pos.y + coords.size.height.toFloat()
                    )
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PaneHeaderBar(
                paneType = paneType,
                callbacks = callbacks,
                path = path,
                dragState = dragState
            )

            // Real pane content
            PaneContent(
                paneType = paneType,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Drop zone overlay (shown only when another pane is being dragged over this one)
        if (dragState.isDragging && activeZone != null) {
            DropZoneOverlay(activeZone)
        }
    }
}

/**
 * Semi-transparent overlay indicating where a dragged pane would be placed.
 */
@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun DropZoneOverlay(zone: DropZone) {
    val overlayColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val borderColor = MaterialTheme.colorScheme.primary
    Box(modifier = Modifier.fillMaxSize()) {
        when (zone) {
            DropZone.LEFT -> Box(
                Modifier.fillMaxHeight().fillMaxWidth(0.5f)
                    .align(Alignment.CenterStart)
                    .background(overlayColor)
                    .border(2.dp, borderColor)
            )
            DropZone.RIGHT -> Box(
                Modifier.fillMaxHeight().fillMaxWidth(0.5f)
                    .align(Alignment.CenterEnd)
                    .background(overlayColor)
                    .border(2.dp, borderColor)
            )
            DropZone.TOP -> Box(
                Modifier.fillMaxWidth().fillMaxHeight(0.5f)
                    .align(Alignment.TopCenter)
                    .background(overlayColor)
                    .border(2.dp, borderColor)
            )
            DropZone.BOTTOM -> Box(
                Modifier.fillMaxWidth().fillMaxHeight(0.5f)
                    .align(Alignment.BottomCenter)
                    .background(overlayColor)
                    .border(2.dp, borderColor)
            )
            DropZone.CENTER -> Box(
                Modifier.fillMaxSize()
                    .background(overlayColor)
                    .border(2.dp, borderColor)
            )
        }
    }
}

/**
 * Header bar for every pane: [drag handle] icon - display name - spacer - overflow - close.
 *
 * The left part (icon + name) is a drag source for pane rearrangement.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun PaneHeaderBar(
    paneType: String,
    callbacks: PaneCallbacks,
    path: List<Int> = emptyList(),
    dragState: WorkspaceDragState = remember { WorkspaceDragState() }
) {
    val info = remember(paneType) { PaneStyling.paneInfo(paneType) }
    var layoutCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Spacing.Space8, vertical = Spacing.Space4)
    ) {
        // Drag handle area (icon + display name) — draggable
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { layoutCoords = it }
                .pointerInput(paneType, path) {
                    detectDragGestures(
                        onDragStart = {
                            dragState.startDrag(paneType, path)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            layoutCoords?.let { coords ->
                                val windowPos = coords.localToWindow(change.position)
                                dragState.updatePointer(windowPos)
                            }
                        },
                        onDragEnd = {
                            val result = dragState.endDrag()
                            if (result != null) {
                                val (dragInfo, target) = result
                                callbacks.onRearrangePane(
                                    dragInfo.sourcePath,
                                    target.path,
                                    target.zone.toPanePlacement()
                                )
                            }
                        },
                        onDragCancel = { dragState.cancelDrag() }
                    )
                }
                .pointerHoverIcon(PointerIcon.Hand)
        ) {
            // Category-coloured icon
            Icon(
                imageVector = info.icon,
                contentDescription = null,
                tint = info.accentColor,
                modifier = Modifier.size(PANE_HEADER_ICON_SIZE)
            )

            Spacer(modifier = Modifier.width(Spacing.Space8))

            // Human-readable display name
            Text(
                text = info.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Toolbar slot (provided by individual panes via LocalPaneToolbar)
        LocalPaneToolbar.current?.invoke()

        // Close button
        IconButton(
            onClick = {
                if (path.isNotEmpty()) callbacks.onCloseAtPath(path)
                else callbacks.onClose(paneType)
            },
            modifier = Modifier.size(PANE_HEADER_ICON_SIZE)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close pane",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(PANE_HEADER_ICON_SIZE)
            )
        }
    }
}
