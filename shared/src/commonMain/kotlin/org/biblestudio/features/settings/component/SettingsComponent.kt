package org.biblestudio.features.settings.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.settings.domain.entities.AppSetting

/**
 * A group of settings for UI display.
 */
data class SettingsGroup(
    val category: String,
    val settings: List<AppSetting>
)

/**
 * A saved workspace layout entry for display in settings.
 */
data class SavedLayout(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val updatedAt: String
)

/**
 * Observable state for the Settings screen.
 */
data class SettingsState(
    val groups: List<SettingsGroup> = emptyList(),
    val fontSize: Int = DEFAULT_FONT_SIZE,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val defaultBible: String = "",
    val savedLayouts: List<SavedLayout> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        const val DEFAULT_FONT_SIZE = 16
    }
}

/**
 * Theme selection mode.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

/**
 * Business-logic boundary for the Settings screen.
 */
interface SettingsComponent {

    /** The current settings state observable. */
    val state: StateFlow<SettingsState>

    /** Updates a single setting. */
    fun updateSetting(key: String, value: String, type: String, category: String)

    /** Sets the font size. */
    fun setFontSize(size: Int)

    /** Sets the theme mode. */
    fun setTheme(mode: ThemeMode)

    /** Sets the default Bible version. */
    fun setDefaultBible(abbreviation: String)

    /** Reloads all settings from the database. */
    fun reload()

    /** Loads saved workspace layouts for display. */
    fun loadLayouts()

    /** Saves a new workspace layout with the given name. */
    fun saveLayout(name: String)

    /** Renames an existing workspace layout. */
    fun renameLayout(layoutId: String, newName: String)

    /** Deletes a workspace layout. */
    fun deleteLayout(layoutId: String)

    /** Activates a workspace layout. */
    fun activateLayout(layoutId: String)
}
