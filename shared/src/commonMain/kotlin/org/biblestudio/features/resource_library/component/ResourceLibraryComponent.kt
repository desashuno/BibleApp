package org.biblestudio.features.resource_library.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry

/**
 * Observable state for the Resource Library pane.
 */
data class ResourceLibraryState(
    val resources: List<Resource> = emptyList(),
    val activeResource: Resource? = null,
    val entry: ResourceEntry? = null,
    val searchQuery: String = "",
    val searchResults: List<ResourceEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Business-logic boundary for the Resource Library pane.
 *
 * Subscribes to [LinkEvent.VerseSelected] and [LinkEvent.ResourceSelected]
 * from the VerseBus and loads resource entries.
 */
interface ResourceLibraryComponent {

    /** The current resource library state observable. */
    val state: StateFlow<ResourceLibraryState>

    /** Selects a resource to view its entries. */
    fun onResourceSelected(uuid: String)

    /** Updates the search query and triggers search. */
    fun onSearchQueryChanged(query: String)

    /** Navigates to a verse referenced in an entry. */
    fun onEntryVerseSelected(globalVerseId: Int)
}
