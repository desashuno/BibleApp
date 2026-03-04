package org.biblestudio.features.theological_atlas.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class AtlasRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: AtlasRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = AtlasRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun insertLocation(
        name: String,
        lat: Double,
        lng: Double,
        type: String = "City",
        description: String = "",
        modernName: String? = null,
        era: String = "AllEras"
    ): Long {
        testDb.database.atlasQueries.insertLocation(
            name = name,
            modernName = modernName,
            latitude = lat,
            longitude = lng,
            type = type,
            description = description,
            era = era
        )
        return testDb.database.atlasQueries.lastInsertLocationId().executeAsOne()
    }

    private fun insertLocationVerse(locationId: Long, globalVerseId: Long) {
        testDb.database.atlasQueries.insertLocationVerse(
            locationId = locationId,
            globalVerseId = globalVerseId
        )
    }

    @Test
    fun `getLocation returns location with verse IDs`() = runTest {
        val id = insertLocation("Jerusalem", 31.7683, 35.2137, description = "Holy City", modernName = "Jerusalem")
        insertLocationVerse(id, 1001001)

        val loc = repo.getLocation(id).getOrThrow()
        assertNotNull(loc)
        assertEquals("Jerusalem", loc.name)
        assertEquals(1, loc.verseIds.size)
        assertEquals("Jerusalem", loc.modernName)
    }

    @Test
    fun `getLocation returns null for non-existent ID`() = runTest {
        val loc = repo.getLocation(99999).getOrThrow()
        assertNull(loc)
    }

    @Test
    fun `getLocationsForVerse returns linked locations`() = runTest {
        val id1 = insertLocation("Jerusalem", 31.7683, 35.2137)
        val id2 = insertLocation("Bethlehem", 31.7054, 35.2024)
        insertLocationVerse(id1, 1001001)
        insertLocationVerse(id2, 1001001)

        val locs = repo.getLocationsForVerse(1001001).getOrThrow()
        assertEquals(2, locs.size)
    }

    @Test
    fun `getLocationsByBounds returns locations in bounding box`() = runTest {
        insertLocation("Jerusalem", 31.7683, 35.2137)
        insertLocation("Rome", 41.9028, 12.4964)

        val locs = repo.getLocationsByBounds(north = 33.0, south = 30.0, east = 36.0, west = 34.0).getOrThrow()
        assertEquals(1, locs.size)
        assertEquals("Jerusalem", locs.first().name)
    }

    @Test
    fun `getLocationsByType filters correctly`() = runTest {
        insertLocation("Jerusalem", 31.7683, 35.2137, type = "City")
        insertLocation("Mount Sinai", 28.5394, 33.9753, type = "Mountain")
        insertLocation("Bethlehem", 31.7054, 35.2024, type = "City")

        val cities = repo.getLocationsByType("City").getOrThrow()
        assertEquals(2, cities.size)
    }

    @Test
    fun `searchLocations finds by name`() = runTest {
        insertLocation("Jerusalem", 31.7683, 35.2137, description = "Holy City")
        insertLocation("Jericho", 31.8611, 35.4611)

        val results = repo.searchLocations("Jerusalem").getOrThrow()
        assertEquals(1, results.size)
    }

    @Test
    fun `getLocationCount returns correct count`() = runTest {
        insertLocation("A", 0.0, 0.0)
        insertLocation("B", 1.0, 1.0)

        val count = repo.getLocationCount().getOrThrow()
        assertEquals(2, count)
    }
}
