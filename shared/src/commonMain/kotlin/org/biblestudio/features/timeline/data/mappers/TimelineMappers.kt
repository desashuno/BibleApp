package org.biblestudio.features.timeline.data.mappers

import migrations.Timeline_events
import org.biblestudio.features.timeline.domain.entities.TimelineCategory
import org.biblestudio.features.timeline.domain.entities.TimelineEvent

internal fun Timeline_events.toTimelineEvent(verseIds: List<Long> = emptyList()): TimelineEvent = TimelineEvent(
    id = id,
    title = title,
    description = description,
    startYear = start_year.toInt(),
    endYear = end_year?.toInt(),
    category = TimelineCategory.fromString(category),
    importance = importance.toInt(),
    verseIds = verseIds
)
