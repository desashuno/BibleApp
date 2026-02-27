package org.biblestudio.features.word_study.data.mappers

import migrations.Resource_entries
import org.biblestudio.features.word_study.domain.entities.DictionaryEntry

internal fun Resource_entries.toDictionaryEntry(): DictionaryEntry = DictionaryEntry(
    id = id,
    resourceId = resource_id,
    globalVerseId = global_verse_id,
    content = content,
    sortOrder = sort_order
)
