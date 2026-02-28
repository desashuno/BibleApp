package org.biblestudio.features.theological_atlas.domain.repositories

import org.biblestudio.features.theological_atlas.domain.entities.AtlasLocation
import org.biblestudio.features.theological_atlas.domain.entities.AtlasRegion

/**
 * Repository for querying the biblical atlas.
 */
interface AtlasRepository {

    /** Returns a single location by ID, or null if not found. */
    suspend fun getLocation(locationId: Long): Result<AtlasLocation?>

    /** Returns locations linked to a specific verse. */
    suspend fun getLocationsForVerse(globalVerseId: Long): Result<List<AtlasLocation>>

    /** Returns locations within a geographic bounding box. */
    suspend fun getLocationsByBounds(
        north: Double, south: Double, east: Double, west: Double
    ): Result<List<AtlasLocation>>

    /** Returns locations filtered by type. */
    suspend fun getLocationsByType(type: String): Result<List<AtlasLocation>>

    /** Full-text search on location name and description. */
    suspend fun searchLocations(query: String, maxResults: Long = 50): Result<List<AtlasLocation>>

    /** Returns all predefined regions. */
    suspend fun getRegions(): Result<List<AtlasRegion>>

    /** Returns total location count. */
    suspend fun getLocationCount(): Result<Long>
}
