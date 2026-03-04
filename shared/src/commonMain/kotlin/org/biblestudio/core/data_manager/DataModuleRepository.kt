package org.biblestudio.core.data_manager

import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleStatus
import org.biblestudio.core.data_manager.model.DataModuleType

/**
 * Persistence layer for [DataModuleDescriptor] records.
 */
interface DataModuleRepository {
    suspend fun getAllModules(): Result<List<DataModuleDescriptor>>
    suspend fun getModulesByType(type: DataModuleType): Result<List<DataModuleDescriptor>>
    suspend fun getModulesByStatus(status: DataModuleStatus): Result<List<DataModuleDescriptor>>
    suspend fun getModuleById(moduleId: String): Result<DataModuleDescriptor?>
    suspend fun insertModule(descriptor: DataModuleDescriptor): Result<Unit>
    suspend fun updateStatus(moduleId: String, status: DataModuleStatus): Result<Unit>
    suspend fun updateProgress(moduleId: String, progress: Float): Result<Unit>
    suspend fun markInstalled(moduleId: String): Result<Unit>
    suspend fun markRemoved(moduleId: String): Result<Unit>
    suspend fun deleteModule(moduleId: String): Result<Unit>
    suspend fun getActiveModules(): Result<List<DataModuleDescriptor>>
    suspend fun getActiveModulesByType(type: DataModuleType): Result<List<DataModuleDescriptor>>
    suspend fun setModuleActive(moduleId: String, isActive: Boolean): Result<Unit>

    /**
     * Scans existing data tables (bibles, morphology, lexicon, etc.) and creates
     * corresponding entries in data_modules. Called when data_modules is empty
     * but the database already has data (e.g. legacy databases, seeded DBs).
     */
    suspend fun autoPopulateFromExistingData(): Result<Int>
}
