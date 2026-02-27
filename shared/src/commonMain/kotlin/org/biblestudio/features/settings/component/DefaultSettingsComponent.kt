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
import org.biblestudio.features.settings.domain.entities.AppSetting
import org.biblestudio.features.settings.domain.repositories.SettingsRepository
import org.biblestudio.features.workspace.domain.repositories.WorkspaceRepository

private const val KEY_FONT_SIZE = "font_size"
private const val KEY_THEME = "theme"
private const val KEY_DEFAULT_BIBLE = "default_bible"
private const val CATEGORY_DISPLAY = "display"
private const val CATEGORY_READING = "reading"
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
                    _state.update { it.copy(error = "Could not save setting.") }
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

                    _state.update {
                        it.copy(
                            groups = groups,
                            fontSize = fontSizeSetting?.value?.toIntOrNull()
                                ?: SettingsState.DEFAULT_FONT_SIZE,
                            theme = themeSetting?.value?.let { v ->
                                ThemeMode.entries.firstOrNull { e -> e.name.equals(v, ignoreCase = true) }
                            } ?: ThemeMode.SYSTEM,
                            defaultBible = defaultBibleSetting?.value ?: "",
                            isLoading = false,
                            error = null
                        )
                    }
                    Napier.d("Loaded ${settings.size} settings in ${groups.size} groups")
                }
                .onFailure { e ->
                    Napier.e("Failed to load settings", e)
                    _state.update {
                        it.copy(isLoading = false, error = "Could not load settings.")
                    }
                }
        }
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
            val workspace = org.biblestudio.features.workspace.domain.entities.Workspace(
                uuid = uuid,
                name = name,
                isActive = false,
                createdAt = PLACEHOLDER_TIMESTAMP,
                updatedAt = PLACEHOLDER_TIMESTAMP,
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
                        val updated = ws.copy(name = newName, updatedAt = PLACEHOLDER_TIMESTAMP)
                        workspaceRepository?.update(updated)
                            ?.onSuccess { loadLayouts() }
                    }
                }
        }
    }

    override fun deleteLayout(layoutId: String) {
        scope.launch {
            workspaceRepository?.delete(layoutId, PLACEHOLDER_TIMESTAMP)
                ?.onSuccess { loadLayouts() }
                ?.onFailure { e -> Napier.e("Failed to delete layout", e) }
        }
    }

    override fun activateLayout(layoutId: String) {
        scope.launch {
            workspaceRepository?.setActive(layoutId, PLACEHOLDER_TIMESTAMP, "local")
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

        /** Placeholder timestamp — a proper Clock injection will replace this. */
        private const val PLACEHOLDER_TIMESTAMP = ""
    }
}
