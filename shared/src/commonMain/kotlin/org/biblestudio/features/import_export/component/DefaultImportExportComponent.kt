package org.biblestudio.features.import_export.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.features.import_export.domain.entities.DataType
import org.biblestudio.features.import_export.domain.entities.ExportFormat
import org.biblestudio.features.import_export.domain.repositories.ImportExportRepository

/**
 * Default [ImportExportComponent] backed by [ImportExportRepository].
 */
class DefaultImportExportComponent(
    componentContext: ComponentContext,
    private val repository: ImportExportRepository
) : ImportExportComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(ImportExportState())
    override val state: StateFlow<ImportExportState> = _state.asStateFlow()

    init {
        loadBackupHistory()
    }

    override fun exportData(dataType: DataType, format: ExportFormat) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            repository.exportData(dataType, format)
                .onSuccess { data ->
                    _state.update { it.copy(isLoading = false, lastExport = data) }
                    Napier.d("Exported ${dataType.name} as ${format.name}")
                }
                .onFailure { e ->
                    Napier.e("Export failed", e)
                    _state.update { it.copy(isLoading = false, error = "Export failed: ${e.message}") }
                }
        }
    }

    override fun importData(content: String, dataType: DataType, format: ExportFormat) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            repository.importData(content, dataType, format)
                .onSuccess { count ->
                    _state.update { it.copy(isLoading = false, lastImportCount = count) }
                    Napier.i("Imported $count items of ${dataType.name}")
                }
                .onFailure { e ->
                    Napier.e("Import failed", e)
                    _state.update { it.copy(isLoading = false, error = "Import failed: ${e.message}") }
                }
        }
    }

    override fun createBackup() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            repository.createBackup()
                .onSuccess { data ->
                    _state.update { it.copy(isLoading = false, lastExport = data) }
                    Napier.i("Backup created")
                    loadBackupHistory()
                }
                .onFailure { e ->
                    Napier.e("Backup failed", e)
                    _state.update { it.copy(isLoading = false, error = "Backup failed: ${e.message}") }
                }
        }
    }

    override fun restoreBackup(content: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            repository.restoreBackup(content)
                .onSuccess { count ->
                    _state.update { it.copy(isLoading = false, lastImportCount = count) }
                    Napier.i("Restored $count items from backup")
                }
                .onFailure { e ->
                    Napier.e("Restore failed", e)
                    _state.update { it.copy(isLoading = false, error = "Restore failed: ${e.message}") }
                }
        }
    }

    override fun loadBackupHistory() {
        scope.launch {
            repository.getBackupHistory()
                .onSuccess { history ->
                    _state.update { it.copy(backupHistory = history) }
                }
                .onFailure { e ->
                    Napier.e("Failed to load backup history", e)
                }
        }
    }
}
