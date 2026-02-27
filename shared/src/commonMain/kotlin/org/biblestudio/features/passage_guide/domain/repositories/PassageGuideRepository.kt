package org.biblestudio.features.passage_guide.domain.repositories

import org.biblestudio.features.passage_guide.domain.entities.PassageReport

/**
 * Aggregates study data from multiple repositories to build a
 * comprehensive passage report for a single verse.
 */
interface PassageGuideRepository {

    /**
     * Builds a full passage report by querying all data sources in parallel:
     * verse text, cross-references, outlines, morphology words, key words,
     * commentary entries, and user notes.
     */
    suspend fun buildReport(globalVerseId: Long): Result<PassageReport>
}
