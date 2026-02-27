package org.biblestudio.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.biblestudio.ui.theme.Spacing

private val ACTIVITY_BAR_WIDTH = 48.dp
private val ICON_SIZE = 24.dp

/**
 * Vertical icon bar on the left edge of the desktop workspace (48 dp wide).
 *
 * Shows quick-access icons for the most common pane types plus a
 * settings button pinned to the bottom.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun ActivityBar(onPaneSelected: (String) -> Unit, onSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(ACTIVITY_BAR_WIDTH)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section — module shortcuts
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.Space4),
            modifier = Modifier.padding(top = Spacing.Space8)
        ) {
            ActivityBarItem(
                icon = Icons.Default.Home,
                contentDescription = "Bible Reader",
                onClick = { onPaneSelected("bible-reader") }
            )
            ActivityBarItem(
                icon = Icons.Default.Search,
                contentDescription = "Search",
                onClick = { onPaneSelected("search") }
            )
            ActivityBarItem(
                icon = Icons.Default.Edit,
                contentDescription = "Notes",
                onClick = { onPaneSelected("note-editor") }
            )
            ActivityBarItem(
                icon = Icons.AutoMirrored.Filled.List,
                contentDescription = "Resources",
                onClick = { onPaneSelected("resource-library") }
            )
            AddPaneMenu(onPaneSelected = onPaneSelected)
        }

        // Bottom section — settings
        Box(modifier = Modifier.padding(bottom = Spacing.Space8)) {
            ActivityBarItem(
                icon = Icons.Default.Settings,
                contentDescription = "Settings",
                onClick = onSettingsClick
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ActivityBarItem(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .size(ICON_SIZE)
            .clickable(onClick = onClick)
            .padding(Spacing.Space4)
    )
}
