package org.biblestudio.features.import_export.domain.entities

/**
 * Supported export formats.
 */
enum class ExportFormat {
    JSON,
    CSV
}

/**
 * Supported data types for export/import.
 */
enum class DataType {
    NOTES,
    HIGHLIGHTS,
    BOOKMARKS,
    READING_PLANS,
    ALL
}

/**
 * A backup bundle descriptor.
 */
data class BackupInfo(
    val id: Long,
    val filename: String,
    val backupType: String,
    val createdAt: String,
    val sizeBytes: Long,
    val itemCount: Long
)
