package org.biblestudio.features.word_study.domain.repositories

import org.biblestudio.features.resource_library.domain.entities.Resource
import org.biblestudio.features.word_study.domain.entities.DictionaryEntry

/**
 * Access to dictionary-typed resources and their topic-based entries.
 */
interface DictionaryRepository {

    /** Returns all dictionary resources. */
    suspend fun getDictionaries(): Result<List<Resource>>

    /** Returns dictionary entries linked to a specific verse (from all dictionaries). */
    suspend fun getEntriesForVerse(globalVerseId: Long): Result<List<DictionaryEntry>>

    /** Returns dictionary entries by headword within a specific dictionary. */
    suspend fun getByHeadword(resourceId: String, headword: String): Result<List<DictionaryEntry>>

    /** Full-text search across all dictionary entries. */
    suspend fun search(query: String, maxResults: Long = 100): Result<List<DictionaryEntry>>

    /** Returns total entry count across all dictionaries. */
    suspend fun getEntryCount(): Result<Long>
}
