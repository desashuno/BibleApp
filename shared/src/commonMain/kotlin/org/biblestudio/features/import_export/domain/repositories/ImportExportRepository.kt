package org.biblestudio.features.import_export.domain.repositories

import org.biblestudio.features.import_export.domain.entities.BackupInfo
import org.biblestudio.features.import_export.domain.entities.DataType
import org.biblestudio.features.import_export.domain.entities.ExportFormat

/**
 * Handles data export and import operations for user data.
 */
interface ImportExportRepository {

    /**
     * Exports data of the specified [dataType] in the given [format].
     *
     * @return The exported data as a string.
     */
    suspend fun exportData(dataType: DataType, format: ExportFormat): Result<String>

    /**
     * Imports data from the given [content] string.
     *
     * @param content The raw import data.
     * @param dataType The type of data being imported.
     * @param format The format of the import data.
     * @return Count of imported items.
     */
    suspend fun importData(content: String, dataType: DataType, format: ExportFormat): Result<Int>

    /**
     * Creates a full backup bundle containing all user data.
     *
     * @return Serialized backup content.
     */
    suspend fun createBackup(): Result<String>

    /**
     * Restores all user data from a backup bundle.
     *
     * @param backupContent The serialized backup content.
     */
    suspend fun restoreBackup(backupContent: String): Result<Int>

    /** Returns backup history. */
    suspend fun getBackupHistory(): Result<List<BackupInfo>>
}
