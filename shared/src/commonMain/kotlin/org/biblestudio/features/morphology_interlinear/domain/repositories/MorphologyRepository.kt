package org.biblestudio.features.morphology_interlinear.domain.repositories

import org.biblestudio.features.morphology_interlinear.domain.entities.AlignmentEntry
import org.biblestudio.core.study.MorphWord
import org.biblestudio.features.morphology_interlinear.domain.entities.MorphologyData
import org.biblestudio.core.study.WordOccurrence

/**
 * Provides morphological analysis and word occurrence data for verses.
 */
interface MorphologyRepository {

    /** Returns morphology data for every word in a verse, enriched with lexicon data. */
    suspend fun getMorphologyForVerse(globalVerseId: Long): Result<List<MorphologyData>>

    /** Returns rich morph-word data for every word in a verse. */
    suspend fun getMorphWords(globalVerseId: Long): Result<List<MorphWord>>

    /** Returns all morph-word entries for a given Strong's number across all verses. */
    suspend fun getWordsByStrongs(strongsNumber: String): Result<List<MorphWord>>

    /** Returns a paginated list of occurrences for a word identified by its Strong's number. */
    suspend fun getOccurrences(
        strongsNumber: String,
        limit: Long = 100,
        offset: Long = 0,
    ): Result<List<WordOccurrence>>

    /** Returns the total occurrence count for a Strong's number. */
    suspend fun getOccurrenceCount(strongsNumber: String): Result<Long>

    /** Returns pre-computed English↔Strong's alignment data for a verse. */
    suspend fun getAlignmentForVerse(globalVerseId: Long): Result<List<AlignmentEntry>>
}
