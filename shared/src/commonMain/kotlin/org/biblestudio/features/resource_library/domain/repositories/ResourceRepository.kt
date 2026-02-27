package org.biblestudio.features.resource_library.domain.repositories

import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry

/**
 * Access to imported resource metadata and per-verse entries.
 */
interface ResourceRepository {

    /** Returns all resources, excluding soft-deleted, ordered by title. */
    suspend fun getAllResources(): Result<List<Resource>>

    /** Returns resources filtered by [type] (e.g., "commentary", "dictionary"). */
    suspend fun getByType(type: String): Result<List<Resource>>

    /** Returns a single resource by UUID. */
    suspend fun getByUuid(uuid: String): Result<Resource?>

    /** Returns entries for a specific resource and verse. */
    suspend fun getEntriesForVerse(resourceId: String, globalVerseId: Long): Result<List<ResourceEntry>>

    /** Returns all entries for a resource, ordered by verse and sort order. */
    suspend fun getAllEntries(resourceId: String): Result<List<ResourceEntry>>

    /** Full-text search across resource entries. */
    suspend fun searchEntries(query: String, maxResults: Long = 100): Result<List<ResourceEntry>>
}
