package org.biblestudio.features.note_editor.domain.entities

/**
 * Represents a user-created note attached to a verse.
 *
 * @param uuid Unique identifier (UUID v4).
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param title Note title.
 * @param content Rich text content of the note.
 * @param format Content format (plain, markdown, richtext).
 * @param createdAt ISO 8601 timestamp.
 * @param updatedAt ISO 8601 timestamp.
 * @param deviceId Identifier of the device that last modified this note.
 */
data class Note(
    val uuid: String,
    val globalVerseId: Long,
    val title: String,
    val content: String,
    val format: NoteFormat = NoteFormat.Markdown,
    val createdAt: String,
    val updatedAt: String,
    val deviceId: String
)
