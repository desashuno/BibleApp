package org.biblestudio.features.word_study.data.mappers

import migrations.Dictionary_entries
import org.biblestudio.database.GetAllEntriesForVerseWithResource
import org.biblestudio.features.word_study.domain.entities.DictionaryEntry

internal fun Dictionary_entries.toDictionaryEntry(): DictionaryEntry = DictionaryEntry(
    id = id,
    resourceId = resource_id,
    headword = headword,
    content = content,
    relatedStrongs = related_strongs,
    sortOrder = sort_order
)

internal fun GetAllEntriesForVerseWithResource.toDictionaryEntryWithResource(): DictionaryEntry = DictionaryEntry(
    id = id,
    resourceId = resource_id,
    headword = headword,
    content = content,
    relatedStrongs = related_strongs,
    sortOrder = sort_order,
    resourceTitle = resource_title ?: "",
    resourceAuthor = resource_author ?: ""
)
