package org.biblestudio.features.word_study.data.repositories

import org.biblestudio.core.util.searchLexiconWithFallback
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.morphology_interlinear.data.mappers.toWordOccurrence
import org.biblestudio.core.study.WordOccurrence
import org.biblestudio.features.word_study.data.mappers.toLexiconEntry
import org.biblestudio.core.study.LexiconEntry
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

    override suspend fun getOccurrences(
        strongsNumber: String,
        limit: Long,
        offset: Long,
    ): Result<List<WordOccurrence>> = runCatching {
        database.studyQueries
            .occurrencesForWordPaged(strongsNumber, limit, offset)
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
        val entry = database.studyQueries
            .lexiconByStrongs(strongsNumber)
            .executeAsOneOrNull()
        val langPrefix = strongsNumber.take(1)

        if (entry != null && entry.transliteration.length >= TRANSLITERATION_PREFIX_LENGTH) {
            val transPrefix = entry.transliteration.take(TRANSLITERATION_PREFIX_LENGTH)
            database.studyQueries
                .relatedByTransliterationPrefix(langPrefix, transPrefix, strongsNumber, RELATED_MAX_RESULTS)
                .executeAsList()
                .map { it.toLexiconEntry() }
        } else {
            val prefix = strongsNumber.take(NUMERIC_PREFIX_LENGTH)
            database.studyQueries
                .relatedByPrefix(prefix, strongsNumber, RELATED_MAX_RESULTS)
                .executeAsList()
                .map { it.toLexiconEntry() }
        }
    }

    override suspend fun searchLexicon(query: String, maxResults: Long): Result<List<LexiconEntry>> = runCatching {
        searchLexiconWithFallback(database, query, maxResults)
    }

    companion object {
        private const val NUMERIC_PREFIX_LENGTH = 3
        private const val TRANSLITERATION_PREFIX_LENGTH = 3
        private const val RELATED_MAX_RESULTS = 20L
    }
}
