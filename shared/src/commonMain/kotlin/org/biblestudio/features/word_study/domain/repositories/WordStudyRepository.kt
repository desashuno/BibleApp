package org.biblestudio.features.word_study.domain.repositories

import org.biblestudio.core.study.WordOccurrence
import org.biblestudio.core.study.LexiconEntry

/**
 * Look up and search Strong's concordance entries.
 */
interface WordStudyRepository {

    /** Finds a lexicon entry by Strong's number. */
    suspend fun lookupByStrongs(strongsNumber: String): Result<LexiconEntry?>

    /** Returns a paginated list of verse occurrences for a Strong's number. */
    suspend fun getOccurrences(
        strongsNumber: String,
        limit: Long = 100,
        offset: Long = 0,
    ): Result<List<WordOccurrence>>

    /** Returns the total count of occurrences for a Strong's number. */
    suspend fun getOccurrenceCount(strongsNumber: String): Result<Long>

    /** Returns semantically related words sharing the same root. */
    suspend fun getRelatedWords(strongsNumber: String): Result<List<LexiconEntry>>

    /** Searches lexicon entries by keyword across word, transliteration, and definition. */
    suspend fun searchLexicon(query: String, maxResults: Long = 50): Result<List<LexiconEntry>>
}
