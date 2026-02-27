package org.biblestudio.features.word_study.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.resource_library.data.mappers.toResource
import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.features.word_study.data.mappers.toDictionaryEntry
import org.biblestudio.features.word_study.domain.entities.DictionaryEntry
import org.biblestudio.features.word_study.domain.repositories.DictionaryRepository

internal class DictionaryRepositoryImpl(
    private val database: BibleStudioDatabase
) : DictionaryRepository {

    override suspend fun getDictionaries(): Result<List<Resource>> = runCatching {
        database.resourceQueries
            .resourcesByType("dictionary")
            .executeAsList()
            .map { it.toResource() }
    }

    override suspend fun getEntriesForVerse(resourceId: String, globalVerseId: Long): Result<List<DictionaryEntry>> =
        runCatching {
            database.resourceQueries
                .entriesForVerse(resourceId, globalVerseId)
                .executeAsList()
                .map { it.toDictionaryEntry() }
        }

    override suspend fun search(query: String, maxResults: Long): Result<List<DictionaryEntry>> = runCatching {
        database.resourceQueries
            .searchResources(query, maxResults)
            .executeAsList()
            .map { it.toDictionaryEntry() }
    }
}
