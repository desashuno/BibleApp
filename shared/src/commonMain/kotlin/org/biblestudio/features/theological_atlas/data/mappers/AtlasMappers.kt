package org.biblestudio.features.theological_atlas.data.mappers

import migrations.Atlas_locations
import migrations.Atlas_regions
import org.biblestudio.features.theological_atlas.domain.entities.AtlasLocation
import org.biblestudio.features.theological_atlas.domain.entities.AtlasRegion
import org.biblestudio.features.theological_atlas.domain.entities.LocationType

internal fun Atlas_locations.toAtlasLocation(verseIds: List<Long> = emptyList()): AtlasLocation = AtlasLocation(
    id = id,
    name = name,
    modernName = modern_name,
    latitude = latitude,
    longitude = longitude,
    type = LocationType.fromString(type),
    description = description,
    era = era,
    verseIds = verseIds
)

internal fun Atlas_regions.toAtlasRegion(): AtlasRegion = AtlasRegion(
    id = id,
    name = name,
    description = description,
    boundsNorth = bounds_north,
    boundsSouth = bounds_south,
    boundsEast = bounds_east,
    boundsWest = bounds_west
)
