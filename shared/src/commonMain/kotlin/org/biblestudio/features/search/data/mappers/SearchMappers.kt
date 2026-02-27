package org.biblestudio.features.search.data.mappers

import migrations.Search_history
import org.biblestudio.features.search.domain.entities.SearchHistoryEntry

internal fun Search_history.toSearchHistoryEntry(): SearchHistoryEntry = SearchHistoryEntry(
    id = id,
    query = query,
    scope = scope,
    resultCount = result_count,
    createdAt = created_at
)
