package org.biblestudio.features.settings.data.mappers

import migrations.Settings
import org.biblestudio.features.settings.domain.entities.AppSetting

internal fun Settings.toAppSetting(): AppSetting = AppSetting(
    key = key,
    value = value_,
    type = type,
    category = category
)
