package org.biblestudio.core.data_manager.model

/**
 * Describes a data module — maps to a row in the `data_modules` table.
 */
data class DataModuleDescriptor(
    val id: Long = 0,
    val moduleId: String,
    val name: String,
    val description: String = "",
    val type: DataModuleType,
    val version: String = "1.0",
    val language: String = "en",
    val sourceUrl: String = "",
    val sizeBytes: Long = 0,
    val status: DataModuleStatus = DataModuleStatus.Available,
    val progress: Float = 0f,
    val installedAt: String? = null,
    val updatedAt: String? = null,
    val checksum: String = "",
    val metadata: String = "{}",
    val isActive: Boolean = false
)
