package org.biblestudio.features.theological_atlas.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.theological_atlas.domain.entities.AtlasLocation
import org.biblestudio.features.theological_atlas.domain.entities.AtlasRegion

/**
 * Observable state for the Theological Atlas pane.
 */
data class AtlasState(
    val locations: List<AtlasLocation> = emptyList(),
    val regions: List<AtlasRegion> = emptyList(),
    val selectedLocation: AtlasLocation? = null,
    val centerLat: Double = 31.7683,
    val centerLng: Double = 35.2137,
    val zoomLevel: Float = 7f,
    val searchQuery: String = "",
    val searchResults: List<AtlasLocation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Business-logic boundary for the Theological Atlas pane.
 */
interface AtlasComponent {

    val state: StateFlow<AtlasState>

    /** Select a location and show its detail card. */
    fun onLocationSelected(location: AtlasLocation)

    /** Pan/zoom the map to a new center and zoom level. */
    fun onMapMoved(lat: Double, lng: Double, zoom: Float)

    /** Perform a full-text search on locations. */
    fun onSearch(query: String)

    /** Navigate to a predefined region. */
    fun onRegionSelected(region: AtlasRegion)

    /** Clear the selected location. */
    fun onClearSelection()
}
