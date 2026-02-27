package org.biblestudio.features.workspace.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.workspace.data.mappers.toWorkspace
import org.biblestudio.features.workspace.data.mappers.toWorkspaceLayout
import org.biblestudio.features.workspace.domain.entities.Workspace
import org.biblestudio.features.workspace.domain.entities.WorkspaceLayout
import org.biblestudio.features.workspace.domain.repositories.WorkspaceRepository

internal class WorkspaceRepositoryImpl(
    private val database: BibleStudioDatabase
) : WorkspaceRepository {

    override suspend fun getAll(): Result<List<Workspace>> = runCatching {
        database.settingsQueries
            .allWorkspaces()
            .executeAsList()
            .map { it.toWorkspace() }
    }

    override suspend fun getActive(): Result<Workspace?> = runCatching {
        database.settingsQueries
            .activeWorkspace()
            .executeAsOneOrNull()
            ?.toWorkspace()
    }

    override suspend fun getByUuid(uuid: String): Result<Workspace?> = runCatching {
        database.settingsQueries
            .workspaceByUuid(uuid)
            .executeAsOneOrNull()
            ?.toWorkspace()
    }

    override suspend fun create(workspace: Workspace): Result<Unit> = runCatching {
        database.settingsQueries.insertWorkspace(
            uuid = workspace.uuid,
            name = workspace.name,
            isActive = if (workspace.isActive) 1L else 0L,
            createdAt = workspace.createdAt,
            updatedAt = workspace.updatedAt,
            deviceId = workspace.deviceId
        )
    }

    override suspend fun update(workspace: Workspace): Result<Unit> = runCatching {
        database.settingsQueries.updateWorkspace(
            name = workspace.name,
            isActive = if (workspace.isActive) 1L else 0L,
            updatedAt = workspace.updatedAt,
            deviceId = workspace.deviceId,
            uuid = workspace.uuid
        )
    }

    override suspend fun setActive(uuid: String, updatedAt: String, deviceId: String): Result<Unit> = runCatching {
        database.transaction {
            database.settingsQueries.setActiveWorkspace()
            database.settingsQueries.updateWorkspace(
                name = null.toString(),
                isActive = 1L,
                updatedAt = updatedAt,
                deviceId = deviceId,
                uuid = uuid
            )
        }
    }

    override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> = runCatching {
        database.settingsQueries.softDeleteWorkspace(
            deletedAt = deletedAt,
            uuid = uuid
        )
    }

    override suspend fun getLayout(workspaceId: String): Result<WorkspaceLayout?> = runCatching {
        database.settingsQueries
            .layoutForWorkspace(workspaceId)
            .executeAsOneOrNull()
            ?.toWorkspaceLayout()
    }

    override suspend fun saveLayout(layout: WorkspaceLayout): Result<Unit> = runCatching {
        val existing = database.settingsQueries
            .layoutForWorkspace(layout.workspaceId)
            .executeAsOneOrNull()

        if (existing != null) {
            database.settingsQueries.updateLayout(
                layoutJson = layout.layoutJson,
                updatedAt = layout.updatedAt,
                id = existing.id
            )
        } else {
            database.settingsQueries.insertLayout(
                workspaceId = layout.workspaceId,
                layoutJson = layout.layoutJson,
                updatedAt = layout.updatedAt
            )
        }
    }

    override fun watchAll(): Flow<List<Workspace>> = database.settingsQueries
        .allWorkspaces()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { it.toWorkspace() } }
}
