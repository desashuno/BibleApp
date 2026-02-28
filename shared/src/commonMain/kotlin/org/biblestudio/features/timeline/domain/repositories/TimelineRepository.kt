package org.biblestudio.features.timeline.domain.repositories

import org.biblestudio.features.timeline.domain.entities.TimelineEvent

/**
 * Repository for querying the biblical timeline.
 */
interface TimelineRepository {

    /** Returns events whose start year falls within the given range. */
    suspend fun getEvents(startYear: Int, endYear: Int): Result<List<TimelineEvent>>

    /** Returns a single event by ID, or null if not found. */
    suspend fun getEvent(eventId: Long): Result<TimelineEvent?>

    /** Returns events linked to a specific verse. */
    suspend fun getEventsForVerse(globalVerseId: Long): Result<List<TimelineEvent>>

    /** Returns events filtered by category. */
    suspend fun getEventsByCategory(category: String): Result<List<TimelineEvent>>

    /** Full-text search on event title and description. */
    suspend fun searchEvents(query: String, maxResults: Long = 50): Result<List<TimelineEvent>>

    /** Returns total event count. */
    suspend fun getEventCount(): Result<Long>

    /** Returns the overall year range of all events. */
    suspend fun getYearRange(): Result<Pair<Long?, Long?>>
}
