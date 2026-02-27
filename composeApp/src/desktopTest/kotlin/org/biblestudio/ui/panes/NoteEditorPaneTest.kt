package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.note_editor.component.NoteEditorState
import org.biblestudio.features.note_editor.domain.entities.Note

@OptIn(ExperimentalTestApi::class)
class NoteEditorPaneTest {

    private val testNote = Note(
        uuid = "note-1",
        globalVerseId = 1_001_001,
        title = "Creation Note",
        content = "In the beginning God created...",
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z",
        deviceId = "dev-1"
    )

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun NoteEditorPane_rendersNoteList() = runComposeUiTest {
        val flow = MutableStateFlow(
            NoteEditorState(
                notes = listOf(testNote),
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            NoteEditorPane(
                stateFlow = flow,
                onNoteSelected = {},
                onTitleChanged = {},
                onContentChanged = {},
                onNewNote = {},
                onDeleteNote = {},
                onSearchQueryChanged = {}
            )
        }

        onNodeWithText("Notes").assertIsDisplayed()
        onNodeWithText("Creation Note").assertIsDisplayed()
    }

    @Test
    @Suppress("ktlint:standard:function-naming")
    fun NoteEditorPane_showsEditorWhenNoteActive() = runComposeUiTest {
        val flow = MutableStateFlow(
            NoteEditorState(
                notes = listOf(testNote),
                activeNote = testNote,
                editTitle = "Creation Note",
                editContent = "In the beginning God created...",
                isLoading = false
            )
        )

        setContent {
            @Suppress("ktlint:standard:function-naming")
            NoteEditorPane(
                stateFlow = flow,
                onNoteSelected = {},
                onTitleChanged = {},
                onContentChanged = {},
                onNewNote = {},
                onDeleteNote = {},
                onSearchQueryChanged = {}
            )
        }

        onNodeWithText("Select or create a note").assertDoesNotExist()
    }
}
