package org.biblestudio.features.sermon_editor.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.biblestudio.features.sermon_editor.domain.entities.Sermon
import org.biblestudio.features.sermon_editor.domain.entities.SermonSection

/**
 * CRUD operations for sermons and their sections.
 */
interface SermonRepository {

    /** Returns all sermons, excluding soft-deleted. */
    suspend fun getAll(): Result<List<Sermon>>

    /** Returns sermons filtered by workflow status. */
    suspend fun getByStatus(status: String): Result<List<Sermon>>

    /** Finds a single sermon by UUID. */
    suspend fun getByUuid(uuid: String): Result<Sermon?>

    /** Creates a new sermon. */
    suspend fun create(sermon: Sermon): Result<Unit>

    /** Updates an existing sermon. */
    suspend fun update(sermon: Sermon): Result<Unit>

    /** Soft-deletes a sermon by UUID. */
    suspend fun delete(uuid: String, deletedAt: String): Result<Unit>

    /** Returns all sections for a sermon, ordered by sort order. */
    suspend fun getSections(sermonId: String): Result<List<SermonSection>>

    /** Creates a new sermon section. */
    suspend fun createSection(section: SermonSection): Result<Unit>

    /** Updates an existing sermon section. */
    suspend fun updateSection(section: SermonSection): Result<Unit>

    /** Deletes a section by ID. */
    suspend fun deleteSection(sectionId: Long): Result<Unit>

    /** Deletes all sections for a sermon. */
    suspend fun deleteAllSections(sermonId: String): Result<Unit>

    /** Reactive stream of all sermons. */
    fun watchAll(): Flow<List<Sermon>>
}
