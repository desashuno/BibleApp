package org.biblestudio.features.cross_references.domain.repositories

import org.biblestudio.features.cross_references.domain.entities.CrossReference

/**
 * Access to cross-reference data between Bible verses.
 */
interface CrossRefRepository {

    /** Returns cross-references originating from a verse. */
    suspend fun getRefsFromVerse(globalVerseId: Long): Result<List<CrossReference>>

    /** Returns cross-references targeting a verse. */
    suspend fun getRefsToVerse(globalVerseId: Long): Result<List<CrossReference>>

    /** Returns all cross-references for a verse (both directions). */
    suspend fun getAllForVerse(globalVerseId: Long): Result<List<CrossReference>>

    /**
     * Loads Treasury of Scripture Knowledge (TSK) data into the cross-reference table.
     * Returns the number of cross-references imported.
     */
    suspend fun loadTskData(): Result<Int>
}
