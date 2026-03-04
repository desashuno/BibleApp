package org.biblestudio.core.data_manager

import io.github.aakira.napier.Napier
import org.biblestudio.core.util.componentScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.data_manager.model.DataManagerState
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleStatus
import org.biblestudio.core.data_manager.model.DataModuleType

/**
 * Default [DataManager] implementation.
 *
 * Owns a coroutine scope for async operations, delegates install/remove
 * to registered [DataModuleHandler]s, and persists state via [DataModuleRepository].
 */
internal class DefaultDataManager(
    private val repository: DataModuleRepository
) : DataManager {

    private val scope = componentScope()
    private val _state = MutableStateFlow(DataManagerState())
    override val state: StateFlow<DataManagerState> = _state.asStateFlow()

    private val handlers = mutableMapOf<DataModuleType, DataModuleHandler>()
    private val downloadJobs = mutableMapOf<String, Job>()

    init {
        scope.launch { loadModules() }
    }

    override suspend fun loadModules() {
        _state.update { it.copy(isLoading = true, error = null) }
        repository.getAllModules()
            .onSuccess { modules ->
                if (modules.isEmpty()) {
                    // Auto-populate from existing data tables (legacy/seeded DBs)
                    repository.autoPopulateFromExistingData()
                        .onSuccess { count ->
                            if (count > 0) {
                                Napier.i("Auto-populated $count data_modules from existing data")
                            }
                        }
                    // Reload after auto-populate
                    val refreshed = repository.getAllModules().getOrElse { emptyList() }
                    _state.update { it.copy(modules = refreshed, isLoading = false) }
                } else {
                    _state.update { it.copy(modules = modules, isLoading = false) }
                }
            }
            .onFailure { e ->
                Napier.e("Failed to load modules", e)
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
    }

    override suspend fun installModule(moduleId: String) {
        val descriptor = repository.getModuleById(moduleId).getOrNull() ?: run {
            _state.update { it.copy(error = "Module not found: $moduleId") }
            return
        }

        val handler = handlers[descriptor.type] ?: run {
            _state.update { it.copy(error = "No handler for type: ${descriptor.type}") }
            return
        }

        repository.updateStatus(moduleId, DataModuleStatus.Installing)
        _state.update { st ->
            st.copy(activeDownloads = st.activeDownloads + (moduleId to 0f))
        }
        refreshModule(moduleId)

        val job = scope.launch {
            handler.install(descriptor) { progress ->
                scope.launch { repository.updateProgress(moduleId, progress) }
                _state.update { st ->
                    st.copy(activeDownloads = st.activeDownloads + (moduleId to progress))
                }
            }
                .onSuccess {
                    repository.markInstalled(moduleId)
                    _state.update { st ->
                        st.copy(activeDownloads = st.activeDownloads - moduleId)
                    }
                    refreshModule(moduleId)
                    Napier.i("Module installed: $moduleId")
                }
                .onFailure { e ->
                    Napier.e("Install failed for $moduleId", e)
                    repository.updateStatus(moduleId, DataModuleStatus.Error)
                    _state.update { st ->
                        st.copy(
                            activeDownloads = st.activeDownloads - moduleId,
                            error = "Install failed: ${e.message}"
                        )
                    }
                    refreshModule(moduleId)
                }
        }
        downloadJobs[moduleId] = job
    }

    override suspend fun removeModule(moduleId: String) {
        val descriptor = repository.getModuleById(moduleId).getOrNull() ?: return

        val handler = handlers[descriptor.type]
        repository.updateStatus(moduleId, DataModuleStatus.Removing)
        refreshModule(moduleId)

        handler?.remove(descriptor)
            ?.onSuccess {
                repository.markRemoved(moduleId)
                refreshModule(moduleId)
                Napier.i("Module removed: $moduleId")
            }
            ?.onFailure { e ->
                Napier.e("Remove failed for $moduleId", e)
                repository.updateStatus(moduleId, DataModuleStatus.Error)
                refreshModule(moduleId)
            }
            ?: run {
                // No handler — just mark as removed
                repository.markRemoved(moduleId)
                refreshModule(moduleId)
            }
    }

    override fun cancelDownload(moduleId: String) {
        downloadJobs[moduleId]?.cancel()
        downloadJobs.remove(moduleId)
        _state.update { st ->
            st.copy(activeDownloads = st.activeDownloads - moduleId)
        }
        scope.launch {
            repository.updateStatus(moduleId, DataModuleStatus.Available)
            repository.updateProgress(moduleId, 0f)
            refreshModule(moduleId)
        }
    }

    override fun registerHandler(handler: DataModuleHandler) {
        handlers[handler.supportedType] = handler
    }

    override fun getModulesByType(type: DataModuleType): List<DataModuleDescriptor> =
        _state.value.modules.filter { it.type == type }

    override suspend fun setModuleActive(moduleId: String, isActive: Boolean) {
        repository.setModuleActive(moduleId, isActive)
            .onSuccess { refreshModule(moduleId) }
            .onFailure { e ->
                Napier.e("Failed to set module active: $moduleId", e)
                _state.update { it.copy(error = "Failed to toggle module: ${e.message}") }
            }
    }

    override fun getActiveModulesByType(type: DataModuleType): List<DataModuleDescriptor> =
        _state.value.modules.filter { it.type == type && it.isActive }

    private suspend fun refreshModule(moduleId: String) {
        repository.getModuleById(moduleId).getOrNull()?.let { updated ->
            _state.update { st ->
                st.copy(modules = st.modules.map { if (it.moduleId == moduleId) updated else it })
            }
        }
    }
}
