package org.biblestudio.core.data_manager

import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleType

/**
 * Handles install/remove/validate for a specific [DataModuleType].
 *
 * Each module type (Bible, Commentary, Dictionary, etc.) provides its own handler
 * that knows how to parse and persist the module's data.
 */
interface DataModuleHandler {
    /** The module type this handler supports. */
    val supportedType: DataModuleType

    /** Installs module data, reporting progress via [progressCallback]. */
    suspend fun install(descriptor: DataModuleDescriptor, progressCallback: (Float) -> Unit = {}): Result<Unit>

    /** Removes module data. */
    suspend fun remove(descriptor: DataModuleDescriptor): Result<Unit>

    /** Validates whether a module's data is intact. */
    suspend fun validate(descriptor: DataModuleDescriptor): Result<Boolean>
}
