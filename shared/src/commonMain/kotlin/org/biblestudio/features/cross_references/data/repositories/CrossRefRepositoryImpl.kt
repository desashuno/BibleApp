package org.biblestudio.features.cross_references.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.cross_references.data.mappers.toCrossReference
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.features.cross_references.domain.repositories.CrossRefRepository

internal class CrossRefRepositoryImpl(
    private val database: BibleStudioDatabase
) : CrossRefRepository {

    override suspend fun getRefsFromVerse(globalVerseId: Long): Result<List<CrossReference>> = runCatching {
        database.referenceQueries
            .crossRefsFromVerse(globalVerseId)
            .executeAsList()
            .map { it.toCrossReference() }
    }

    override suspend fun getRefsToVerse(globalVerseId: Long): Result<List<CrossReference>> = runCatching {
        database.referenceQueries
            .crossRefsToVerse(globalVerseId)
            .executeAsList()
            .map { it.toCrossReference() }
    }

    override suspend fun getAllForVerse(globalVerseId: Long): Result<List<CrossReference>> = runCatching {
        database.referenceQueries
            .allCrossRefsForVerse(globalVerseId)
            .executeAsList()
            .map { it.toCrossReference() }
    }

    override suspend fun loadTskData(): Result<Int> = runCatching {
        // TODO: Parse bundled TSK dataset and insert into cross_references table
        0
    }
}
