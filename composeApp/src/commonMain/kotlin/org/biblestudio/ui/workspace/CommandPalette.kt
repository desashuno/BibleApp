package org.biblestudio.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.biblestudio.core.pane_registry.PaneRegistry
import org.biblestudio.ui.theme.PaneStyling
import org.biblestudio.ui.theme.Spacing

private val PALETTE_WIDTH = 480.dp
private val PALETTE_MAX_HEIGHT = 360.dp
private val ITEM_ICON_SIZE = 20.dp

/**
 * VS Code-style command palette overlay.
 *
 * Shows a search field and a filtered list of all available pane types.
 * Selecting an item opens that pane in the workspace.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun CommandPalette(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onPaneSelected: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    if (!isVisible) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.width(PALETTE_WIDTH)
        ) {
            Column(modifier = Modifier.padding(Spacing.Space8)) {
                var query by remember { mutableStateOf("") }
                val focusRequester = remember { FocusRequester() }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search panes and actions\u2026") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                LaunchedEffect(Unit) { focusRequester.requestFocus() }

                Spacer(Modifier.height(Spacing.Space8))

                val allItems = remember { buildPaletteItems() }
                val filtered = remember(query) {
                    if (query.isBlank()) allItems
                    else allItems.filter {
                        it.label.contains(query, ignoreCase = true) ||
                            it.category.contains(query, ignoreCase = true)
                    }
                }

                LazyColumn(modifier = Modifier.height(PALETTE_MAX_HEIGHT)) {
                    items(filtered, key = { it.id }) { item ->
                        PaletteItem(
                            item = item,
                            onClick = {
                                when (item.id) {
                                    "action:settings" -> onSettingsClick()
                                    else -> onPaneSelected(item.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun PaletteItem(item: PaletteEntry, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Space8, vertical = Spacing.Space8)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = item.accentColor,
            modifier = Modifier.size(ITEM_ICON_SIZE)
        )
        Spacer(Modifier.width(Spacing.Space8))
        Column {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

private data class PaletteEntry(
    val id: String,
    val label: String,
    val category: String,
    val icon: ImageVector,
    val accentColor: Color
)

private fun buildPaletteItems(): List<PaletteEntry> {
    val paneItems = PaneRegistry.availableTypes.mapNotNull { type ->
        try {
            val info = PaneStyling.paneInfo(type)
            PaletteEntry(
                id = type,
                label = info.displayName,
                category = info.category.name,
                icon = info.icon,
                accentColor = info.accentColor
            )
        } catch (_: Exception) {
            null
        }
    }

    val settingsItem = PaletteEntry(
        id = "action:settings",
        label = "Settings",
        category = "Action",
        icon = Icons.Default.Settings,
        accentColor = Color.Gray
    )

    return paneItems + settingsItem
}
