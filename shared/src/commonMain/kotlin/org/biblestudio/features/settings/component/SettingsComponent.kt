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
    val showVerseNumbers: Boolean = true,
    val redLetter: Boolean = false,
    val paragraphMode: Boolean = false,
    val continuousScroll: Boolean = false,
    val sidebarCollapsed: Boolean = false,
    val pinnedPanes: Set<String> = DEFAULT_PINNED_PANES,
    val favoritePanes: Set<String> = emptySet(),
    val savedLayouts: List<SavedLayout> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        const val DEFAULT_FONT_SIZE = 16
        val DEFAULT_PINNED_PANES = setOf(
            "bible-reader", "search", "note-editor", "dashboard", "cross-references"
        )
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

    /** Sets whether verse numbers are shown. */
    fun setShowVerseNumbers(show: Boolean)

    /** Sets whether red-letter words are enabled. */
    fun setRedLetter(enabled: Boolean)

    /** Sets whether paragraph mode is enabled. */
    fun setParagraphMode(enabled: Boolean)

    /** Sets whether continuous scroll mode is enabled. */
    fun setContinuousScroll(enabled: Boolean)

    /** Sets whether the sidebar is collapsed by default. */
    fun setSidebarCollapsed(collapsed: Boolean)

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

    /** Toggles whether a pane type is pinned to the sidebar. */
    fun togglePinned(paneType: String)

    /** Toggles whether a pane type is marked as favorite. */
    fun toggleFavorite(paneType: String)
}
