package org.biblestudio.core.data_manager.model

/**
 * Observable state for the centralized data manager.
 */
data class DataManagerState(
    val modules: List<DataModuleDescriptor> = emptyList(),
    val activeDownloads: Map<String, Float> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)
