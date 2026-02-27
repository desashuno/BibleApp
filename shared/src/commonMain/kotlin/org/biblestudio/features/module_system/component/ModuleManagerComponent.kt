package org.biblestudio.features.module_system.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.module_system.domain.entities.InstalledModule
import org.biblestudio.features.module_system.domain.entities.ModuleSource

/**
 * Observable state for the Module Manager pane.
 */
data class ModuleManagerState(
    val installedModules: List<InstalledModule> = emptyList(),
    val selectedModule: InstalledModule? = null,
    val installProgress: Float? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Business-logic boundary for the Module Manager pane.
 */
interface ModuleManagerComponent {

    /** The current module manager state observable. */
    val state: StateFlow<ModuleManagerState>

    /** Loads the list of installed modules. */
    fun loadModules()

    /** Installs a module from a given source. */
    fun installModule(source: ModuleSource)

    /** Removes an installed module. */
    fun removeModule(uuid: String)

    /** Selects a module for detail view. */
    fun selectModule(module: InstalledModule)

    /** Triggers file picker / import flow for a new module. */
    fun requestImport()
}
