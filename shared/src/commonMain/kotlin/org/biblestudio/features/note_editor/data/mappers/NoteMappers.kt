package org.biblestudio.features.note_editor.data.mappers

import migrations.Notes
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.note_editor.domain.entities.NoteFormat

internal fun Notes.toNote(): Note = Note(
    uuid = uuid,
    globalVerseId = global_verse_id,
    title = title,
    content = content,
    format = NoteFormat.fromString(format),
    createdAt = created_at,
    updatedAt = updated_at,
    deviceId = device_id
)
