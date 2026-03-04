package org.biblestudio.core.data_manager

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.data_manager.model.DataManagerState
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleType

/**
 * Central orchestrator for all data lifecycle operations:
 * download, install, enable, and remove data modules.
 */
interface DataManager {
    /** Observable state of all modules and active operations. */
    val state: StateFlow<DataManagerState>

    /** Reloads the module list from the database. */
    suspend fun loadModules()

    /** Installs a module by its module ID. */
    suspend fun installModule(moduleId: String)

    /** Removes a module by its module ID. */
    suspend fun removeModule(moduleId: String)

    /** Cancels an active download by module ID. */
    fun cancelDownload(moduleId: String)

    /** Registers a handler for a specific module type. */
    fun registerHandler(handler: DataModuleHandler)

    /** Returns modules filtered by type from the current state. */
    fun getModulesByType(type: DataModuleType): List<DataModuleDescriptor>

    /** Sets a module's active state and reloads the module list. */
    suspend fun setModuleActive(moduleId: String, isActive: Boolean)

    /** Returns active modules filtered by type from the current state. */
    fun getActiveModulesByType(type: DataModuleType): List<DataModuleDescriptor>
}
