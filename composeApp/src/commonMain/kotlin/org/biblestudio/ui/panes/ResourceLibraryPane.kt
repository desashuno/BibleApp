package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleStatus
import org.biblestudio.core.data_manager.model.DataModuleType
import org.biblestudio.features.resource_library.component.ResourceLibraryState
import org.biblestudio.ui.components.Badge
import org.biblestudio.ui.components.DetailRow
import org.biblestudio.ui.components.EmptyStateMessage
import org.biblestudio.ui.components.ErrorMessage
import org.biblestudio.ui.components.LoadingIndicator
import org.biblestudio.ui.components.StatusBadge
import org.biblestudio.ui.theme.Spacing

/**
 * Resource Library pane: browse, install, and manage data modules.
 */
@OptIn(ExperimentalLayoutApi::class)
@Suppress("ktlint:standard:function-naming", "LongMethod", "LongParameterList")
@Composable
fun ResourceLibraryPane(
    stateFlow: StateFlow<ResourceLibraryState>,
    onModuleSelected: (String) -> Unit,
    onInstallModule: (String) -> Unit,
    onRemoveModule: (String) -> Unit,
    onCancelDownload: (String) -> Unit,
    onFilterTypeChanged: (DataModuleType?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleModuleActive: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Row(modifier = modifier.fillMaxSize()) {
        // ── Main content: module grid ──
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(Spacing.Space16)
        ) {
            Text(
                text = "Resource Library",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = Spacing.Space8)
            )

            // Search field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search modules\u2026") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.Space8))

            // Filter chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Space8)
            ) {
                FilterChip(
                    selected = state.filterType == null,
                    onClick = { onFilterTypeChanged(null) },
                    label = { Text("All") }
                )
                DataModuleType.entries.forEach { type ->
                    FilterChip(
                        selected = state.filterType == type,
                        onClick = { onFilterTypeChanged(type) },
                        label = { Text(type.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Space12))

            if (state.isLoading && state.filteredModules.isEmpty()) {
                LoadingIndicator(fullScreen = true)
            } else if (state.error != null && state.filteredModules.isEmpty()) {
                ErrorMessage(message = state.error ?: "Error")
            } else if (state.filteredModules.isEmpty()) {
                EmptyStateMessage(message = "No modules found")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = Spacing.Space48 * 5),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Space12),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Space12),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.filteredModules, key = { it.moduleId }) { module ->
                        ModuleCard(
                            module = module,
                            downloadProgress = state.activeDownloads[module.moduleId],
                            isSelected = state.selectedModule?.moduleId == module.moduleId,
                            onClick = { onModuleSelected(module.moduleId) },
                            onInstall = { onInstallModule(module.moduleId) },
                            onRemove = { onRemoveModule(module.moduleId) },
                            onCancel = { onCancelDownload(module.moduleId) },
                            onToggleActive = { onToggleModuleActive(module.moduleId) }
                        )
                    }
                }
            }
        }

        // ── Detail panel ──
        if (state.selectedModule != null) {
            ModuleDetailSidebar(
                module = state.selectedModule!!,
                downloadProgress = state.activeDownloads[state.selectedModule!!.moduleId],
                onInstall = { onInstallModule(state.selectedModule!!.moduleId) },
                onRemove = { onRemoveModule(state.selectedModule!!.moduleId) },
                onCancel = { onCancelDownload(state.selectedModule!!.moduleId) },
                onToggleActive = { onToggleModuleActive(state.selectedModule!!.moduleId) },
                modifier = Modifier.width(Spacing.Space48 * 7)
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming", "LongMethod", "LongParameterList", "UnusedParameter")
@Composable
private fun ModuleCard(
    module: DataModuleDescriptor,
    downloadProgress: Float?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onInstall: () -> Unit,
    onRemove: () -> Unit,
    onCancel: () -> Unit,
    onToggleActive: () -> Unit
) {
    val cardAlpha = if (module.isActive || module.status != DataModuleStatus.Installed) 1f else 0.6f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(Spacing.Space12)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(Spacing.Space8))
                if (module.status == DataModuleStatus.Installed) {
                    ActiveBadge(module.isActive)
                } else {
                    ModuleStatusBadge(module.status)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Space4))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TypeBadge(module.type)
                Spacer(Modifier.width(Spacing.Space8))
                Text(
                    text = module.language,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (module.sizeBytes > 0) {
                    Spacer(Modifier.width(Spacing.Space8))
                    Text(
                        text = formatBytes(module.sizeBytes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress bar for active downloads
            if (downloadProgress != null) {
                Spacer(modifier = Modifier.height(Spacing.Space8))
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.Space8))

            // Action: toggle for installed modules, install/cancel for others
            when (module.status) {
                DataModuleStatus.Installed ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (module.isActive) "Active" else "Inactive",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = module.isActive,
                            onCheckedChange = { onToggleActive() }
                        )
                    }
                DataModuleStatus.Available, DataModuleStatus.Error ->
                    Button(onClick = onInstall, modifier = Modifier.fillMaxWidth()) {
                        Text("Install")
                    }
                DataModuleStatus.Downloading, DataModuleStatus.Installing ->
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel")
                    }
                else -> {}
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming", "LongParameterList", "LongMethod", "UnusedParameter")
@Composable
private fun ModuleDetailSidebar(
    module: DataModuleDescriptor,
    downloadProgress: Float?,
    onInstall: () -> Unit,
    onRemove: () -> Unit,
    onCancel: () -> Unit,
    onToggleActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.Space16)
    ) {
        Text(
            text = module.name,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(Spacing.Space8))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.Space16)) {
                DetailRow("Type", module.type.displayName)
                DetailRow("Language", module.language)
                DetailRow("Version", module.version)
                if (module.status == DataModuleStatus.Installed) {
                    DetailRow("Status", if (module.isActive) "Active" else "Inactive")
                } else {
                    DetailRow("Status", module.status.displayName)
                }
                if (module.sizeBytes > 0) {
                    DetailRow("Size", formatBytes(module.sizeBytes))
                }
                module.installedAt?.let { DetailRow("Installed", it) }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.Space8))

        if (module.description.isNotBlank()) {
            Text(
                text = module.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.Space16))
        }

        if (downloadProgress != null) {
            LinearProgressIndicator(
                progress = { downloadProgress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.Space8))
        }

        when (module.status) {
            DataModuleStatus.Installed -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (module.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = module.isActive,
                        onCheckedChange = { onToggleActive() }
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.Space4))
                Text(
                    text = if (module.isActive) {
                        "This resource is visible to the rest of the app."
                    } else {
                        "This resource is hidden. Toggle to make it available."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DataModuleStatus.Available, DataModuleStatus.Error ->
                Button(onClick = onInstall) { Text("Install") }
            DataModuleStatus.Downloading, DataModuleStatus.Installing ->
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            else -> {}
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TypeBadge(type: DataModuleType) {
    Badge(text = type.displayName, color = MaterialTheme.colorScheme.tertiary)
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ActiveBadge(isActive: Boolean) {
    val color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    StatusBadge(text = if (isActive) "Active" else "Inactive", statusColor = color)
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ModuleStatusBadge(status: DataModuleStatus) {
    val color = when (status) {
        DataModuleStatus.Installed -> MaterialTheme.colorScheme.primary
        DataModuleStatus.Error -> MaterialTheme.colorScheme.error
        DataModuleStatus.Downloading, DataModuleStatus.Installing -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    StatusBadge(text = status.displayName, statusColor = color)
}

// DetailRow is imported from org.biblestudio.ui.components

@Suppress("MagicNumber")
private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${"%.1f".format(kb)} KB"
    val mb = kb / 1024.0
    return "${"%.1f".format(mb)} MB"
}
