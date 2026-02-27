package org.biblestudio.features.bible_reader.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.bible_reader.domain.entities.VersionComparison
import org.biblestudio.features.bible_reader.domain.repositories.TextComparisonRepository

internal class TextComparisonRepositoryImpl(
    private val database: BibleStudioDatabase
) : TextComparisonRepository {

    override suspend fun getVersesForComparison(globalVerseId: Long): Result<VersionComparison> = runCatching {
        val bibles = database.bibleQueries.allBibles().executeAsList()
        val versions = mutableMapOf<String, String>()

        for (bible in bibles) {
            val verse = database.bibleQueries
                .verseByGlobalId(globalVerseId)
                .executeAsOneOrNull()

            if (verse != null) {
                versions[bible.abbreviation] = verse.text
            }
        }

        VersionComparison(
            globalVerseId = globalVerseId,
            versions = versions
        )
    }
}
