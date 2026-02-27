package org.biblestudio.features.note_editor.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.biblestudio.features.note_editor.domain.entities.Note

/**
 * CRUD operations for user notes.
 */
interface NoteRepository {

    /** Returns notes for a specific verse, excluding soft-deleted. */
    suspend fun getNotesForVerse(globalVerseId: Long): Result<List<Note>>

    /** Returns a paginated list of all notes. */
    suspend fun getAllNotes(limit: Long, offset: Long): Result<List<Note>>

    /** Finds a single note by its UUID. */
    suspend fun getNoteByUuid(uuid: String): Result<Note?>

    /** Creates a new note. */
    suspend fun create(note: Note): Result<Unit>

    /** Updates an existing note. */
    suspend fun update(note: Note): Result<Unit>

    /** Soft-deletes a note by UUID. */
    suspend fun delete(uuid: String, deletedAt: String): Result<Unit>

    /** Reactive stream of notes for a specific verse. */
    fun watchNotesForVerse(globalVerseId: Long): Flow<List<Note>>

    /** Full-text search across all notes. */
    suspend fun searchNotes(query: String, maxResults: Long = 50): Result<List<Note>>
}
