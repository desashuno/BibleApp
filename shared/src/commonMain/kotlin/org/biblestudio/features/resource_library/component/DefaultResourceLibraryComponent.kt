package org.biblestudio.features.resource_library.component

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
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.resource_library.domain.repositories.ResourceRepository

/**
 * Default [ResourceLibraryComponent] backed by Decompose lifecycle,
 * [ResourceRepository] for data, and [VerseBus] for cross-pane events.
 */
class DefaultResourceLibraryComponent(
    componentContext: ComponentContext,
    private val repository: ResourceRepository,
    private val verseBus: VerseBus
) : ResourceLibraryComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(ResourceLibraryState())
    override val state: StateFlow<ResourceLibraryState> = _state.asStateFlow()

    private var currentVerseId: Long? = null

    init {
        loadResources()
        observeVerseBus()
    }

    override fun onResourceSelected(uuid: String) {
        scope.launch {
            val resource = _state.value.resources.firstOrNull { it.uuid == uuid }
            _state.update { it.copy(activeResource = resource) }
            if (resource != null) {
                loadEntryForCurrentVerse(resource.uuid)
            }
        }
    }

    override fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length >= 2) {
            scope.launch {
                repository.searchEntries(query)
                    .onSuccess { results ->
                        _state.update { it.copy(searchResults = results) }
                    }
            }
        } else {
            _state.update { it.copy(searchResults = emptyList()) }
        }
    }

    override fun onEntryVerseSelected(globalVerseId: Int) {
        verseBus.publish(LinkEvent.VerseSelected(globalVerseId))
    }

    private fun loadResources() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getAllResources()
                .onSuccess { resources ->
                    _state.update { it.copy(resources = resources, isLoading = false) }
                }
                .onFailure { e ->
                    Napier.e("Failed to load resources", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun loadEntryForCurrentVerse(resourceId: String) {
        val verseId = currentVerseId ?: return
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getEntriesForVerse(resourceId, verseId)
                .onSuccess { entries ->
                    _state.update { it.copy(entry = entries.firstOrNull(), isLoading = false) }
                }
                .onFailure { e ->
                    Napier.e("Failed to load entry", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun observeVerseBus() {
        scope.launch {
            verseBus.events.collect { event ->
                when (event) {
                    is LinkEvent.VerseSelected -> {
                        currentVerseId = event.globalVerseId.toLong()
                        val resource = _state.value.activeResource
                        if (resource != null) {
                            loadEntryForCurrentVerse(resource.uuid)
                        }
                    }
                    is LinkEvent.ResourceSelected -> {
                        onResourceSelected(event.resourceId)
                    }
                    else -> { /* ignore */ }
                }
            }
        }
    }
}
