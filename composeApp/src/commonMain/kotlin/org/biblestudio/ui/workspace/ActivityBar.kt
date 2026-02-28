package org.biblestudio.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.biblestudio.core.pane_registry.PaneRegistry
import org.biblestudio.ui.theme.PaneDisplayInfo
import org.biblestudio.ui.theme.PaneStyling
import org.biblestudio.ui.theme.Spacing

private val ACTIVITY_BAR_WIDTH_EXPANDED = 80.dp
private val ACTIVITY_BAR_WIDTH_COLLAPSED = 44.dp
private val ICON_SIZE = 22.dp
private val ACTIVE_INDICATOR_WIDTH = 3.dp
private val ITEM_HEIGHT_EXPANDED = 56.dp
private val ITEM_HEIGHT_COLLAPSED = 40.dp
private val LABEL_FONT_SIZE = 9.sp
private val ADD_BUTTON_SIZE = 36.dp

/**
 * Vertical icon+label bar on the left edge of the desktop workspace.
 *
 * Shows user-pinned panes ordered by [PaneRegistry] registration order.
 * A '+' button opens the [ModulePicker] to browse all modules.
 * Settings is pinned to the bottom.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun ActivityBar(
    onPaneSelected: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onShowModulePicker: () -> Unit,
    onToggleWorkspaceSwitcher: () -> Unit = {},
    activePaneType: String? = null,
    pinnedPanes: Set<String> = emptySet(),
    isCollapsed: Boolean = false,
    onToggleCollapse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val barWidth = if (isCollapsed) ACTIVITY_BAR_WIDTH_COLLAPSED else ACTIVITY_BAR_WIDTH_EXPANDED
    val itemHeight = if (isCollapsed) ITEM_HEIGHT_COLLAPSED else ITEM_HEIGHT_EXPANDED

    // Order pinned panes by PaneRegistry registration order
    val orderedPinned = remember(pinnedPanes) {
        PaneRegistry.availableTypes.filter { it in pinnedPanes }
    }

    Column(
        modifier = modifier
            .width(barWidth)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section -- collapse toggle + pinned pane shortcuts (scrollable)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Collapse/expand toggle
            IconButton(
                onClick = onToggleCollapse,
                modifier = Modifier.padding(top = Spacing.Space4)
            ) {
                Icon(
                    imageVector = if (isCollapsed) {
                        Icons.AutoMirrored.Filled.KeyboardArrowRight
                    } else {
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    },
                    contentDescription = if (isCollapsed) "Expand sidebar" else "Collapse sidebar",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(ICON_SIZE)
                )
            }

            orderedPinned.forEach { paneType ->
                val info = remember(paneType) { PaneStyling.paneInfo(paneType) }
                val isActive = activePaneType == paneType

                ActivityBarItem(
                    info = info,
                    isActive = isActive,
                    isCollapsed = isCollapsed,
                    itemHeight = itemHeight,
                    onClick = { onPaneSelected(paneType) }
                )
            }

            // '+' button to open module picker
            Spacer(Modifier.height(Spacing.Space8))
            IconButton(
                onClick = onShowModulePicker,
                modifier = Modifier.size(ADD_BUTTON_SIZE)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Open module picker",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(ICON_SIZE)
                )
            }
        }

        // Bottom section -- desktops + settings
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = Spacing.Space8)
        ) {
            // Desktops (workspace switcher) button
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = "Desktops",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(ICON_SIZE)
                    .clickable(onClick = onToggleWorkspaceSwitcher)
            )
            if (!isCollapsed) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Desktops",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = LABEL_FONT_SIZE),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(Spacing.Space12))

            // Settings button
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(ICON_SIZE)
                    .clickable(onClick = onSettingsClick)
            )
            if (!isCollapsed) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = LABEL_FONT_SIZE),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ActivityBarItem(
    info: PaneDisplayInfo,
    isActive: Boolean,
    isCollapsed: Boolean,
    itemHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .clickable(onClick = onClick)
            .then(
                if (isActive) {
                    Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                } else {
                    Modifier
                }
            )
    ) {
        // Active indicator bar on the left edge
        if (isActive) {
            Box(
                modifier = Modifier
                    .width(ACTIVE_INDICATOR_WIDTH)
                    .height(itemHeight)
                    .background(
                        color = info.accentColor,
                        shape = RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                    )
            )
        } else {
            Box(modifier = Modifier.width(ACTIVE_INDICATOR_WIDTH))
        }

        // Icon + Label stacked vertically
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = info.icon,
                contentDescription = info.displayName,
                tint = if (isActive) info.accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(ICON_SIZE)
            )
            if (!isCollapsed) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = info.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = LABEL_FONT_SIZE),
                    color = if (isActive) info.accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
