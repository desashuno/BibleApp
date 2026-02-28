package org.biblestudio.features.theological_atlas.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.theological_atlas.data.mappers.toAtlasLocation
import org.biblestudio.features.theological_atlas.data.mappers.toAtlasRegion
import org.biblestudio.features.theological_atlas.domain.entities.AtlasLocation
import org.biblestudio.features.theological_atlas.domain.entities.AtlasRegion
import org.biblestudio.features.theological_atlas.domain.repositories.AtlasRepository

internal class AtlasRepositoryImpl(
    private val database: BibleStudioDatabase
) : AtlasRepository {

    override suspend fun getLocation(locationId: Long): Result<AtlasLocation?> = runCatching {
        val row = database.atlasQueries
            .getLocationById(locationId)
            .executeAsOneOrNull() ?: return@runCatching null

        val verseIds = database.atlasQueries
            .getVersesForLocation(locationId)
            .executeAsList()

        row.toAtlasLocation(verseIds)
    }

    override suspend fun getLocationsForVerse(globalVerseId: Long): Result<List<AtlasLocation>> = runCatching {
        database.atlasQueries
            .getLocationsForVerse(globalVerseId)
            .executeAsList()
            .map { it.toAtlasLocation() }
    }

    override suspend fun getLocationsByBounds(
        north: Double, south: Double, east: Double, west: Double
    ): Result<List<AtlasLocation>> = runCatching {
        database.atlasQueries
            .getLocationsByBounds(south = south, north = north, west = west, east = east)
            .executeAsList()
            .map { it.toAtlasLocation() }
    }

    override suspend fun getLocationsByType(type: String): Result<List<AtlasLocation>> = runCatching {
        database.atlasQueries
            .getLocationsByType(type)
            .executeAsList()
            .map { it.toAtlasLocation() }
    }

    override suspend fun searchLocations(query: String, maxResults: Long): Result<List<AtlasLocation>> = runCatching {
        database.atlasQueries
            .searchLocations(query = query, maxResults = maxResults)
            .executeAsList()
            .map { it.toAtlasLocation() }
    }

    override suspend fun getRegions(): Result<List<AtlasRegion>> = runCatching {
        database.atlasQueries
            .getAllRegions()
            .executeAsList()
            .map { it.toAtlasRegion() }
    }

    override suspend fun getLocationCount(): Result<Long> = runCatching {
        database.atlasQueries
            .locationCount()
            .executeAsOne()
    }
}
