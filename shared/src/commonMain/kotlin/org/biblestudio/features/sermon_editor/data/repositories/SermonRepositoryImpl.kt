package org.biblestudio.features.sermon_editor.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.sermon_editor.data.mappers.toSermon
import org.biblestudio.features.sermon_editor.data.mappers.toSermonSection
import org.biblestudio.features.sermon_editor.domain.entities.Sermon
import org.biblestudio.features.sermon_editor.domain.entities.SermonSection
import org.biblestudio.features.sermon_editor.domain.repositories.SermonRepository

internal class SermonRepositoryImpl(
    private val database: BibleStudioDatabase
) : SermonRepository {

    override suspend fun getAll(): Result<List<Sermon>> = runCatching {
        database.writingQueries
            .allSermons()
            .executeAsList()
            .map { it.toSermon() }
    }

    override suspend fun getByStatus(status: String): Result<List<Sermon>> = runCatching {
        database.writingQueries
            .sermonsByStatus(status)
            .executeAsList()
            .map { it.toSermon() }
    }

    override suspend fun getByUuid(uuid: String): Result<Sermon?> = runCatching {
        database.writingQueries
            .sermonByUuid(uuid)
            .executeAsOneOrNull()
            ?.toSermon()
    }

    override suspend fun create(sermon: Sermon): Result<Unit> = runCatching {
        database.writingQueries.insertSermon(
            uuid = sermon.uuid,
            title = sermon.title,
            scriptureRef = sermon.scriptureRef,
            createdAt = sermon.createdAt,
            updatedAt = sermon.updatedAt,
            status = sermon.status,
            deviceId = sermon.deviceId
        )
    }

    override suspend fun update(sermon: Sermon): Result<Unit> = runCatching {
        database.writingQueries.updateSermon(
            uuid = sermon.uuid,
            title = sermon.title,
            scriptureRef = sermon.scriptureRef,
            updatedAt = sermon.updatedAt,
            status = sermon.status,
            deviceId = sermon.deviceId
        )
    }

    override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> = runCatching {
        database.writingQueries.softDeleteSermon(
            uuid = uuid,
            deletedAt = deletedAt
        )
    }

    override suspend fun getSections(sermonId: String): Result<List<SermonSection>> = runCatching {
        database.writingQueries
            .sectionsBySermon(sermonId)
            .executeAsList()
            .map { it.toSermonSection() }
    }

    override suspend fun createSection(section: SermonSection): Result<Unit> = runCatching {
        database.writingQueries.insertSection(
            sermonId = section.sermonId,
            type = section.type,
            content = section.content,
            sortOrder = section.sortOrder
        )
    }

    override suspend fun updateSection(section: SermonSection): Result<Unit> = runCatching {
        database.writingQueries.updateSection(
            id = section.id,
            type = section.type,
            content = section.content,
            sortOrder = section.sortOrder
        )
    }

    override suspend fun deleteSection(sectionId: Long): Result<Unit> = runCatching {
        database.writingQueries.deleteSection(sectionId)
    }

    override suspend fun deleteAllSections(sermonId: String): Result<Unit> = runCatching {
        database.writingQueries.deleteSectionsForSermon(sermonId)
    }

    override fun watchAll(): Flow<List<Sermon>> = database.writingQueries
        .allSermons()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { it.toSermon() } }
}
