package org.biblestudio.features.resource_library.component

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.util.componentScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.data_manager.DataManager
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleType

/**
 * Default [ResourceLibraryComponent] backed by the centralized [DataManager].
 *
 * Collects the DataManager state and applies local filters (type, search query).
 */
internal class DefaultResourceLibraryComponent(
    componentContext: ComponentContext,
    private val dataManager: DataManager
) : ResourceLibraryComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(ResourceLibraryState())
    override val state: StateFlow<ResourceLibraryState> = _state.asStateFlow()

    init {
        observeDataManager()
    }

    override fun onModuleSelected(moduleId: String) {
        val module = _state.value.filteredModules.firstOrNull { it.moduleId == moduleId }
        _state.update { it.copy(selectedModule = module) }
    }

    override fun onInstallModule(moduleId: String) {
        scope.launch { dataManager.installModule(moduleId) }
    }

    override fun onRemoveModule(moduleId: String) {
        scope.launch { dataManager.removeModule(moduleId) }
        _state.update {
            if (it.selectedModule?.moduleId == moduleId) it.copy(selectedModule = null) else it
        }
    }

    override fun onCancelDownload(moduleId: String) {
        dataManager.cancelDownload(moduleId)
    }

    override fun onFilterTypeChanged(type: DataModuleType?) {
        _state.update { it.copy(filterType = type) }
        applyFilters()
    }

    override fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    override fun onToggleModuleActive(moduleId: String) {
        val module = _state.value.modules.firstOrNull { it.moduleId == moduleId } ?: return
        scope.launch { dataManager.setModuleActive(moduleId, !module.isActive) }
    }

    private fun observeDataManager() {
        scope.launch {
            dataManager.state.collect { dmState ->
                _state.update { current ->
                    current.copy(
                        modules = dmState.modules,
                        activeDownloads = dmState.activeDownloads,
                        isLoading = dmState.isLoading,
                        error = dmState.error
                    )
                }
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        _state.update { current ->
            val filtered = current.modules
                .let { modules ->
                    val type = current.filterType
                    if (type != null) modules.filter { it.type == type } else modules
                }
                .let { modules ->
                    val query = current.searchQuery
                    if (query.isNotBlank()) {
                        modules.filter { matchesSearch(it, query) }
                    } else {
                        modules
                    }
                }
            current.copy(filteredModules = filtered)
        }
    }

    private fun matchesSearch(module: DataModuleDescriptor, query: String): Boolean =
        module.name.contains(query, ignoreCase = true) ||
            module.description.contains(query, ignoreCase = true) ||
            module.type.value.contains(query, ignoreCase = true)
}
