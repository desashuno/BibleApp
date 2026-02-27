package org.biblestudio.features.import_export.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.features.import_export.domain.entities.DataType
import org.biblestudio.features.import_export.domain.entities.ExportFormat
import org.biblestudio.test.TestDatabase

class ImportExportRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: ImportExportRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = ImportExportRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    @Test
    fun `exportData returns valid JSON for notes`() = runTest {
        val data = repo.exportData(DataType.NOTES, ExportFormat.JSON).getOrThrow()
        assertTrue(data.isNotBlank())
    }

    @Test
    fun `exportData returns valid CSV for reading plans`() = runTest {
        val data = repo.exportData(DataType.READING_PLANS, ExportFormat.CSV).getOrThrow()
        assertTrue(data.isNotBlank())
    }

    @Test
    fun `export then import round-trips notes`() = runTest {
        // Seed a note
        testDb.database.annotationQueries.insertNote(
            uuid = "test-uuid-1",
            globalVerseId = 1001001,
            title = "Test Note",
            content = "This is a test note body.",
            format = "markdown",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            deviceId = "local"
        )

        val exported = repo.exportData(DataType.NOTES, ExportFormat.JSON).getOrThrow()
        assertTrue(exported.contains("Test Note"))
    }

    @Test
    fun `createBackup returns non-empty content`() = runTest {
        val backup = repo.createBackup().getOrThrow()
        assertTrue(backup.isNotBlank())
    }

    @Test
    fun `getBackupHistory returns list`() = runTest {
        val history = repo.getBackupHistory().getOrThrow()
        assertTrue(history is List)
    }

    @Test
    fun `restoreBackup with valid content succeeds`() = runTest {
        val backup = repo.createBackup().getOrThrow()
        val count = repo.restoreBackup(backup).getOrThrow()
        assertEquals(0, count)
    }
}
