package org.biblestudio.ui.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.biblestudio.features.settings.component.SavedLayout
import org.biblestudio.features.workspace.domain.model.LayoutNode
import org.biblestudio.features.workspace.domain.model.SplitAxis
import org.biblestudio.ui.theme.PaneStyling
import org.biblestudio.ui.theme.Spacing

private val CARD_WIDTH = 280.dp
private val CARD_HEIGHT = 175.dp
private val CARD_SHAPE = RoundedCornerShape(12.dp)
private val CARD_SPACING = 16.dp
private val OVERVIEW_PADDING = 24.dp

/**
 * Full-screen Android-style virtual desktop overview.
 *
 * Replaces the content area when active, showing a horizontal carousel
 * of workspace preview cards with the active workspace's layout rendered
 * as a colour-coded mini-map.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun DesktopOverview(
    savedLayouts: List<SavedLayout>,
    activeLayout: LayoutNode?,
    onLoadWorkspace: (String) -> Unit,
    onCreateWorkspace: (String) -> Unit,
    onDeleteWorkspace: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(OVERVIEW_PADDING)) {
            // ── Header ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(Spacing.Space8))
                Text(
                    text = "Desktops",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // ── Carousel ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(CARD_SPACING),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp)
            ) {
                savedLayouts.forEach { layout ->
                    DesktopCard(
                        name = layout.name,
                        isActive = layout.isActive,
                        layoutPreview = if (layout.isActive) activeLayout else null,
                        onClick = {
                            if (layout.isActive) {
                                onDismiss()
                            } else {
                                onLoadWorkspace(layout.id)
                            }
                        },
                        onDelete = { onDeleteWorkspace(layout.id) }
                    )
                }

                // ── Add new desktop ──
                AddDesktopCard(
                    onClick = { onCreateWorkspace("Desktop ${savedLayouts.size + 1}") }
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────

@Suppress("ktlint:standard:function-naming")
@Composable
private fun DesktopCard(
    name: String,
    isActive: Boolean,
    layoutPreview: LayoutNode?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CARD_SHAPE,
            border = if (isActive) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            },
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .width(CARD_WIDTH)
                .height(CARD_HEIGHT)
                .clickable(onClick = onClick)
        ) {
            Box(Modifier.fillMaxSize()) {
                // Layout mini-map preview (active) or placeholder icon (inactive)
                if (layoutPreview != null) {
                    LayoutMiniMap(
                        node = layoutPreview,
                        modifier = Modifier.fillMaxSize().padding(Spacing.Space8)
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.Space8))

        // Name label with active indicator
        Text(
            text = if (isActive) "\u2605 $name" else name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun AddDesktopCard(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CARD_SHAPE,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
            modifier = Modifier
                .width(CARD_WIDTH)
                .height(CARD_HEIGHT)
                .clickable(onClick = onClick)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Desktop",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(Spacing.Space8))
                Text(
                    text = "New Desktop",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Spacer matching the label height on DesktopCard
        Spacer(Modifier.height(Spacing.Space8))
        Text(
            text = "",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Layout mini-map: simplified colour-coded rectangles showing pane structure
// ────────────────────────────────────────────────────────────────────────────

/**
 * Draws a simplified schematic of a [LayoutNode] tree.
 *
 * Leaf nodes are rendered as coloured rectangles with a 3-letter pane
 * abbreviation, Split nodes are rendered as Row/Column, and Tabs show
 * only the active tab.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
private fun LayoutMiniMap(node: LayoutNode, modifier: Modifier = Modifier) {
    when (node) {
        is LayoutNode.Leaf -> {
            val info = remember(node.paneType) { PaneStyling.paneInfo(node.paneType) }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = info.accentColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, info.accentColor.copy(alpha = 0.3f)),
                modifier = modifier
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = node.paneType.take(3).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = info.accentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        is LayoutNode.Split -> {
            if (node.axis == SplitAxis.Horizontal) {
                Row(
                    modifier = modifier,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    LayoutMiniMap(node.first, Modifier.weight(node.ratio))
                    LayoutMiniMap(node.second, Modifier.weight(1f - node.ratio))
                }
            } else {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    LayoutMiniMap(node.first, Modifier.weight(node.ratio))
                    LayoutMiniMap(node.second, Modifier.weight(1f - node.ratio))
                }
            }
        }

        is LayoutNode.Tabs -> {
            val activeChild =
                node.children.getOrNull(node.activeIndex) ?: node.children.firstOrNull()
            if (activeChild != null) {
                LayoutMiniMap(activeChild, modifier)
            }
        }
    }
}
