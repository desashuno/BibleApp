package org.biblestudio.ui.workspace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.Modifier
import org.biblestudio.core.pane_registry.PaneCategory
import org.biblestudio.core.pane_registry.PaneRegistry
import org.biblestudio.ui.theme.Spacing

/**
 * Icon button that opens a dropdown listing all available pane types
 * grouped by [PaneCategory].
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun AddPaneMenu(onPaneSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add pane",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        val groupedPanes = PaneRegistry.availableTypes
            .mapNotNull { type -> PaneRegistry.metadata(type)?.let { type to it } }
            .groupBy { (_, meta) -> meta.category }

        PaneCategory.entries.forEach { category ->
            val panes = groupedPanes[category] ?: return@forEach
            Column(modifier = Modifier.padding(horizontal = Spacing.Space8)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = Spacing.Space4)
                )
                panes.forEach { (type, meta) ->
                    DropdownMenuItem(
                        text = { Text(meta.displayName) },
                        onClick = {
                            expanded = false
                            onPaneSelected(type)
                        }
                    )
                }
            }
        }
    }
}
