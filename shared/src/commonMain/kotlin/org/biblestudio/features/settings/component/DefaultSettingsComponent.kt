package org.biblestudio.features.settings.component

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.util.componentScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.util.nowIso
import org.biblestudio.core.AppConstants
import org.biblestudio.core.util.generateUuid
import org.biblestudio.features.settings.domain.entities.AppSetting
import org.biblestudio.features.settings.domain.repositories.SettingsRepository
import org.biblestudio.features.workspace.domain.repositories.WorkspaceRepository

private const val KEY_FONT_SIZE = "font_size"
private const val KEY_THEME = "theme"
private const val KEY_DEFAULT_BIBLE = "default_bible"
private const val KEY_SHOW_VERSE_NUMBERS = "show_verse_numbers"
private const val KEY_RED_LETTER = "red_letter"
private const val KEY_PARAGRAPH_MODE = "paragraph_mode"
private const val KEY_CONTINUOUS_SCROLL = "continuous_scroll"
private const val KEY_SIDEBAR_COLLAPSED = "sidebar_collapsed"
private const val KEY_PINNED_PANES = "pinned_panes"
private const val KEY_FAVORITE_PANES = "favorite_panes"

/**
 * Default [SettingsComponent] backed by [SettingsRepository].
 */
@Suppress("TooManyFunctions")
internal class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val repository: SettingsRepository,
    private val workspaceRepository: WorkspaceRepository
) : SettingsComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(SettingsState())
    override val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        reload()
    }

    override fun updateSetting(key: String, value: String, type: String, category: String) {
        scope.launch {
            val setting = AppSetting(key = key, value = value, type = type, category = category)
            repository.setSetting(setting)
                .onSuccess { reload() }
                .onFailure { e ->
                    Napier.e("Failed to update setting '$key'", e)
                    _state.update { it.copy(error = "Could not save setting '$key': ${e.message}") }
                }
        }
    }

    override fun setFontSize(size: Int) {
        val clamped = size.coerceIn(AppConstants.FONT_SIZE_MIN, AppConstants.FONT_SIZE_MAX)
        _state.update { it.copy(fontSize = clamped) }
        updateSetting(KEY_FONT_SIZE, clamped.toString(), AppConstants.SettingType.INT, AppConstants.SettingCategory.DISPLAY)
    }

    override fun setTheme(mode: ThemeMode) {
        _state.update { it.copy(theme = mode) }
        updateSetting(KEY_THEME, mode.name.lowercase(), AppConstants.SettingType.STRING, AppConstants.SettingCategory.DISPLAY)
    }

    override fun setDefaultBible(abbreviation: String) {
        _state.update { it.copy(defaultBible = abbreviation) }
        updateSetting(KEY_DEFAULT_BIBLE, abbreviation, AppConstants.SettingType.STRING, AppConstants.SettingCategory.READING)
    }

    override fun setShowVerseNumbers(show: Boolean) {
        _state.update { it.copy(showVerseNumbers = show) }
        updateSetting(KEY_SHOW_VERSE_NUMBERS, show.toString(), AppConstants.SettingType.BOOLEAN, AppConstants.SettingCategory.READING)
    }

    override fun setRedLetter(enabled: Boolean) {
        _state.update { it.copy(redLetter = enabled) }
        updateSetting(KEY_RED_LETTER, enabled.toString(), AppConstants.SettingType.BOOLEAN, AppConstants.SettingCategory.READING)
    }

    override fun setParagraphMode(enabled: Boolean) {
        _state.update { it.copy(paragraphMode = enabled) }
        updateSetting(KEY_PARAGRAPH_MODE, enabled.toString(), AppConstants.SettingType.BOOLEAN, AppConstants.SettingCategory.READING)
    }

    override fun setContinuousScroll(enabled: Boolean) {
        _state.update { it.copy(continuousScroll = enabled) }
        updateSetting(KEY_CONTINUOUS_SCROLL, enabled.toString(), AppConstants.SettingType.BOOLEAN, AppConstants.SettingCategory.READING)
    }

    override fun setSidebarCollapsed(collapsed: Boolean) {
        _state.update { it.copy(sidebarCollapsed = collapsed) }
        updateSetting(KEY_SIDEBAR_COLLAPSED, collapsed.toString(), AppConstants.SettingType.BOOLEAN, AppConstants.SettingCategory.WORKSPACE)
    }

    override fun reload() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            repository.getAll()
                .onSuccess { settings ->
                    val groups = settings
                        .groupBy { it.category }
                        .map { (cat, list) -> SettingsGroup(cat, list) }
                        .sortedBy { it.category }

                    val fontSizeSetting = settings.firstOrNull { it.key == KEY_FONT_SIZE }
                    val themeSetting = settings.firstOrNull { it.key == KEY_THEME }
                    val defaultBibleSetting = settings.firstOrNull { it.key == KEY_DEFAULT_BIBLE }
                    val verseNumbersSetting = settings.firstOrNull { it.key == KEY_SHOW_VERSE_NUMBERS }
                    val redLetterSetting = settings.firstOrNull { it.key == KEY_RED_LETTER }
                    val paragraphModeSetting = settings.firstOrNull { it.key == KEY_PARAGRAPH_MODE }
                    val continuousScrollSetting = settings.firstOrNull { it.key == KEY_CONTINUOUS_SCROLL }
                    val sidebarSetting = settings.firstOrNull { it.key == KEY_SIDEBAR_COLLAPSED }
                    val pinnedSetting = settings.firstOrNull { it.key == KEY_PINNED_PANES }
                    val favoriteSetting = settings.firstOrNull { it.key == KEY_FAVORITE_PANES }

                    _state.update {
                        it.copy(
                            groups = groups,
                            fontSize = fontSizeSetting?.value?.toIntOrNull()
                                ?: SettingsState.DEFAULT_FONT_SIZE,
                            theme = themeSetting?.value?.let { v ->
                                ThemeMode.entries.firstOrNull { e -> e.name.equals(v, ignoreCase = true) }
                            } ?: ThemeMode.SYSTEM,
                            defaultBible = defaultBibleSetting?.value ?: "",
                            showVerseNumbers = verseNumbersSetting?.value?.toBooleanStrictOrNull() ?: true,
                            redLetter = redLetterSetting?.value?.toBooleanStrictOrNull() ?: false,
                            paragraphMode = paragraphModeSetting?.value?.toBooleanStrictOrNull() ?: false,
                            continuousScroll = continuousScrollSetting?.value?.toBooleanStrictOrNull() ?: false,
                            sidebarCollapsed = sidebarSetting?.value?.toBooleanStrictOrNull() ?: false,
                            pinnedPanes = pinnedSetting?.value
                                ?.split(",")?.filter { s -> s.isNotBlank() }?.toSet()
                                ?: SettingsState.DEFAULT_PINNED_PANES,
                            favoritePanes = favoriteSetting?.value
                                ?.split(",")?.filter { s -> s.isNotBlank() }?.toSet()
                                ?: emptySet(),
                            isLoading = false,
                            error = null
                        )
                    }
                    Napier.d("Loaded ${settings.size} settings in ${groups.size} groups")
                }
                .onFailure { e ->
                    Napier.e("Failed to load settings", e)
                    _state.update {
                        it.copy(isLoading = false, error = "Could not load settings: ${e.message}")
                    }
                }
        }
    }

    override fun togglePinned(paneType: String) {
        _state.update { current ->
            val newSet = if (paneType in current.pinnedPanes) {
                current.pinnedPanes - paneType
            } else {
                current.pinnedPanes + paneType
            }
            current.copy(pinnedPanes = newSet)
        }
        updateSetting(KEY_PINNED_PANES, _state.value.pinnedPanes.joinToString(","), AppConstants.SettingType.STRING, AppConstants.SettingCategory.WORKSPACE)
    }

    override fun toggleFavorite(paneType: String) {
        _state.update { current ->
            val newSet = if (paneType in current.favoritePanes) {
                current.favoritePanes - paneType
            } else {
                current.favoritePanes + paneType
            }
            current.copy(favoritePanes = newSet)
        }
        updateSetting(KEY_FAVORITE_PANES, _state.value.favoritePanes.joinToString(","), AppConstants.SettingType.STRING, AppConstants.SettingCategory.WORKSPACE)
    }

    override fun loadLayouts() {
        scope.launch {
            workspaceRepository.getAll()
                .onSuccess { workspaces ->
                    val layouts = workspaces.map { ws ->
                        SavedLayout(
                            id = ws.uuid,
                            name = ws.name,
                            isActive = ws.isActive,
                            updatedAt = ws.updatedAt
                        )
                    }
                    _state.update { it.copy(savedLayouts = layouts) }
                }
                .onFailure { e ->
                    Napier.e("Failed to load workspace layouts", e)
                }
        }
    }

    override fun saveLayout(name: String) {
        scope.launch {
            val uuid = generateUuid()
            val now = nowIso()
            val workspace = org.biblestudio.features.workspace.domain.entities.Workspace(
                uuid = uuid,
                name = name,
                isActive = false,
                createdAt = now,
                updatedAt = now,
                deviceId = AppConstants.DEVICE_ID_LOCAL
            )
            workspaceRepository.create(workspace)
                .onSuccess { loadLayouts() }
                .onFailure { e -> Napier.e("Failed to save layout", e) }
        }
    }

    override fun renameLayout(layoutId: String, newName: String) {
        scope.launch {
            workspaceRepository.getByUuid(layoutId)
                .onSuccess { ws ->
                    if (ws != null) {
                        val updated = ws.copy(name = newName, updatedAt = nowIso())
                        workspaceRepository.update(updated)
                            .onSuccess { loadLayouts() }
                    }
                }
        }
    }

    override fun deleteLayout(layoutId: String) {
        scope.launch {
            workspaceRepository.delete(layoutId, nowIso())
                .onSuccess { loadLayouts() }
                .onFailure { e -> Napier.e("Failed to delete layout", e) }
        }
    }

    override fun activateLayout(layoutId: String) {
        scope.launch {
            workspaceRepository.setActive(layoutId, nowIso(), AppConstants.DEVICE_ID_LOCAL)
                .onSuccess { loadLayouts() }
                .onFailure { e -> Napier.e("Failed to activate layout", e) }
        }
    }

}
