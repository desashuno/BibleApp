package org.biblestudio.features.cross_references.domain.repositories

import org.biblestudio.features.cross_references.domain.entities.ParallelPassage

/**
 * Access to parallel/synoptic passage groups.
 */
interface ParallelRepository {

    /** Returns parallel passages that include the given verse. */
    suspend fun getForVerse(globalVerseId: Long): Result<List<ParallelPassage>>

    /** Returns all passages within a specific parallel group. */
    suspend fun getByGroup(groupId: Long): Result<List<ParallelPassage>>
}
