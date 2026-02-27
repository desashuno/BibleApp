package org.biblestudio.features.cross_references.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.cross_references.data.mappers.toParallelPassage
import org.biblestudio.features.cross_references.domain.entities.ParallelPassage
import org.biblestudio.features.cross_references.domain.repositories.ParallelRepository

internal class ParallelRepositoryImpl(
    private val database: BibleStudioDatabase
) : ParallelRepository {

    override suspend fun getForVerse(globalVerseId: Long): Result<List<ParallelPassage>> = runCatching {
        database.referenceQueries
            .parallelPassagesForVerse(globalVerseId)
            .executeAsList()
            .map { it.toParallelPassage() }
    }

    override suspend fun getByGroup(groupId: Long): Result<List<ParallelPassage>> = runCatching {
        database.referenceQueries
            .parallelPassagesByGroup(groupId)
            .executeAsList()
            .map { it.toParallelPassage() }
    }
}
