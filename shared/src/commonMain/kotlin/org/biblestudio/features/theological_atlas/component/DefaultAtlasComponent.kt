package org.biblestudio.features.theological_atlas.component

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.util.componentScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.theological_atlas.domain.entities.AtlasLocation
import org.biblestudio.features.theological_atlas.domain.entities.AtlasRegion
import org.biblestudio.features.theological_atlas.domain.repositories.AtlasRepository

internal class DefaultAtlasComponent(
    componentContext: ComponentContext,
    private val repository: AtlasRepository,
    private val verseBus: VerseBus
) : AtlasComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(AtlasState())
    override val state: StateFlow<AtlasState> = _state.asStateFlow()

    init {
        loadRegions()
        loadVisibleLocations()
        observeVerseBus()
    }

    override fun onLocationSelected(location: AtlasLocation) {
        _state.update {
            it.copy(
                selectedLocation = location,
                centerLat = location.latitude,
                centerLng = location.longitude
            )
        }
        location.verseIds.firstOrNull()?.let { verseId ->
            verseBus.publish(LinkEvent.VerseSelected(verseId.toInt()))
        }
    }

    override fun onMapMoved(lat: Double, lng: Double, zoom: Float) {
        _state.update { it.copy(centerLat = lat, centerLng = lng, zoomLevel = zoom) }
        loadVisibleLocations()
    }

    override fun onSearch(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList()) }
            return
        }
        scope.launch {
            repository.searchLocations(query)
                .onSuccess { results -> _state.update { it.copy(searchResults = results) } }
                .onFailure { e ->
                    Napier.e("Failed to search atlas locations", e)
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    override fun onRegionSelected(region: AtlasRegion) {
        val lat = (region.boundsNorth + region.boundsSouth) / 2.0
        val lng = (region.boundsEast + region.boundsWest) / 2.0
        _state.update { it.copy(centerLat = lat, centerLng = lng, zoomLevel = REGION_ZOOM) }
        loadVisibleLocations()
    }

    override fun onClearSelection() {
        _state.update { it.copy(selectedLocation = null) }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { event ->
                    loadLocationsForVerse(event.globalVerseId.toLong())
                }
        }
    }

    private fun loadLocationsForVerse(globalVerseId: Long) {
        scope.launch {
            repository.getLocationsForVerse(globalVerseId)
                .onSuccess { locations ->
                    _state.update { it.copy(locations = locations) }
                    locations.firstOrNull()?.let { first ->
                        _state.update {
                            it.copy(selectedLocation = first, centerLat = first.latitude, centerLng = first.longitude)
                        }
                    }
                }
                .onFailure { e -> Napier.e("Failed to load locations for verse", e) }
        }
    }

    private fun loadVisibleLocations() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val s = _state.value
            val span = DEGREES_AT_ZOOM_1 / s.zoomLevel
            repository.getLocationsByBounds(
                north = s.centerLat + span,
                south = s.centerLat - span,
                east = s.centerLng + span,
                west = s.centerLng - span
            )
                .onSuccess { locs -> _state.update { it.copy(locations = locs, isLoading = false) } }
                .onFailure { e ->
                    Napier.e("Failed to load atlas locations", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun loadRegions() {
        scope.launch {
            repository.getRegions()
                .onSuccess { regions -> _state.update { it.copy(regions = regions) } }
                .onFailure { e -> Napier.e("Failed to load atlas regions", e) }
        }
    }

    companion object {
        private const val DEGREES_AT_ZOOM_1 = 40.0
        private const val REGION_ZOOM = 8f
    }
}
