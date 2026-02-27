package org.biblestudio.features.bookmarks_history.data.mappers

import migrations.Navigation_history
import org.biblestudio.features.bookmarks_history.domain.entities.HistoryEntry

internal fun Navigation_history.toHistoryEntry(): HistoryEntry = HistoryEntry(
    id = id,
    globalVerseId = global_verse_id,
    visitedAt = visited_at
)
