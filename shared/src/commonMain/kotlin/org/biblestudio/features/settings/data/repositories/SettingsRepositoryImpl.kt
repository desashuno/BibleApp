package org.biblestudio.features.settings.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.settings.data.mappers.toAppSetting
import org.biblestudio.features.settings.domain.entities.AppSetting
import org.biblestudio.features.settings.domain.repositories.SettingsRepository

internal class SettingsRepositoryImpl(
    private val database: BibleStudioDatabase
) : SettingsRepository {

    override suspend fun getSetting(key: String): Result<AppSetting?> = runCatching {
        database.settingsQueries
            .getSetting(key)
            .executeAsOneOrNull()
            ?.toAppSetting()
    }

    override suspend fun getAll(): Result<List<AppSetting>> = runCatching {
        database.settingsQueries
            .getAllSettings()
            .executeAsList()
            .map { it.toAppSetting() }
    }

    override suspend fun getByCategory(category: String): Result<List<AppSetting>> = runCatching {
        database.settingsQueries
            .getSettingsByCategory(category)
            .executeAsList()
            .map { it.toAppSetting() }
    }

    override suspend fun setSetting(setting: AppSetting): Result<Unit> = runCatching {
        database.settingsQueries.setSetting(
            key = setting.key,
            value = setting.value,
            type = setting.type,
            category = setting.category
        )
    }

    override suspend fun delete(key: String): Result<Unit> = runCatching {
        database.settingsQueries.deleteSetting(key)
    }

    override fun watchAll(): Flow<List<AppSetting>> = database.settingsQueries
        .getAllSettings()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { it.toAppSetting() } }
}
