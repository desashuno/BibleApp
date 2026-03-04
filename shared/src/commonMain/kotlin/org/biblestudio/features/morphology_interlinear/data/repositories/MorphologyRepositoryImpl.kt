package org.biblestudio.features.morphology_interlinear.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.morphology_interlinear.data.mappers.toAlignmentEntry
import org.biblestudio.features.morphology_interlinear.data.mappers.toMorphWord
import org.biblestudio.features.morphology_interlinear.data.mappers.toMorphologyData
import org.biblestudio.features.morphology_interlinear.data.mappers.toWordOccurrence
import org.biblestudio.features.morphology_interlinear.domain.entities.AlignmentEntry
import org.biblestudio.core.study.MorphWord
import org.biblestudio.features.morphology_interlinear.domain.entities.MorphologyData
import org.biblestudio.core.study.WordOccurrence
import org.biblestudio.features.morphology_interlinear.domain.repositories.MorphologyRepository

internal class MorphologyRepositoryImpl(
    private val database: BibleStudioDatabase
) : MorphologyRepository {

    private val studyQueries get() = database.studyQueries

    override suspend fun getMorphologyForVerse(globalVerseId: Long): Result<List<MorphologyData>> = runCatching {
        studyQueries
            .morphologyForVerse(globalVerseId)
            .executeAsList()
            .map { it.toMorphologyData() }
    }

    override suspend fun getMorphWords(globalVerseId: Long): Result<List<MorphWord>> = runCatching {
        studyQueries
            .morphologyForVerse(globalVerseId)
            .executeAsList()
            .map { it.toMorphWord() }
    }

    override suspend fun getWordsByStrongs(strongsNumber: String): Result<List<MorphWord>> = runCatching {
        studyQueries
            .morphologyByStrongs(strongsNumber)
            .executeAsList()
            .map { it.toMorphWord() }
    }

    override suspend fun getOccurrences(
        strongsNumber: String,
        limit: Long,
        offset: Long,
    ): Result<List<WordOccurrence>> = runCatching {
        studyQueries
            .occurrencesForWordPaged(strongsNumber, limit, offset)
            .executeAsList()
            .map { it.toWordOccurrence() }
    }

    override suspend fun getOccurrenceCount(strongsNumber: String): Result<Long> = runCatching {
        studyQueries
            .occurrenceCount(strongsNumber)
            .executeAsOneOrNull()
            ?.count ?: 0L
    }

    override suspend fun getAlignmentForVerse(globalVerseId: Long): Result<List<AlignmentEntry>> = runCatching {
        studyQueries
            .alignmentForVerse(globalVerseId)
            .executeAsList()
            .map { it.toAlignmentEntry() }
    }
}
