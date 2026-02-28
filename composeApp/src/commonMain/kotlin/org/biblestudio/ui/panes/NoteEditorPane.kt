package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.note_editor.component.NoteEditorState
import org.biblestudio.ui.theme.Spacing
import org.biblestudio.ui.theme.scaledBodyStyle

/**
 * Note Editor pane: note list on the left + Markdown editor on the right.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "LongParameterList")
@Composable
fun NoteEditorPane(
    stateFlow: StateFlow<NoteEditorState>,
    onNoteSelected: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onNewNote: () -> Unit,
    onDeleteNote: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // ── Note list sidebar ──
            Column(
                modifier = Modifier
                    .width(Spacing.Space48 * 5)
                    .padding(Spacing.Space8)
            ) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = Spacing.Space8)
                )
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = { Text("Search notes…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.Space8))

                val displayNotes = if (state.searchQuery.length >= 2) {
                    state.searchResults
                } else {
                    state.notes
                }
                LazyColumn {
                    items(displayNotes, key = { it.uuid }) { note ->
                        NoteListItem(
                            title = note.title.ifEmpty { "Untitled" },
                            preview = note.content.take(PREVIEW_LENGTH),
                            isActive = note.uuid == state.activeNote?.uuid,
                            onClick = { onNoteSelected(note.uuid) },
                            onDelete = { onDeleteNote(note.uuid) }
                        )
                    }
                }
            }

            // ── Editor area ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(Spacing.Space16)
            ) {
                val activeNote = state.activeNote
                if (activeNote == null) {
                    Text(
                        text = "Select or create a note",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Spacing.Space24)
                    )
                } else {
                    // Title field
                    OutlinedTextField(
                        value = state.editTitle,
                        onValueChange = onTitleChanged,
                        placeholder = { Text("Note title") },
                        textStyle = MaterialTheme.typography.titleLarge,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Spacing.Space8))

                    // Save status
                    if (state.isSaving) {
                        Text(
                            text = "Saving…",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (state.isDirty) {
                        Text(
                            text = "Unsaved changes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.Space8))

                    // Content editor
                    OutlinedTextField(
                        value = state.editContent,
                        onValueChange = onContentChanged,
                        placeholder = { Text("Write your note…") },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = scaledBodyStyle()
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = onNewNote,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.Space16)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New note")
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun NoteListItem(title: String, preview: String, isActive: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.Space4)
    ) {
        Row(modifier = Modifier.padding(Spacing.Space8)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
                if (preview.isNotEmpty()) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    HorizontalDivider()
}

private const val PREVIEW_LENGTH = 80
