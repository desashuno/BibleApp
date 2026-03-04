package org.biblestudio.features.timeline.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class TimelineRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: TimelineRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = TimelineRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun insertEvent(
        title: String,
        startYear: Int,
        endYear: Int? = null,
        category: String = "Patriarchs",
        importance: Int = 1,
        description: String = ""
    ): Long {
        testDb.database.timelineQueries.insertEvent(
            title = title,
            description = description,
            startYear = startYear.toLong(),
            endYear = endYear?.toLong(),
            category = category,
            importance = importance.toLong()
        )
        return testDb.database.timelineQueries.lastInsertEventId().executeAsOne()
    }

    private fun insertEventVerse(eventId: Long, globalVerseId: Long) {
        testDb.database.timelineQueries.insertEventVerse(
            eventId = eventId,
            globalVerseId = globalVerseId
        )
    }

    @Test
    fun `getEvents returns events in year range`() = runTest {
        insertEvent("Creation", -4000, -3900, "Creation")
        insertEvent("Abraham", -2000, -1900, "Patriarchs")
        insertEvent("Exodus", -1446, -1406, "Exodus")

        val events = repo.getEvents(-2100, -1400).getOrThrow()
        assertEquals(2, events.size)
    }

    @Test
    fun `getEvent returns single event with verse IDs`() = runTest {
        val id = insertEvent("The Flood", -2350, -2349, "Creation", description = "Global flood")
        insertEventVerse(id, 1006001)
        insertEventVerse(id, 1007001)

        val event = repo.getEvent(id).getOrThrow()
        assertNotNull(event)
        assertEquals("The Flood", event.title)
        assertEquals(2, event.verseIds.size)
    }

    @Test
    fun `getEvent returns null for non-existent ID`() = runTest {
        val event = repo.getEvent(99999).getOrThrow()
        assertNull(event)
    }

    @Test
    fun `getEventsForVerse returns linked events`() = runTest {
        val id1 = insertEvent("Event A", -1000)
        val id2 = insertEvent("Event B", -900)
        insertEventVerse(id1, 1001001)
        insertEventVerse(id2, 1001001)

        val events = repo.getEventsForVerse(1001001).getOrThrow()
        assertEquals(2, events.size)
    }

    @Test
    fun `getEventsByCategory filters correctly`() = runTest {
        insertEvent("A", -1000, category = "Kingdom")
        insertEvent("B", -500, category = "Exile")
        insertEvent("C", -900, category = "Kingdom")

        val kingdom = repo.getEventsByCategory("Kingdom").getOrThrow()
        assertEquals(2, kingdom.size)
    }

    @Test
    fun `searchEvents finds event by title`() = runTest {
        insertEvent("Tower of Babel", -2200, description = "Language confusion")
        insertEvent("Abraham's Call", -2000)

        val results = repo.searchEvents("Babel").getOrThrow()
        assertEquals(1, results.size)
        assertEquals("Tower of Babel", results.first().title)
    }

    @Test
    fun `getEventCount returns correct count`() = runTest {
        insertEvent("A", -1000)
        insertEvent("B", -500)
        insertEvent("C", -100)

        val count = repo.getEventCount().getOrThrow()
        assertEquals(3, count)
    }

    @Test
    fun `getYearRange returns min and max years`() = runTest {
        insertEvent("A", -4000, -3900)
        insertEvent("B", 30, 33)

        val (minYear, maxYear) = repo.getYearRange().getOrThrow()
        assertEquals(-4000L, minYear)
        assertEquals(33L, maxYear)
    }
}
