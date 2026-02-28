package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.resource_library.component.ResourceLibraryState
import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.ui.theme.Spacing
import org.biblestudio.ui.theme.scaledBodyStyle

/**
 * Resource Library pane: sidebar with installed resources + content area.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "UnusedParameter")
@Composable
fun ResourceLibraryPane(
    stateFlow: StateFlow<ResourceLibraryState>,
    onResourceSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onEntryVerseSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Row(modifier = modifier.fillMaxSize()) {
        // ── Sidebar: resource list ──
        Column(
            modifier = Modifier
                .width(Spacing.Space48 * 5)
                .padding(Spacing.Space8)
        ) {
            Text(
                text = "Resources",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = Spacing.Space8)
            )
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.Space8))

            if (state.isLoading && state.resources.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(Spacing.Space16)
                )
            } else {
                LazyColumn {
                    items(state.resources, key = { it.uuid }) { resource ->
                        ResourceRow(
                            resource = resource,
                            isActive = resource.uuid == state.activeResource?.uuid,
                            onClick = { onResourceSelected(resource.uuid) }
                        )
                    }
                }
            }
        }

        // ── Content area ──
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(Spacing.Space16)
        ) {
            val entry = state.entry
            val activeResource = state.activeResource

            if (activeResource == null) {
                Text(
                    text = "Select a resource to view entries",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(Spacing.Space24)
                )
            } else if (entry != null) {
                Text(
                    text = activeResource.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = Spacing.Space8)
                )
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.Space8))
                LazyColumn {
                    item {
                        Text(
                            text = entry.content,
                            style = scaledBodyStyle()
                        )
                    }
                }
            } else {
                Text(
                    text = "No entry for this verse",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(Spacing.Space24)
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ResourceRow(resource: Resource, isActive: Boolean, onClick: () -> Unit) {
    val bg = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    Surface(color = bg) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = Spacing.Space4)
        ) {
            Column(modifier = Modifier.padding(Spacing.Space8)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${resource.author} · ${resource.type}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
