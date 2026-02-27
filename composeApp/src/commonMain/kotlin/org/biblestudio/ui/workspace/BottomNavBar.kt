package org.biblestudio.ui.workspace

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val paneType: String
)

private val NAV_ITEMS = listOf(
    NavItem("Bible", Icons.Default.Home, "bible-reader"),
    NavItem("Search", Icons.Default.Search, "search"),
    NavItem("Notes", Icons.Default.Edit, "note-editor"),
    NavItem("More", Icons.Default.MoreVert, "resource-library")
)

/**
 * Bottom navigation bar for Compact and Medium screen sizes.
 *
 * Provides quick access to the four most-used pane types.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun BottomNavBar(onPaneSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableStateOf(0) }

    NavigationBar(modifier = modifier) {
        NAV_ITEMS.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = {
                    selectedIndex = index
                    onPaneSelected(item.paneType)
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
}
