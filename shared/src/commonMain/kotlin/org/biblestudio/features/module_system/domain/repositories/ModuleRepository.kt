package org.biblestudio.features.module_system.domain.repositories

import org.biblestudio.features.module_system.domain.entities.InstalledModule
import org.biblestudio.features.module_system.domain.entities.ModuleSource

/**
 * Manages installed Bible modules: listing, installing, and removing.
 */
interface ModuleRepository {

    /** Returns all active installed modules. */
    suspend fun getInstalledModules(): Result<List<InstalledModule>>

    /** Returns installed modules of a given type (e.g., "bible", "commentary"). */
    suspend fun getModulesByType(type: String): Result<List<InstalledModule>>

    /** Returns a single module by UUID. */
    suspend fun getModule(uuid: String): Result<InstalledModule?>

    /**
     * Installs a module from the given [source].
     *
     * @param source The module source descriptor.
     * @param progressCallback Optional callback for progress updates (0.0–1.0).
     * @return The installed module metadata.
     */
    suspend fun installModule(
        source: ModuleSource,
        progressCallback: ((Float) -> Unit)? = null
    ): Result<InstalledModule>

    /** Soft-deletes a module by UUID. */
    suspend fun removeModule(uuid: String): Result<Unit>

    /** Permanently removes a module and all associated data. */
    suspend fun purgeModule(uuid: String): Result<Unit>
}
