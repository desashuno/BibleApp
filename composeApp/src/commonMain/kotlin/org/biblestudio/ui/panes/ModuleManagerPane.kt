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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.module_system.component.ModuleManagerState
import org.biblestudio.features.module_system.domain.entities.InstalledModule
import org.biblestudio.ui.theme.Spacing

/**
 * Module Manager pane: list of installed modules with detail view and install/remove actions.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun ModuleManagerPane(
    stateFlow: StateFlow<ModuleManagerState>,
    onModuleSelected: (InstalledModule) -> Unit,
    onRemoveModule: (String) -> Unit,
    onImportModule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.Space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Module Manager",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onImportModule) {
                Text("Import Module")
            }
        }

        // Install progress
        if (state.installProgress != null) {
            LinearProgressIndicator(
                progress = { state.installProgress!! },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Space16)
            )
            Spacer(modifier = Modifier.height(Spacing.Space8))
        }

        HorizontalDivider()

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(Spacing.Space24)
            )
        } else if (state.error != null) {
            Text(
                text = state.error ?: "Error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(Spacing.Space16)
            )
        } else if (state.installedModules.isEmpty()) {
            Text(
                text = "No modules installed. Import a module to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.Space16)
            )
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // Module list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    items(state.installedModules, key = { it.id }) { module ->
                        ModuleListItem(
                            module = module,
                            isSelected = state.selectedModule?.id == module.id,
                            onClick = { onModuleSelected(module) }
                        )
                    }
                }

                // Detail panel
                if (state.selectedModule != null) {
                    ModuleDetailPanel(
                        module = state.selectedModule!!,
                        onRemove = { onRemoveModule(state.selectedModule!!.uuid) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ModuleListItem(module: InstalledModule, isSelected: Boolean, onClick: () -> Unit) {
    val background = if (isSelected) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }
    Surface(color = background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.Space16, vertical = Spacing.Space12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${module.abbreviation} • ${module.language}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(Spacing.Space4)
            ) {
                Text(
                    text = module.type,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(
                        horizontal = Spacing.Space8,
                        vertical = Spacing.Space2
                    )
                )
            }
        }
    }
    HorizontalDivider()
}

@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun ModuleDetailPanel(module: InstalledModule, onRemove: () -> Unit, modifier: Modifier = Modifier) {
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
                DetailRow("Abbreviation", module.abbreviation)
                DetailRow("Language", module.language)
                DetailRow("Type", module.type)
                DetailRow("Version", module.version)
                DetailRow("Size", formatBytes(module.sizeBytes))
                DetailRow("Source", module.sourceType)
                DetailRow("Installed", module.installedAt)
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

        OutlinedButton(
            onClick = onRemove,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Remove Module")
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Space4)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(Spacing.Space48 * 2)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Suppress("MagicNumber")
private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${"%.1f".format(kb)} KB"
    val mb = kb / 1024.0
    return "${"%.1f".format(mb)} MB"
}
