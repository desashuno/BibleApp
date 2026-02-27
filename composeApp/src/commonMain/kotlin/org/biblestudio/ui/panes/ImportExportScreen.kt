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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.biblestudio.features.import_export.domain.entities.BackupInfo
import org.biblestudio.features.import_export.domain.entities.DataType
import org.biblestudio.features.import_export.domain.entities.ExportFormat
import org.biblestudio.ui.theme.Spacing

/**
 * Import/Export screen: export data, create backups, restore from backup.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun ImportExportScreen(
    backups: List<BackupInfo>,
    isExporting: Boolean,
    onExport: (DataType, ExportFormat) -> Unit,
    onCreateBackup: () -> Unit,
    onRestore: (BackupInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.JSON) }
    var selectedDataType by remember { mutableStateOf(DataType.ALL) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Import & Export",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(Spacing.Space16)
        )
        HorizontalDivider()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Export section
            item { SectionTitle("Export Data") }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Space16, vertical = Spacing.Space8)
                ) {
                    Column(modifier = Modifier.padding(Spacing.Space16)) {
                        Text(
                            text = "Data Type",
                            style = MaterialTheme.typography.labelLarge
                        )
                        DataType.entries.forEach { type ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDataType = type }
                                    .padding(vertical = Spacing.Space2)
                            ) {
                                RadioButton(
                                    selected = selectedDataType == type,
                                    onClick = { selectedDataType = type }
                                )
                                Text(
                                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.Space12))

                        Text(
                            text = "Format",
                            style = MaterialTheme.typography.labelLarge
                        )
                        ExportFormat.entries.forEach { format ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFormat = format }
                                    .padding(vertical = Spacing.Space2)
                            ) {
                                RadioButton(
                                    selected = selectedFormat == format,
                                    onClick = { selectedFormat = format }
                                )
                                Text(
                                    text = format.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.Space16))

                        Button(
                            onClick = { onExport(selectedDataType, selectedFormat) },
                            enabled = !isExporting,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.width(Spacing.Space24).height(Spacing.Space24)
                                )
                            } else {
                                Text("Export")
                            }
                        }
                    }
                }
            }

            // Backup section
            item { SectionTitle("Backup & Restore") }

            item {
                Button(
                    onClick = onCreateBackup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Space16)
                ) {
                    Text("Create Full Backup")
                }
            }

            if (backups.isNotEmpty()) {
                item {
                    Text(
                        text = "Backup History",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(
                            start = Spacing.Space16,
                            top = Spacing.Space16,
                            bottom = Spacing.Space8
                        )
                    )
                }
                items(backups, key = { it.id }) { backup ->
                    BackupRow(backup = backup, onRestore = { onRestore(backup) })
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = Spacing.Space16,
            end = Spacing.Space16,
            top = Spacing.Space24,
            bottom = Spacing.Space8
        )
    )
}

@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun BackupRow(backup: BackupInfo, onRestore: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Space16, vertical = Spacing.Space8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = backup.filename,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${backup.backupType} • ${backup.itemCount} items • ${backup.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(onClick = onRestore) {
            Text("Restore")
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.Space16))
}
