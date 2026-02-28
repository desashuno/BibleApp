package org.biblestudio.features.bible_reader.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.bible_reader.domain.entities.VersionComparison
import org.biblestudio.features.bible_reader.domain.repositories.TextComparisonRepository

internal class TextComparisonRepositoryImpl(
    private val database: BibleStudioDatabase
) : TextComparisonRepository {

    override suspend fun getVersesForComparison(globalVerseId: Long): Result<VersionComparison> = runCatching {
        val rows = database.bibleQueries
            .versesForComparisonByGlobalId(globalVerseId)
            .executeAsList()

        val versions = linkedMapOf<String, String>()
        for (row in rows) {
            versions[row.abbreviation] = row.text
        }

        VersionComparison(
            globalVerseId = globalVerseId,
            versions = versions
        )
    }
}
