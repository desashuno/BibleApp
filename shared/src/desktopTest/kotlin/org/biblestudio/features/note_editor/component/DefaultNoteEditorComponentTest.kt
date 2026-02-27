package org.biblestudio.features.note_editor.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.note_editor.domain.entities.NoteFormat
import org.biblestudio.features.note_editor.domain.repositories.NoteRepository

class DefaultNoteEditorComponentTest {

    private val storedNotes = mutableListOf<Note>()

    private val fakeRepo = object : NoteRepository {
        override suspend fun getNotesForVerse(globalVerseId: Long): Result<List<Note>> =
            Result.success(storedNotes.filter { it.globalVerseId == globalVerseId })

        override suspend fun getAllNotes(limit: Long, offset: Long): Result<List<Note>> =
            Result.success(storedNotes.take(limit.toInt()))

        override suspend fun getNoteByUuid(uuid: String): Result<Note?> =
            Result.success(storedNotes.find { it.uuid == uuid })

        override suspend fun create(note: Note): Result<Unit> {
            storedNotes.add(note)
            return Result.success(Unit)
        }

        override suspend fun update(note: Note): Result<Unit> {
            val idx = storedNotes.indexOfFirst { it.uuid == note.uuid }
            if (idx >= 0) storedNotes[idx] = note
            return Result.success(Unit)
        }

        override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> {
            storedNotes.removeAll { it.uuid == uuid }
            return Result.success(Unit)
        }

        override fun watchNotesForVerse(globalVerseId: Long): Flow<List<Note>> = emptyFlow()

        override suspend fun searchNotes(query: String, maxResults: Long): Result<List<Note>> =
            Result.success(storedNotes.filter { it.title.contains(query, ignoreCase = true) })
    }

    private fun createComponent(verseBus: VerseBus = VerseBus()): DefaultNoteEditorComponent {
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle = lifecycle)
        return DefaultNoteEditorComponent(
            componentContext = context,
            repository = fakeRepo,
            verseBus = verseBus
        )
    }

    @Test
    fun initialStateIsEmpty() {
        val component = createComponent()
        assertNull(component.state.value.activeNote)
        assertTrue(component.state.value.notes.isEmpty())
        assertFalse(component.state.value.isDirty)
    }

    @Test
    fun formatDefaultsToMarkdown() {
        val component = createComponent()
        assertEquals(NoteFormat.Markdown, component.state.value.format)
    }

    @Test
    fun titleChangedSetsDirty() = runTest {
        val component = createComponent()
        component.onTitleChanged("New Title")

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (!component.state.value.isDirty && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertTrue(component.state.value.isDirty)
        assertEquals("New Title", component.state.value.editTitle)
    }
}
