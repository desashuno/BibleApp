package org.biblestudio.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.biblestudio.core.pane_registry.PaneCategory
import org.biblestudio.core.pane_registry.PaneRegistry
import org.biblestudio.ui.theme.PaneDisplayInfo
import org.biblestudio.ui.theme.PaneStyling
import org.biblestudio.ui.theme.Spacing

private val CARD_WIDTH = 170.dp
private val CARD_ICON_SIZE = 28.dp

/**
 * Filter tabs for the module picker.
 */
private enum class PickerFilter(val label: String) {
    All("All"),
    Favorites("Favorites"),
    Read("Read"),
    Write("Write")
}

/** Categories that belong to the "Read" filter. */
private val READ_CATEGORIES = setOf(
    PaneCategory.Text, PaneCategory.Study, PaneCategory.Resource
)

/** Categories that belong to the "Write" filter. */
private val WRITE_CATEGORIES = setOf(
    PaneCategory.Writing, PaneCategory.Tool, PaneCategory.Media
)

/**
 * Centered dialog for browsing, searching, and opening all available pane types.
 *
 * Module cards are grouped by [PaneCategory]. Each card shows the pane icon,
 * display name, and toggle buttons for pinning and favoriting.
 * Clicking the card body opens the pane and closes the picker.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun ModulePicker(
    pinnedPanes: Set<String>,
    favoritePanes: Set<String>,
    onPaneSelected: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .widthIn(max = 800.dp)
                .heightIn(max = 600.dp)
        ) {
            ModulePickerContent(
                pinnedPanes = pinnedPanes,
                favoritePanes = favoritePanes,
                onPaneSelected = onPaneSelected,
                onTogglePinned = onTogglePinned,
                onToggleFavorite = onToggleFavorite,
                onDismiss = onDismiss
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
private fun ModulePickerContent(
    pinnedPanes: Set<String>,
    favoritePanes: Set<String>,
    onPaneSelected: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(PickerFilter.All) }

    // Build grouped pane list from registry
    val allPanes = remember(Unit) {
        PaneRegistry.availableTypes.map { type ->
            type to PaneStyling.paneInfo(type)
        }
    }

    // Apply filters
    val filteredPanes = remember(searchQuery, activeFilter, favoritePanes) {
        allPanes
            .filter { (_, info) ->
                // Search filter: matches display name or description
                if (searchQuery.isNotBlank()) {
                    info.displayName.contains(searchQuery, ignoreCase = true) ||
                        info.description.contains(searchQuery, ignoreCase = true)
                } else {
                    true
                }
            }
            .filter { (type, info) ->
                when (activeFilter) {
                    PickerFilter.All -> true
                    PickerFilter.Favorites -> type in favoritePanes
                    PickerFilter.Read -> info.category in READ_CATEGORIES
                    PickerFilter.Write -> info.category in WRITE_CATEGORIES
                }
            }
    }

    // Group by category
    val groupedPanes = remember(filteredPanes) {
        filteredPanes.groupBy { (_, info) -> info.category }
    }

    Column(modifier = Modifier.fillMaxSize().padding(Spacing.Space16)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Modules",
                style = MaterialTheme.typography.headlineSmall
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        Spacer(Modifier.height(Spacing.Space8))

        // Filter tabs
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Space8)) {
            PickerFilter.entries.forEach { filter ->
                FilterChip(
                    selected = activeFilter == filter,
                    onClick = { activeFilter = filter },
                    label = { Text(filter.label) }
                )
            }
        }

        Spacer(Modifier.height(Spacing.Space8))

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search modules...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(Spacing.Space12))

        // Scrollable grid of module cards grouped by category
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            val categoryOrder = PaneCategory.entries
            categoryOrder.forEach { category ->
                val panes = groupedPanes[category] ?: return@forEach

                // Category header
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = PaneStyling.categoryColor(category),
                    modifier = Modifier.padding(vertical = Spacing.Space8)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Space8),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Space8)
                ) {
                    panes.forEach { (type, info) ->
                        ModuleCard(
                            paneType = type,
                            info = info,
                            isPinned = type in pinnedPanes,
                            isFavorite = type in favoritePanes,
                            onOpen = { onPaneSelected(type) },
                            onTogglePin = { onTogglePinned(type) },
                            onToggleFav = { onToggleFavorite(type) }
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.Space8))
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ModuleCard(
    paneType: String,
    info: PaneDisplayInfo,
    isPinned: Boolean,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFav: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .width(CARD_WIDTH)
            .clickable(onClick = onOpen)
    ) {
        Column(modifier = Modifier.padding(Spacing.Space12)) {
            // Icon + action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = info.icon,
                    contentDescription = null,
                    tint = info.accentColor,
                    modifier = Modifier.size(CARD_ICON_SIZE)
                )
                Row {
                    IconButton(
                        onClick = onToggleFav,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (isPinned) "Unpin from sidebar" else "Pin to sidebar",
                            tint = if (isPinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.Space8))

            // Pane name
            Text(
                text = info.displayName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
