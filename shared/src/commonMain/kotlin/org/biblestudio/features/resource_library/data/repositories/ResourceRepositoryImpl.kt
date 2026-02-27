package org.biblestudio.features.resource_library.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.resource_library.data.mappers.toResource
import org.biblestudio.features.resource_library.data.mappers.toResourceEntry
import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry
import org.biblestudio.features.resource_library.domain.repositories.ResourceRepository

internal class ResourceRepositoryImpl(
    private val database: BibleStudioDatabase
) : ResourceRepository {

    override suspend fun getAllResources(): Result<List<Resource>> = runCatching {
        database.resourceQueries
            .allResources()
            .executeAsList()
            .map { it.toResource() }
    }

    override suspend fun getByType(type: String): Result<List<Resource>> = runCatching {
        database.resourceQueries
            .resourcesByType(type)
            .executeAsList()
            .map { it.toResource() }
    }

    override suspend fun getByUuid(uuid: String): Result<Resource?> = runCatching {
        database.resourceQueries
            .resourceByUuid(uuid)
            .executeAsOneOrNull()
            ?.toResource()
    }

    override suspend fun getEntriesForVerse(resourceId: String, globalVerseId: Long): Result<List<ResourceEntry>> =
        runCatching {
            database.resourceQueries
                .entriesForVerse(resourceId, globalVerseId)
                .executeAsList()
                .map { it.toResourceEntry() }
        }

    override suspend fun getAllEntries(resourceId: String): Result<List<ResourceEntry>> = runCatching {
        database.resourceQueries
            .allEntriesForResource(resourceId)
            .executeAsList()
            .map { it.toResourceEntry() }
    }

    override suspend fun searchEntries(query: String, maxResults: Long): Result<List<ResourceEntry>> = runCatching {
        database.resourceQueries
            .searchResources(query, maxResults)
            .executeAsList()
            .map { it.toResourceEntry() }
    }
}
