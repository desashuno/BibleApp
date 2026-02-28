package org.biblestudio.features.timeline.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.timeline.data.mappers.toTimelineEvent
import org.biblestudio.features.timeline.domain.entities.TimelineEvent
import org.biblestudio.features.timeline.domain.repositories.TimelineRepository

internal class TimelineRepositoryImpl(
    private val database: BibleStudioDatabase
) : TimelineRepository {

    override suspend fun getEvents(startYear: Int, endYear: Int): Result<List<TimelineEvent>> = runCatching {
        database.timelineQueries
            .getEventsByRange(startYear.toLong(), endYear.toLong())
            .executeAsList()
            .map { row ->
                val verseIds = database.timelineQueries
                    .getVersesForEvent(row.id)
                    .executeAsList()
                row.toTimelineEvent(verseIds)
            }
    }

    override suspend fun getEvent(eventId: Long): Result<TimelineEvent?> = runCatching {
        val row = database.timelineQueries
            .getEventById(eventId)
            .executeAsOneOrNull() ?: return@runCatching null

        val verseIds = database.timelineQueries
            .getVersesForEvent(eventId)
            .executeAsList()

        row.toTimelineEvent(verseIds)
    }

    override suspend fun getEventsForVerse(globalVerseId: Long): Result<List<TimelineEvent>> = runCatching {
        database.timelineQueries
            .getEventsForVerse(globalVerseId)
            .executeAsList()
            .map { it.toTimelineEvent() }
    }

    override suspend fun getEventsByCategory(category: String): Result<List<TimelineEvent>> = runCatching {
        database.timelineQueries
            .getEventsByCategory(category)
            .executeAsList()
            .map { it.toTimelineEvent() }
    }

    override suspend fun searchEvents(query: String, maxResults: Long): Result<List<TimelineEvent>> = runCatching {
        database.timelineQueries
            .searchEvents(query = query, maxResults = maxResults)
            .executeAsList()
            .map { it.toTimelineEvent() }
    }

    override suspend fun getEventCount(): Result<Long> = runCatching {
        database.timelineQueries
            .eventCount()
            .executeAsOne()
    }

    override suspend fun getYearRange(): Result<Pair<Long?, Long?>> = runCatching {
        val row = database.timelineQueries
            .yearRange()
            .executeAsOne()
        Pair(row.min_year, row.max_year)
    }
}
