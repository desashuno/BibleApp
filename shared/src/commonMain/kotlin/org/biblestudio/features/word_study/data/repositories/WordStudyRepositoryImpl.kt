package org.biblestudio.features.word_study.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.morphology_interlinear.data.mappers.toWordOccurrence
import org.biblestudio.features.morphology_interlinear.domain.entities.WordOccurrence
import org.biblestudio.features.word_study.data.mappers.toLexiconEntry
import org.biblestudio.features.word_study.domain.entities.LexiconEntry
import org.biblestudio.features.word_study.domain.repositories.WordStudyRepository

internal class WordStudyRepositoryImpl(
    private val database: BibleStudioDatabase
) : WordStudyRepository {

    override suspend fun lookupByStrongs(strongsNumber: String): Result<LexiconEntry?> = runCatching {
        database.studyQueries
            .lexiconByStrongs(strongsNumber)
            .executeAsOneOrNull()
            ?.toLexiconEntry()
    }

    override suspend fun getOccurrences(strongsNumber: String): Result<List<WordOccurrence>> = runCatching {
        database.studyQueries
            .occurrencesForWord(strongsNumber)
            .executeAsList()
            .map { it.toWordOccurrence() }
    }

    override suspend fun getOccurrenceCount(strongsNumber: String): Result<Long> = runCatching {
        database.studyQueries
            .occurrenceCount(strongsNumber)
            .executeAsOneOrNull()
            ?.count
            ?: 0L
    }

    override suspend fun getRelatedWords(strongsNumber: String): Result<List<LexiconEntry>> = runCatching {
        val prefix = strongsNumber.take(RELATED_PREFIX_LENGTH)
        database.studyQueries
            .relatedByPrefix(prefix, strongsNumber, RELATED_MAX_RESULTS)
            .executeAsList()
            .map { it.toLexiconEntry() }
    }

    override suspend fun searchLexicon(query: String, maxResults: Long): Result<List<LexiconEntry>> = runCatching {
        database.studyQueries
            .searchLexicon(query, maxResults)
            .executeAsList()
            .map { it.toLexiconEntry() }
    }

    companion object {
        private const val RELATED_PREFIX_LENGTH = 4
        private const val RELATED_MAX_RESULTS = 20L
    }
}
