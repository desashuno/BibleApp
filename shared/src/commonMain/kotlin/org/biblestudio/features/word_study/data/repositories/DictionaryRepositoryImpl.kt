package org.biblestudio.features.word_study.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.resource_library.data.mappers.toResource
import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.features.word_study.data.mappers.toDictionaryEntry
import org.biblestudio.features.word_study.data.mappers.toDictionaryEntryWithResource
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

    override suspend fun getEntriesForVerse(globalVerseId: Long): Result<List<DictionaryEntry>> = runCatching {
        database.dictionaryQueries
            .getAllEntriesForVerseWithResource(globalVerseId)
            .executeAsList()
            .map { it.toDictionaryEntryWithResource() }
    }

    override suspend fun getByHeadword(resourceId: String, headword: String): Result<List<DictionaryEntry>> =
        runCatching {
            database.dictionaryQueries
                .getEntriesByHeadword(resourceId, headword)
                .executeAsList()
                .map { it.toDictionaryEntry() }
        }

    override suspend fun search(query: String, maxResults: Long): Result<List<DictionaryEntry>> = runCatching {
        database.dictionaryQueries
            .searchDictionary(query, maxResults)
            .executeAsList()
            .map { it.toDictionaryEntry() }
    }

    override suspend fun getEntryCount(): Result<Long> = runCatching {
        database.dictionaryQueries
            .entryCount()
            .executeAsOne()
    }
}
