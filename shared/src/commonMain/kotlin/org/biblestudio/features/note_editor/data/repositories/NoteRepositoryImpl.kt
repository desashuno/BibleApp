package org.biblestudio.features.note_editor.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.note_editor.data.mappers.toNote
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.note_editor.domain.repositories.NoteRepository

internal class NoteRepositoryImpl(
    private val database: BibleStudioDatabase
) : NoteRepository {

    override suspend fun getNotesForVerse(globalVerseId: Long): Result<List<Note>> = runCatching {
        database.annotationQueries
            .notesByVerse(globalVerseId)
            .executeAsList()
            .map { it.toNote() }
    }

    override suspend fun getAllNotes(limit: Long, offset: Long): Result<List<Note>> = runCatching {
        database.annotationQueries
            .allNotes(limit, offset)
            .executeAsList()
            .map { it.toNote() }
    }

    override suspend fun getNoteByUuid(uuid: String): Result<Note?> = runCatching {
        database.annotationQueries
            .noteByUuid(uuid)
            .executeAsOneOrNull()
            ?.toNote()
    }

    override suspend fun create(note: Note): Result<Unit> = runCatching {
        database.annotationQueries.insertNote(
            uuid = note.uuid,
            globalVerseId = note.globalVerseId,
            title = note.title,
            content = note.content,
            format = note.format.dbValue,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            deviceId = note.deviceId
        )
    }

    override suspend fun update(note: Note): Result<Unit> = runCatching {
        database.annotationQueries.updateNote(
            uuid = note.uuid,
            title = note.title,
            content = note.content,
            format = note.format.dbValue,
            updatedAt = note.updatedAt,
            deviceId = note.deviceId
        )
    }

    override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> = runCatching {
        database.annotationQueries.softDeleteNote(
            uuid = uuid,
            deletedAt = deletedAt
        )
    }

    override fun watchNotesForVerse(globalVerseId: Long): Flow<List<Note>> = database.annotationQueries
        .notesByVerse(globalVerseId)
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows -> rows.map { it.toNote() } }

    override suspend fun searchNotes(query: String, maxResults: Long): Result<List<Note>> = runCatching {
        database.annotationQueries
            .searchNotesFts(query, maxResults)
            .executeAsList()
            .map { it.toNote() }
    }
}
