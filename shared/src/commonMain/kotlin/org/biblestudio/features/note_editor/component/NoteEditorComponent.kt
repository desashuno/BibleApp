package org.biblestudio.features.note_editor.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.note_editor.domain.entities.NoteFormat

/**
 * Observable state for the Note Editor pane.
 */
data class NoteEditorState(
    val notes: List<Note> = emptyList(),
    val activeNote: Note? = null,
    val editTitle: String = "",
    val editContent: String = "",
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val format: NoteFormat = NoteFormat.Markdown,
    val searchQuery: String = "",
    val searchResults: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Business-logic boundary for the Note Editor pane.
 *
 * Manages CRUD, auto-save with debounce, and FTS search.
 */
interface NoteEditorComponent {

    /** The current note editor state observable. */
    val state: StateFlow<NoteEditorState>

    /** Selects a note for editing. */
    fun onNoteSelected(uuid: String)

    /** Called when note title text changes. */
    fun onTitleChanged(title: String)

    /** Called when note content text changes — triggers debounced auto-save. */
    fun onContentChanged(content: String)

    /** Changes the note format. */
    fun onFormatChanged(format: NoteFormat)

    /** Creates a new blank note for the current verse. */
    fun onNewNote()

    /** Deletes a note by UUID. */
    fun onDeleteNote(uuid: String)

    /** Updates the search query and triggers FTS search. */
    fun onSearchQueryChanged(query: String)
}
