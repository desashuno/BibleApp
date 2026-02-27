package org.biblestudio.features.settings.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.biblestudio.features.settings.domain.entities.AppSetting

/**
 * Read/write access to key-value application settings.
 */
interface SettingsRepository {

    /** Returns a single setting by key. */
    suspend fun getSetting(key: String): Result<AppSetting?>

    /** Returns all settings, ordered by category and key. */
    suspend fun getAll(): Result<List<AppSetting>>

    /** Returns all settings in a specific category. */
    suspend fun getByCategory(category: String): Result<List<AppSetting>>

    /** Creates or replaces a setting (upsert). */
    suspend fun setSetting(setting: AppSetting): Result<Unit>

    /** Deletes a setting by key. */
    suspend fun delete(key: String): Result<Unit>

    /** Reactive stream of all settings. */
    fun watchAll(): Flow<List<AppSetting>>
}
