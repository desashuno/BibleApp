package org.biblestudio.features.timeline.domain.entities

/**
 * Represents a historical event on the biblical timeline.
 *
 * @param id Database primary key.
 * @param title Short title of the event.
 * @param description Human-readable description.
 * @param startYear Approximate start year (negative = BC).
 * @param endYear Optional end year; null for point events.
 * @param category Historical era category.
 * @param importance Display importance (1 = normal, 2 = major, 3 = critical).
 * @param verseIds BBCCCVVV-encoded verse references linked to this event.
 */
data class TimelineEvent(
    val id: Long,
    val title: String,
    val description: String,
    val startYear: Int,
    val endYear: Int?,
    val category: TimelineCategory,
    val importance: Int = 1,
    val verseIds: List<Long> = emptyList()
)
