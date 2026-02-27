package org.biblestudio.features.module_system.data.mappers

import migrations.Installed_modules
import org.biblestudio.features.module_system.domain.entities.InstalledModule

internal fun Installed_modules.toInstalledModule(): InstalledModule = InstalledModule(
    id = id,
    uuid = uuid,
    name = name,
    abbreviation = abbreviation,
    language = language,
    type = type,
    version = version,
    sizeBytes = size_bytes,
    description = description,
    sourceType = source_type,
    installedAt = installed_at,
    isActive = is_active == 1L
)
