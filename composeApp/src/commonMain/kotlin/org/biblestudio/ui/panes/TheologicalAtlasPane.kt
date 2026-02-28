package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.theological_atlas.component.AtlasState
import org.biblestudio.features.theological_atlas.domain.entities.AtlasLocation
import org.biblestudio.features.theological_atlas.domain.entities.AtlasRegion
import org.biblestudio.features.theological_atlas.domain.entities.LocationType
import org.biblestudio.ui.map.MapPin
import org.biblestudio.ui.map.OsmMapState
import org.biblestudio.ui.map.OsmTileMap
import org.biblestudio.ui.theme.Spacing

/**
 * Theological Atlas pane: OpenStreetMap tile map with biblical location pins and detail card.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun TheologicalAtlasPane(
    stateFlow: StateFlow<AtlasState>,
    onLocationSelected: (AtlasLocation) -> Unit,
    onMapMoved: (Double, Double, Float) -> Unit,
    onSearch: (String) -> Unit,
    onRegionSelected: (AtlasRegion) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    var selectedRegion by remember { mutableStateOf<AtlasRegion?>(null) }

    // Map state synced with component state
    val mapState = remember {
        OsmMapState(
            initialCenterLat = state.centerLat,
            initialCenterLng = state.centerLng,
            initialZoom = state.zoomLevel.toInt().coerceIn(OsmMapState.MIN_ZOOM, OsmMapState.MAX_ZOOM)
        )
    }

    // Build lookup for pin → location ID resolution
    val locationById = remember(state.locations) {
        state.locations.associateBy { it.id }
    }

    // Convert AtlasLocations to MapPins
    val pins = remember(state.locations, state.selectedLocation) {
        state.locations.map { loc ->
            MapPin(
                id = loc.id,
                latitude = loc.latitude,
                longitude = loc.longitude,
                label = loc.name,
                color = locationColor(loc.type),
                isSelected = state.selectedLocation?.id == loc.id
            )
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            placeholder = { Text("Search locations…") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Space16)
        )

        // Region chips
        if (state.regions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Space16)
            ) {
                items(state.regions) { region ->
                    FilterChip(
                        selected = selectedRegion?.id == region.id,
                        onClick = {
                            selectedRegion = if (selectedRegion?.id == region.id) null else region
                            onRegionSelected(region)
                        },
                        label = { Text(region.name) },
                        modifier = Modifier.padding(end = Spacing.Space4)
                    )
                }
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(Spacing.Space24)
            )
        }

        state.error?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(Spacing.Space16)
            )
        }

        // Map or search results
        if (state.searchResults.isNotEmpty()) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(state.searchResults, key = { it.id }) { location ->
                    LocationListItem(location = location, onClick = { onLocationSelected(location) })
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // OSM tile map with biblical location pins
                OsmTileMap(
                    mapState = mapState,
                    pins = pins,
                    onPinClicked = { pin ->
                        locationById[pin.id]?.let { onLocationSelected(it) }
                    },
                    onZoomChanged = { zoom ->
                        onMapMoved(mapState.centerLat, mapState.centerLng, zoom.toFloat())
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Zoom controls overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.Space8)
                ) {
                    FilledTonalButton(
                        onClick = {
                            mapState.zoomIn()
                            onMapMoved(mapState.centerLat, mapState.centerLng, mapState.zoom.toFloat())
                        },
                        modifier = Modifier.size(40.dp)
                    ) { Text("+") }
                    Spacer(modifier = Modifier.height(4.dp))
                    FilledTonalButton(
                        onClick = {
                            mapState.zoomOut()
                            onMapMoved(mapState.centerLat, mapState.centerLng, mapState.zoom.toFloat())
                        },
                        modifier = Modifier.size(40.dp)
                    ) { Text("−") }
                }

                // Empty state overlay
                if (state.locations.isEmpty() && !state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(modifier = Modifier.padding(Spacing.Space24)) {
                            Text(
                                text = "Select a verse or search for locations",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(Spacing.Space16)
                            )
                        }
                    }
                }
            }
        }

        // Location detail card
        state.selectedLocation?.let { location ->
            HorizontalDivider()
            LocationDetailCard(location = location, onDismiss = onClearSelection)
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun LocationDetailCard(location: AtlasLocation, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.Space8)
    ) {
        Column(modifier = Modifier.padding(Spacing.Space16)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "✕",
                    modifier = Modifier.clickable(onClick = onDismiss).padding(Spacing.Space4),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(Spacing.Space4))
            Text(
                text = "${location.type.displayName}  •  ${location.latitude.format(4)}, ${location.longitude.format(4)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            location.modernName?.let { modern ->
                Text(
                    text = "Modern: $modern",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (location.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(Spacing.Space8))
                Text(text = location.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun LocationListItem(location: AtlasLocation, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Space16, vertical = Spacing.Space8)
    ) {
        Text(text = location.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(text = location.type.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.Space16))
}

@Suppress("MagicNumber")
private fun locationColor(type: LocationType): Color = when (type) {
    LocationType.City -> Color(0xFFE91E63)
    LocationType.Town -> Color(0xFFFF5722)
    LocationType.Village -> Color(0xFFFF9800)
    LocationType.Mountain -> Color(0xFF795548)
    LocationType.River -> Color(0xFF2196F3)
    LocationType.Lake -> Color(0xFF03A9F4)
    LocationType.Sea -> Color(0xFF0288D1)
    LocationType.Region -> Color(0xFF4CAF50)
    LocationType.Desert -> Color(0xFFFFC107)
    LocationType.Valley -> Color(0xFF8BC34A)
    LocationType.Island -> Color(0xFF009688)
}

private fun Double.format(digits: Int): String = "%.${digits}f".format(this)
