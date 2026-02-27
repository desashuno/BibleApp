package org.biblestudio.features.workspace.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.biblestudio.features.workspace.domain.entities.Workspace
import org.biblestudio.features.workspace.domain.entities.WorkspaceLayout

/**
 * CRUD operations for workspaces and their persisted layouts.
 */
interface WorkspaceRepository {

    /** Returns all workspaces, excluding soft-deleted. */
    suspend fun getAll(): Result<List<Workspace>>

    /** Returns the currently active workspace. */
    suspend fun getActive(): Result<Workspace?>

    /** Finds a workspace by UUID. */
    suspend fun getByUuid(uuid: String): Result<Workspace?>

    /** Creates a new workspace. */
    suspend fun create(workspace: Workspace): Result<Unit>

    /** Updates an existing workspace. */
    suspend fun update(workspace: Workspace): Result<Unit>

    /** Sets a workspace as the active one (deactivates all others first). */
    suspend fun setActive(uuid: String, updatedAt: String, deviceId: String): Result<Unit>

    /** Soft-deletes a workspace by UUID. */
    suspend fun delete(uuid: String, deletedAt: String): Result<Unit>

    /** Returns the most recent layout for a workspace. */
    suspend fun getLayout(workspaceId: String): Result<WorkspaceLayout?>

    /** Saves a layout snapshot for a workspace. */
    suspend fun saveLayout(layout: WorkspaceLayout): Result<Unit>

    /** Reactive stream of all workspaces. */
    fun watchAll(): Flow<List<Workspace>>
}
