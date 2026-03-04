package org.biblestudio.core.data_manager.handlers

import org.biblestudio.core.data_manager.DataModuleHandler
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleType

/**
 * Handles geography data module installation.
 */
internal class GeographyModuleHandler : DataModuleHandler {
    override val supportedType: DataModuleType = DataModuleType.Geography

    override suspend fun install(descriptor: DataModuleDescriptor, progressCallback: (Float) -> Unit): Result<Unit> =
        Result.success(Unit)

    override suspend fun remove(descriptor: DataModuleDescriptor): Result<Unit> = Result.success(Unit)

    override suspend fun validate(descriptor: DataModuleDescriptor): Result<Boolean> = Result.success(true)
}
