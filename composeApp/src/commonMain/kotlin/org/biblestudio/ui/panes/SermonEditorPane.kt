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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.sermon_editor.component.SermonEditorState
import org.biblestudio.ui.theme.Spacing

/**
 * Sermon Editor pane: sermon list on the left + metadata header, section list,
 * and word-count footer on the right.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "LongParameterList")
@Composable
fun SermonEditorPane(
    stateFlow: StateFlow<SermonEditorState>,
    onSermonSelected: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onScriptureRefChanged: (String) -> Unit,
    onNewSermon: () -> Unit,
    onDeleteSermon: (String) -> Unit,
    onSectionContentChanged: (Long, String) -> Unit,
    onAddSection: (String) -> Unit,
    onDeleteSection: (Long) -> Unit,
    onMoveSectionUp: (Long) -> Unit,
    onMoveSectionDown: (Long) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNewSermon) {
                Icon(Icons.Default.Add, contentDescription = "New sermon")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Sermon list sidebar ──
            SermonListSidebar(
                state = state,
                onSermonSelected = onSermonSelected,
                onDeleteSermon = onDeleteSermon,
                onSearchQueryChanged = onSearchQueryChanged,
                modifier = Modifier.width(Spacing.Space48 * 5)
            )

            // ── Editor area ──
            SermonEditorArea(
                state = state,
                onTitleChanged = onTitleChanged,
                onScriptureRefChanged = onScriptureRefChanged,
                onSectionContentChanged = onSectionContentChanged,
                onAddSection = onAddSection,
                onDeleteSection = onDeleteSection,
                onMoveSectionUp = onMoveSectionUp,
                onMoveSectionDown = onMoveSectionDown,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming", "LongParameterList")
@Composable
private fun SermonListSidebar(
    state: SermonEditorState,
    onSermonSelected: (String) -> Unit,
    onDeleteSermon: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(Spacing.Space8)) {
        Text(
            text = "Sermons",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = Spacing.Space8)
        )
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search sermons…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(Spacing.Space8))

        val displaySermons = if (state.searchQuery.length >= 2) state.searchResults else state.sermons
        LazyColumn {
            items(displaySermons, key = { it.uuid }) { sermon ->
                SermonListItem(
                    title = sermon.title.ifEmpty { "Untitled" },
                    scriptureRef = sermon.scriptureRef,
                    isActive = sermon.uuid == state.activeSermon?.uuid,
                    onClick = { onSermonSelected(sermon.uuid) },
                    onDelete = { onDeleteSermon(sermon.uuid) }
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming", "LongParameterList")
@Composable
private fun SermonEditorArea(
    state: SermonEditorState,
    onTitleChanged: (String) -> Unit,
    onScriptureRefChanged: (String) -> Unit,
    onSectionContentChanged: (Long, String) -> Unit,
    onAddSection: (String) -> Unit,
    onDeleteSection: (Long) -> Unit,
    onMoveSectionUp: (Long) -> Unit,
    onMoveSectionDown: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(Spacing.Space16)) {
        if (state.activeSermon == null) {
            Text(
                text = "Select or create a sermon",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.Space24)
            )
        } else {
            SermonMetadataHeader(
                state = state,
                onTitleChanged = onTitleChanged,
                onScriptureRefChanged = onScriptureRefChanged
            )

            // ── Section list ──
            SectionList(
                state = state,
                onSectionContentChanged = onSectionContentChanged,
                onAddSection = onAddSection,
                onDeleteSection = onDeleteSection,
                onMoveSectionUp = onMoveSectionUp,
                onMoveSectionDown = onMoveSectionDown,
                modifier = Modifier.weight(1f)
            )

            // ── Footer ──
            HorizontalDivider()
            WordCountFooter(state)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SermonMetadataHeader(
    state: SermonEditorState,
    onTitleChanged: (String) -> Unit,
    onScriptureRefChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = state.editTitle,
        onValueChange = onTitleChanged,
        placeholder = { Text("Sermon title") },
        textStyle = MaterialTheme.typography.titleLarge,
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(Spacing.Space8))
    OutlinedTextField(
        value = state.editScriptureRef,
        onValueChange = onScriptureRefChanged,
        placeholder = { Text("Scripture reference") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(Spacing.Space4))
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
}

@Suppress("ktlint:standard:function-naming", "LongParameterList")
@Composable
private fun SectionList(
    state: SermonEditorState,
    onSectionContentChanged: (Long, String) -> Unit,
    onAddSection: (String) -> Unit,
    onDeleteSection: (Long) -> Unit,
    onMoveSectionUp: (Long) -> Unit,
    onMoveSectionDown: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(state.sections, key = { it.id }) { section ->
            SectionEditor(
                section = section,
                onContentChanged = { onSectionContentChanged(section.id, it) },
                onDelete = { onDeleteSection(section.id) },
                onMoveUp = { onMoveSectionUp(section.id) },
                onMoveDown = { onMoveSectionDown(section.id) }
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Spacing.Space8),
                horizontalArrangement = Arrangement.Center
            ) {
                var expanded by remember { mutableStateOf(false) }
                val sectionTypes = listOf("Introduction", "Point", "Application", "Illustration", "Conclusion")
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add section")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sectionTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                expanded = false
                                onAddSection(type.lowercase())
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
private fun WordCountFooter(state: SermonEditorState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(Spacing.Space8),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${state.wordCount} words",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "~${state.estimatedMinutes} min",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Suppress("ktlint:standard:function-naming", "LongParameterList")
@Composable
private fun SectionEditor(
    section: org.biblestudio.features.sermon_editor.domain.entities.SermonSection,
    onContentChanged: (String) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Space4)
    ) {
        Column(modifier = Modifier.padding(Spacing.Space8)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = section.type.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onMoveUp) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                }
                IconButton(onClick = onMoveDown) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete section",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            OutlinedTextField(
                value = section.content,
                onValueChange = onContentChanged,
                placeholder = { Text("Section content…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SermonListItem(
    title: String,
    scriptureRef: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
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
                if (scriptureRef.isNotEmpty()) {
                    Text(
                        text = scriptureRef,
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
