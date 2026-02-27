package org.biblestudio.features.note_editor.domain.entities

/**
 * Supported note content formats.
 */
enum class NoteFormat(val dbValue: String) {
    PlainText("plain"),
    Markdown("markdown"),
    RichText("richtext");

    companion object {
        fun fromString(value: String): NoteFormat = entries.firstOrNull { it.dbValue.equals(value, ignoreCase = true) }
            ?: Markdown
    }
}
