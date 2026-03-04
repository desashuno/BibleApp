package org.biblestudio.features.resource_library.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleType

/**
 * Observable state for the Resource Library pane.
 *
 * Now backed by the centralized DataManager instead of the old ResourceRepository.
 */
data class ResourceLibraryState(
    val modules: List<DataModuleDescriptor> = emptyList(),
    val filteredModules: List<DataModuleDescriptor> = emptyList(),
    val selectedModule: DataModuleDescriptor? = null,
    val filterType: DataModuleType? = null,
    val searchQuery: String = "",
    val activeDownloads: Map<String, Float> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Business-logic boundary for the Resource Library pane.
 *
 * Manages data module browsing, installation, and removal
 * through the centralized [DataManager].
 */
interface ResourceLibraryComponent {

    /** The current resource library state observable. */
    val state: StateFlow<ResourceLibraryState>

    /** Selects a module for detail view. */
    fun onModuleSelected(moduleId: String)

    /** Installs a module by ID. */
    fun onInstallModule(moduleId: String)

    /** Removes a module by ID. */
    fun onRemoveModule(moduleId: String)

    /** Cancels an active download. */
    fun onCancelDownload(moduleId: String)

    /** Filters modules by type (null = show all). */
    fun onFilterTypeChanged(type: DataModuleType?)

    /** Updates the search query for filtering. */
    fun onSearchQueryChanged(query: String)

    /** Toggles a module's active/inactive state. */
    fun onToggleModuleActive(moduleId: String)
}
