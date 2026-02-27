package org.biblestudio.features.note_editor.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.test.TestDatabase

class NoteRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: NoteRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = NoteRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun sampleNote(uuid: String = "note-1", globalVerseId: Long = 1001L) = Note(
        uuid = uuid,
        globalVerseId = globalVerseId,
        title = "Test Note",
        content = "Some content here",
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z",
        deviceId = "device-1"
    )

    @Test
    fun `create and retrieve note by uuid`() = runTest {
        val note = sampleNote()
        repo.create(note).getOrThrow()

        val result = repo.getNoteByUuid("note-1").getOrThrow()
        assertNotNull(result)
        assertEquals("Test Note", result.title)
        assertEquals("Some content here", result.content)
        assertEquals(1001L, result.globalVerseId)
    }

    @Test
    fun `getNotesForVerse returns notes for specific verse`() = runTest {
        repo.create(sampleNote("n1", 100)).getOrThrow()
        repo.create(sampleNote("n2", 100)).getOrThrow()
        repo.create(sampleNote("n3", 200)).getOrThrow()

        val notes = repo.getNotesForVerse(100).getOrThrow()
        assertEquals(2, notes.size)
    }

    @Test
    fun `update note changes content`() = runTest {
        val note = sampleNote()
        repo.create(note).getOrThrow()

        val updated = note.copy(
            title = "Updated Title",
            content = "New content",
            updatedAt = "2025-06-01T00:00:00Z"
        )
        repo.update(updated).getOrThrow()

        val result = repo.getNoteByUuid("note-1").getOrThrow()
        assertNotNull(result)
        assertEquals("Updated Title", result.title)
        assertEquals("New content", result.content)
    }

    @Test
    fun `soft delete hides note from getAll`() = runTest {
        repo.create(sampleNote()).getOrThrow()

        val before = repo.getAllNotes(100, 0).getOrThrow()
        assertEquals(1, before.size)

        repo.delete("note-1", "2025-06-01T00:00:00Z").getOrThrow()

        val after = repo.getAllNotes(100, 0).getOrThrow()
        assertTrue(after.isEmpty())
    }

    @Test
    fun `getNoteByUuid returns null for non-existent uuid`() = runTest {
        val result = repo.getNoteByUuid("no-such-note").getOrThrow()
        assertNull(result)
    }
}
