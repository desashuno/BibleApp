package org.biblestudio.features.bible_reader.domain.repositories

import org.biblestudio.features.bible_reader.domain.entities.VersionComparison

/**
 * Compares verse text across multiple Bible versions.
 */
interface TextComparisonRepository {

    /**
     * Returns the text for a given [globalVerseId] from all available Bible versions.
     */
    suspend fun getVersesForComparison(globalVerseId: Long): Result<VersionComparison>
}
