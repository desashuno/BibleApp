package org.biblestudio.features.note_editor.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.note_editor.domain.entities.NoteFormat
import org.biblestudio.features.note_editor.domain.repositories.NoteRepository

/**
 * Default [NoteEditorComponent] with auto-save debounce and FTS search.
 */
@Suppress("TooManyFunctions")
class DefaultNoteEditorComponent(
    componentContext: ComponentContext,
    private val repository: NoteRepository,
    private val verseBus: VerseBus
) : NoteEditorComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(NoteEditorState())
    override val state: StateFlow<NoteEditorState> = _state.asStateFlow()

    private var currentVerseId: Long? = null
    private var saveJob: Job? = null

    init {
        observeVerseBus()
    }

    override fun onNoteSelected(uuid: String) {
        scope.launch {
            repository.getNoteByUuid(uuid)
                .onSuccess { note ->
                    if (note != null) {
                        _state.update {
                            it.copy(
                                activeNote = note,
                                editTitle = note.title,
                                editContent = note.content,
                                format = note.format,
                                isDirty = false
                            )
                        }
                    }
                }
        }
    }

    override fun onTitleChanged(title: String) {
        _state.update { it.copy(editTitle = title, isDirty = true) }
        scheduleAutoSave()
    }

    override fun onContentChanged(content: String) {
        _state.update { it.copy(editContent = content, isDirty = true) }
        scheduleAutoSave()
    }

    override fun onFormatChanged(format: NoteFormat) {
        _state.update { it.copy(format = format, isDirty = true) }
        scheduleAutoSave()
    }

    override fun onNewNote() {
        val verseId = currentVerseId ?: return
        val now = Clock.System.now().toString()
        val uuid = generateUuid()
        val note = Note(
            uuid = uuid,
            globalVerseId = verseId,
            title = "",
            content = "",
            format = NoteFormat.Markdown,
            createdAt = now,
            updatedAt = now,
            deviceId = ""
        )
        scope.launch {
            repository.create(note)
                .onSuccess {
                    _state.update {
                        it.copy(
                            activeNote = note,
                            editTitle = "",
                            editContent = "",
                            format = NoteFormat.Markdown,
                            isDirty = false
                        )
                    }
                    loadNotesForVerse(verseId)
                }
        }
    }

    override fun onDeleteNote(uuid: String) {
        val now = Clock.System.now().toString()
        scope.launch {
            repository.delete(uuid, now)
                .onSuccess {
                    if (_state.value.activeNote?.uuid == uuid) {
                        _state.update { it.copy(activeNote = null, editTitle = "", editContent = "") }
                    }
                    currentVerseId?.let { loadNotesForVerse(it) }
                }
        }
    }

    override fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length >= 2) {
            scope.launch {
                repository.searchNotes(query)
                    .onSuccess { results ->
                        _state.update { it.copy(searchResults = results) }
                    }
            }
        } else {
            _state.update { it.copy(searchResults = emptyList()) }
        }
    }

    private fun scheduleAutoSave() {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(AUTO_SAVE_DELAY_MS)
            performSave()
        }
    }

    private suspend fun performSave() {
        val s = _state.value
        val note = s.activeNote ?: return
        if (!s.isDirty) return

        _state.update { it.copy(isSaving = true) }
        val now = Clock.System.now().toString()
        val updated = note.copy(
            title = s.editTitle,
            content = s.editContent,
            format = s.format,
            updatedAt = now
        )
        repository.update(updated)
            .onSuccess {
                _state.update { it.copy(activeNote = updated, isDirty = false, isSaving = false) }
                Napier.d("Note auto-saved: ${updated.uuid}")
            }
            .onFailure { e ->
                Napier.e("Auto-save failed", e)
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { event ->
                    currentVerseId = event.globalVerseId.toLong()
                    loadNotesForVerse(event.globalVerseId.toLong())
                }
        }
    }

    private fun loadNotesForVerse(verseId: Long) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getNotesForVerse(verseId)
                .onSuccess { notes ->
                    _state.update { it.copy(notes = notes, isLoading = false) }
                }
                .onFailure { e ->
                    Napier.e("Failed to load notes", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun generateUuid(): String {
        // Simple UUID v4 generation using random bytes
        val chars = "0123456789abcdef"
        val template = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx"
        return template.map { c ->
            when (c) {
                'x' -> chars.random()
                'y' -> chars["89ab".random().digitToInt(16)]
                else -> c
            }
        }.joinToString("")
    }

    companion object {
        internal const val AUTO_SAVE_DELAY_MS = 2000L
    }
}
