package org.biblestudio.features.word_study.domain.repositories

import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.features.word_study.domain.entities.DictionaryEntry

/**
 * Access to dictionary-typed resources and their per-verse entries.
 */
interface DictionaryRepository {

    /** Returns all dictionary resources. */
    suspend fun getDictionaries(): Result<List<Resource>>

    /** Returns dictionary entries for a specific resource and verse. */
    suspend fun getEntriesForVerse(resourceId: String, globalVerseId: Long): Result<List<DictionaryEntry>>

    /** Full-text search across dictionary entries. */
    suspend fun search(query: String, maxResults: Long = 100): Result<List<DictionaryEntry>>
}
