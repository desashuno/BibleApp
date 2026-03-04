package org.biblestudio.ui.workspace

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.biblestudio.core.pane_registry.PaneCategory
import org.biblestudio.core.pane_registry.PaneRegistry
import org.biblestudio.core.pane_registry.PaneType
import org.biblestudio.ui.theme.PaneStyling
import org.biblestudio.ui.theme.Spacing

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val paneType: String?
)

private val NAV_ITEMS = listOf(
    NavItem("Bible", Icons.Default.Home, PaneType.BIBLE_READER),
    NavItem("Search", Icons.Default.Search, PaneType.SEARCH),
    NavItem("Notes", Icons.Default.Edit, PaneType.NOTE_EDITOR),
    NavItem("More", Icons.Default.MoreVert, null)
)

/**
 * Bottom navigation bar for Compact and Medium screen sizes.
 *
 * Provides quick access to three main pane types plus a "More" item
 * that opens a bottom sheet listing all available pane types grouped
 * by [PaneCategory].
 *
 * @param activePaneType the currently focused pane type, used to
 *   highlight the active navigation item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun BottomNavBar(onPaneSelected: (String) -> Unit, activePaneType: String? = null, modifier: Modifier = Modifier) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Determine which nav item is selected based on active pane
    val selectedIndex = NAV_ITEMS.indexOfFirst { it.paneType == activePaneType }
        .coerceAtLeast(0)

    NavigationBar(modifier = modifier) {
        NAV_ITEMS.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = {
                    if (item.paneType != null) {
                        onPaneSelected(item.paneType)
                    } else {
                        showSheet = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Text(
                text = "Open Module",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = Spacing.Space16, vertical = Spacing.Space8)
            )
            HorizontalDivider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.Space24)
            ) {
                PaneCategory.entries.forEach { category ->
                    val panes = PaneRegistry.availableTypes
                        .mapNotNull { type ->
                            val meta = try {
                                PaneRegistry.metadata(type)
                            } catch (_: IllegalArgumentException) {
                                null
                            }
                            if (meta != null && meta.category == category) type to meta else null
                        }

                    if (panes.isNotEmpty()) {
                        item {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = PaneStyling.categoryColor(category),
                                modifier = Modifier.padding(
                                    start = Spacing.Space16,
                                    end = Spacing.Space16,
                                    top = Spacing.Space16,
                                    bottom = Spacing.Space4
                                )
                            )
                        }

                        panes.forEach { (type, meta) ->
                            item {
                                val info = remember(type) { PaneStyling.paneInfo(type) }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showSheet = false
                                            onPaneSelected(type)
                                        }
                                        .padding(
                                            horizontal = Spacing.Space16,
                                            vertical = Spacing.Space12
                                        )
                                ) {
                                    Icon(
                                        imageVector = info.icon,
                                        contentDescription = null,
                                        tint = info.accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(Spacing.Space12))
                                    Column {
                                        Text(
                                            text = meta.displayName,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = meta.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(Spacing.Space16)) }
            }
        }
    }
}
