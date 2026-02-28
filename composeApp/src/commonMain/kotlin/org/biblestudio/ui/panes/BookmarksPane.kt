package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.util.VerseRefFormatter
import org.biblestudio.features.bookmarks_history.component.BookmarksState
import org.biblestudio.features.bookmarks_history.component.BookmarksViewMode
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.entities.BookmarkFolder
import org.biblestudio.features.bookmarks_history.domain.entities.HistoryEntry
import org.biblestudio.ui.theme.Spacing

/**
 * Bookmarks & History pane: tabbed layout switching between bookmark folders
 * and chronological navigation history.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "LongParameterList")
@Composable
fun BookmarksPane(
    stateFlow: StateFlow<BookmarksState>,
    onBookmarkTapped: (Bookmark) -> Unit,
    onFolderSelected: (String?) -> Unit,
    onCreateFolder: (String, String?) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onHistoryTapped: (HistoryEntry) -> Unit,
    onViewModeChanged: (BookmarksViewMode) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    val tabIndex = if (state.viewMode == BookmarksViewMode.Bookmarks) 0 else 1
    var showNewFolder by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (state.viewMode == BookmarksViewMode.Bookmarks) {
                FloatingActionButton(
                    onClick = { showNewFolder = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Folder")
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = tabIndex) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { onViewModeChanged(BookmarksViewMode.Bookmarks) },
                    text = { Text("Bookmarks") }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { onViewModeChanged(BookmarksViewMode.History) },
                    text = { Text("History") }
                )
            }

            when (state.viewMode) {
                BookmarksViewMode.Bookmarks -> BookmarksTab(
                    bookmarks = state.bookmarks,
                    folders = state.folders,
                    activeFolder = state.activeFolder,
                    showNewFolder = showNewFolder,
                    onDismissNewFolder = { showNewFolder = false },
                    onBookmarkTapped = onBookmarkTapped,
                    onFolderSelected = onFolderSelected,
                    onCreateFolder = onCreateFolder,
                    onDeleteBookmark = onDeleteBookmark,
                    onDeleteFolder = onDeleteFolder
                )
                BookmarksViewMode.History -> HistoryTab(
                    history = state.history,
                    onHistoryTapped = onHistoryTapped,
                    onClearHistory = onClearHistory
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming", "LongParameterList")
@Composable
private fun BookmarksTab(
    bookmarks: List<Bookmark>,
    folders: List<BookmarkFolder>,
    activeFolder: BookmarkFolder?,
    showNewFolder: Boolean,
    onDismissNewFolder: () -> Unit,
    onBookmarkTapped: (Bookmark) -> Unit,
    onFolderSelected: (String?) -> Unit,
    onCreateFolder: (String, String?) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onDeleteFolder: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(Spacing.Space16)) {
        // ── Back to parent / breadcrumb ──
        if (activeFolder != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onFolderSelected(activeFolder.parentId) }
                    .padding(bottom = Spacing.Space8)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(Spacing.Space4))
                Text(
                    text = activeFolder.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // ── Inline new-folder form ──
        if (showNewFolder) {
            NewFolderRow(
                parentId = activeFolder?.uuid,
                onCreateFolder = onCreateFolder,
                onDismiss = onDismissNewFolder
            )
        }

        LazyColumn {
            // ── Folders ──
            items(folders, key = { it.uuid }) { folder ->
                FolderRow(
                    folder = folder,
                    onClick = { onFolderSelected(folder.uuid) },
                    onDelete = { onDeleteFolder(folder.uuid) }
                )
            }
            // ── Bookmarks ──
            items(bookmarks, key = { it.uuid }) { bookmark ->
                BookmarkRow(
                    bookmark = bookmark,
                    onClick = { onBookmarkTapped(bookmark) },
                    onDelete = { onDeleteBookmark(bookmark.uuid) }
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun NewFolderRow(parentId: String?, onCreateFolder: (String, String?) -> Unit, onDismiss: () -> Unit) {
    var folderName by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.Space8)
    ) {
        androidx.compose.material3.OutlinedTextField(
            value = folderName,
            onValueChange = { folderName = it },
            label = { Text("Folder name") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(Spacing.Space8))
        TextButton(
            onClick = {
                if (folderName.isNotBlank()) {
                    onCreateFolder(folderName.trim(), parentId)
                    onDismiss()
                }
            }
        ) { Text("Create") }
        TextButton(onClick = onDismiss) { Text("Cancel") }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun FolderRow(folder: BookmarkFolder, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Space4)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.Space12)
        ) {
            Text(
                text = "\uD83D\uDCC1 ${folder.name}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete folder",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun BookmarkRow(bookmark: Bookmark, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Space4)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.Space12)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.label,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = VerseRefFormatter.format(bookmark.globalVerseId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete bookmark",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun HistoryTab(
    history: List<HistoryEntry>,
    onHistoryTapped: (HistoryEntry) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(Spacing.Space16)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Navigation History",
                style = MaterialTheme.typography.titleSmall
            )
            if (history.isNotEmpty()) {
                TextButton(onClick = onClearHistory) {
                    Text("Clear All")
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.Space8))

        if (history.isEmpty()) {
            Text(
                text = "No history entries yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.Space16)
            )
        } else {
            LazyColumn {
                items(history, key = { it.id }) { entry ->
                    HistoryRow(entry = entry, onClick = { onHistoryTapped(entry) })
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun HistoryRow(entry: HistoryEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Space4)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.Space12)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = VerseRefFormatter.format(entry.globalVerseId),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = entry.visitedAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
