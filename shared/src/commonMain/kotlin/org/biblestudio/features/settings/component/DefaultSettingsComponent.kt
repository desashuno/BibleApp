package org.biblestudio.features.settings.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
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
private const val CATEGORY_DISPLAY = "display"
private const val CATEGORY_READING = "reading"
private const val CATEGORY_WORKSPACE = "workspace"
private const val MIN_FONT_SIZE = 12
private const val MAX_FONT_SIZE = 28

/**
 * Default [SettingsComponent] backed by [SettingsRepository].
 */
class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val repository: SettingsRepository,
    private val workspaceRepository: WorkspaceRepository? = null
) : SettingsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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
        val clamped = size.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        _state.update { it.copy(fontSize = clamped) }
        updateSetting(KEY_FONT_SIZE, clamped.toString(), "int", CATEGORY_DISPLAY)
    }

    override fun setTheme(mode: ThemeMode) {
        _state.update { it.copy(theme = mode) }
        updateSetting(KEY_THEME, mode.name.lowercase(), "string", CATEGORY_DISPLAY)
    }

    override fun setDefaultBible(abbreviation: String) {
        _state.update { it.copy(defaultBible = abbreviation) }
        updateSetting(KEY_DEFAULT_BIBLE, abbreviation, "string", CATEGORY_READING)
    }

    override fun setShowVerseNumbers(show: Boolean) {
        _state.update { it.copy(showVerseNumbers = show) }
        updateSetting(KEY_SHOW_VERSE_NUMBERS, show.toString(), "boolean", CATEGORY_READING)
    }

    override fun setRedLetter(enabled: Boolean) {
        _state.update { it.copy(redLetter = enabled) }
        updateSetting(KEY_RED_LETTER, enabled.toString(), "boolean", CATEGORY_READING)
    }

    override fun setParagraphMode(enabled: Boolean) {
        _state.update { it.copy(paragraphMode = enabled) }
        updateSetting(KEY_PARAGRAPH_MODE, enabled.toString(), "boolean", CATEGORY_READING)
    }

    override fun setContinuousScroll(enabled: Boolean) {
        _state.update { it.copy(continuousScroll = enabled) }
        updateSetting(KEY_CONTINUOUS_SCROLL, enabled.toString(), "boolean", CATEGORY_READING)
    }

    override fun setSidebarCollapsed(collapsed: Boolean) {
        _state.update { it.copy(sidebarCollapsed = collapsed) }
        updateSetting(KEY_SIDEBAR_COLLAPSED, collapsed.toString(), "boolean", CATEGORY_WORKSPACE)
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
        updateSetting(KEY_PINNED_PANES, _state.value.pinnedPanes.joinToString(","), "string", CATEGORY_WORKSPACE)
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
        updateSetting(KEY_FAVORITE_PANES, _state.value.favoritePanes.joinToString(","), "string", CATEGORY_WORKSPACE)
    }

    override fun loadLayouts() {
        scope.launch {
            workspaceRepository?.getAll()
                ?.onSuccess { workspaces ->
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
                ?.onFailure { e ->
                    Napier.e("Failed to load workspace layouts", e)
                }
        }
    }

    override fun saveLayout(name: String) {
        scope.launch {
            val uuid = generateUuid()
            val now = Clock.System.now().toString()
            val workspace = org.biblestudio.features.workspace.domain.entities.Workspace(
                uuid = uuid,
                name = name,
                isActive = false,
                createdAt = now,
                updatedAt = now,
                deviceId = "local"
            )
            workspaceRepository?.create(workspace)
                ?.onSuccess { loadLayouts() }
                ?.onFailure { e -> Napier.e("Failed to save layout", e) }
        }
    }

    override fun renameLayout(layoutId: String, newName: String) {
        scope.launch {
            workspaceRepository?.getByUuid(layoutId)
                ?.onSuccess { ws ->
                    if (ws != null) {
                        val updated = ws.copy(name = newName, updatedAt = Clock.System.now().toString())
                        workspaceRepository?.update(updated)
                            ?.onSuccess { loadLayouts() }
                    }
                }
        }
    }

    override fun deleteLayout(layoutId: String) {
        scope.launch {
            workspaceRepository?.delete(layoutId, Clock.System.now().toString())
                ?.onSuccess { loadLayouts() }
                ?.onFailure { e -> Napier.e("Failed to delete layout", e) }
        }
    }

    override fun activateLayout(layoutId: String) {
        scope.launch {
            workspaceRepository?.setActive(layoutId, Clock.System.now().toString(), "local")
                ?.onSuccess { loadLayouts() }
                ?.onFailure { e -> Napier.e("Failed to activate layout", e) }
        }
    }

    private fun generateUuid(): String {
        val chars = "abcdef0123456789"
        val segments = listOf(UUID_SEGMENT_8, UUID_SEGMENT_4, UUID_SEGMENT_4, UUID_SEGMENT_4, UUID_SEGMENT_12)
        return segments.joinToString("-") { len ->
            (1..len).map { chars.random() }.joinToString("")
        }
    }

    companion object {
        private const val UUID_SEGMENT_8 = 8
        private const val UUID_SEGMENT_4 = 4
        private const val UUID_SEGMENT_12 = 12
    }
}
