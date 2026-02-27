package org.biblestudio.features.import_export.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.import_export.domain.entities.BackupInfo
import org.biblestudio.features.import_export.domain.entities.DataType
import org.biblestudio.features.import_export.domain.entities.ExportFormat

/**
 * Observable state for the Import/Export screen.
 */
data class ImportExportState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastExport: String? = null,
    val lastImportCount: Int? = null,
    val backupHistory: List<BackupInfo> = emptyList()
)

/**
 * Business-logic boundary for the Import/Export screen.
 */
interface ImportExportComponent {

    /** The current state observable. */
    val state: StateFlow<ImportExportState>

    /** Exports data of a given type and format. */
    fun exportData(dataType: DataType, format: ExportFormat)

    /** Imports data from a string. */
    fun importData(content: String, dataType: DataType, format: ExportFormat)

    /** Creates a full backup bundle. */
    fun createBackup()

    /** Restores from a backup bundle. */
    fun restoreBackup(content: String)

    /** Loads backup history. */
    fun loadBackupHistory()
}
