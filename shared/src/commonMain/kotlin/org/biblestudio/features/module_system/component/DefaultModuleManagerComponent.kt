package org.biblestudio.features.module_system.component

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
import org.biblestudio.features.module_system.domain.entities.InstalledModule
import org.biblestudio.features.module_system.domain.entities.ModuleSource
import org.biblestudio.features.module_system.domain.repositories.ModuleRepository

/**
 * Default [ModuleManagerComponent] backed by [ModuleRepository].
 */
class DefaultModuleManagerComponent(
    componentContext: ComponentContext,
    private val repository: ModuleRepository
) : ModuleManagerComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(ModuleManagerState())
    override val state: StateFlow<ModuleManagerState> = _state.asStateFlow()

    init {
        loadModules()
    }

    override fun loadModules() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            repository.getInstalledModules()
                .onSuccess { modules ->
                    _state.update {
                        it.copy(installedModules = modules, isLoading = false)
                    }
                    Napier.d("Loaded ${modules.size} installed modules")
                }
                .onFailure { e ->
                    Napier.e("Failed to load modules", e)
                    _state.update {
                        it.copy(isLoading = false, error = "Could not load modules.")
                    }
                }
        }
    }

    override fun installModule(source: ModuleSource) {
        _state.update { it.copy(installProgress = 0f, error = null) }
        scope.launch {
            repository.installModule(source) { progress ->
                _state.update { it.copy(installProgress = progress) }
            }
                .onSuccess { module ->
                    _state.update { it.copy(installProgress = null) }
                    loadModules()
                    Napier.i("Module installed: ${module.name}")
                }
                .onFailure { e ->
                    Napier.e("Module installation failed", e)
                    _state.update {
                        it.copy(installProgress = null, error = "Installation failed: ${e.message}")
                    }
                }
        }
    }

    override fun removeModule(uuid: String) {
        scope.launch {
            repository.removeModule(uuid)
                .onSuccess {
                    loadModules()
                    Napier.i("Module removed: $uuid")
                }
                .onFailure { e ->
                    Napier.e("Failed to remove module", e)
                    _state.update { it.copy(error = "Could not remove module.") }
                }
        }
    }

    override fun selectModule(module: InstalledModule) {
        _state.update { it.copy(selectedModule = module) }
    }

    override fun requestImport() {
        // Platform-specific file picker should supply a ModuleSource callback.
        // For now, log and no-op — desktop/iOS/Android wrappers will override.
        Napier.d("Import requested — platform file picker required")
    }
}
