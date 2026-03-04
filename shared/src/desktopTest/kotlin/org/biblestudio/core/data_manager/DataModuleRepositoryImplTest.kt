package org.biblestudio.core.data_manager

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleStatus
import org.biblestudio.core.data_manager.model.DataModuleType
import org.biblestudio.test.TestDatabase

class DataModuleRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: DataModuleRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = DataModuleRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun sampleDescriptor(moduleId: String = "test-module-1", type: DataModuleType = DataModuleType.Bible) =
        DataModuleDescriptor(
            moduleId = moduleId,
            name = "Test Module",
            description = "A test module",
            type = type,
            version = "1.0",
            language = "en",
            sourceUrl = "https://example.com/module.zip",
            sizeBytes = 1024,
            status = DataModuleStatus.Available,
            checksum = "abc123"
        )

    @Test
    fun `insertModule and getAllModules round-trip`() = runTest {
        val descriptor = sampleDescriptor()
        repo.insertModule(descriptor).getOrThrow()

        val modules = repo.getAllModules().getOrThrow()
        assertEquals(1, modules.size)
        assertEquals("test-module-1", modules.first().moduleId)
        assertEquals("Test Module", modules.first().name)
        assertEquals(DataModuleType.Bible, modules.first().type)
    }

    @Test
    fun `getModuleById returns correct module`() = runTest {
        repo.insertModule(sampleDescriptor()).getOrThrow()

        val found = repo.getModuleById("test-module-1").getOrThrow()
        assertNotNull(found)
        assertEquals("Test Module", found.name)
    }

    @Test
    fun `getModuleById returns null for unknown id`() = runTest {
        val found = repo.getModuleById("nonexistent").getOrThrow()
        assertNull(found)
    }

    @Test
    fun `getModulesByType filters correctly`() = runTest {
        repo.insertModule(sampleDescriptor("bible-1", DataModuleType.Bible)).getOrThrow()
        repo.insertModule(sampleDescriptor("dict-1", DataModuleType.Dictionary)).getOrThrow()

        val bibles = repo.getModulesByType(DataModuleType.Bible).getOrThrow()
        assertEquals(1, bibles.size)
        assertEquals("bible-1", bibles.first().moduleId)

        val dicts = repo.getModulesByType(DataModuleType.Dictionary).getOrThrow()
        assertEquals(1, dicts.size)
        assertEquals("dict-1", dicts.first().moduleId)
    }

    @Test
    fun `updateStatus changes module status`() = runTest {
        repo.insertModule(sampleDescriptor()).getOrThrow()

        repo.updateStatus("test-module-1", DataModuleStatus.Installing).getOrThrow()

        val module = repo.getModuleById("test-module-1").getOrThrow()
        assertNotNull(module)
        assertEquals(DataModuleStatus.Installing, module.status)
    }

    @Test
    fun `markInstalled sets status and progress`() = runTest {
        repo.insertModule(sampleDescriptor()).getOrThrow()

        repo.markInstalled("test-module-1").getOrThrow()

        val module = repo.getModuleById("test-module-1").getOrThrow()
        assertNotNull(module)
        assertEquals(DataModuleStatus.Installed, module.status)
        assertEquals(1.0f, module.progress)
        assertNotNull(module.installedAt)
    }

    @Test
    fun `markRemoved resets status and progress`() = runTest {
        repo.insertModule(sampleDescriptor()).getOrThrow()
        repo.markInstalled("test-module-1").getOrThrow()

        repo.markRemoved("test-module-1").getOrThrow()

        val module = repo.getModuleById("test-module-1").getOrThrow()
        assertNotNull(module)
        assertEquals(DataModuleStatus.Available, module.status)
        assertEquals(0.0f, module.progress)
        assertNull(module.installedAt)
    }

    @Test
    fun `deleteModule removes the record`() = runTest {
        repo.insertModule(sampleDescriptor()).getOrThrow()

        repo.deleteModule("test-module-1").getOrThrow()

        val modules = repo.getAllModules().getOrThrow()
        assertTrue(modules.isEmpty())
    }

    @Test
    fun `getModulesByStatus filters correctly`() = runTest {
        repo.insertModule(sampleDescriptor("mod-1")).getOrThrow()
        repo.insertModule(sampleDescriptor("mod-2")).getOrThrow()
        repo.markInstalled("mod-1").getOrThrow()

        val installed = repo.getModulesByStatus(DataModuleStatus.Installed).getOrThrow()
        assertEquals(1, installed.size)
        assertEquals("mod-1", installed.first().moduleId)

        val available = repo.getModulesByStatus(DataModuleStatus.Available).getOrThrow()
        assertEquals(1, available.size)
        assertEquals("mod-2", available.first().moduleId)
    }

    @Test
    fun `updateProgress sets progress value`() = runTest {
        repo.insertModule(sampleDescriptor()).getOrThrow()

        repo.updateProgress("test-module-1", 0.5f).getOrThrow()

        val module = repo.getModuleById("test-module-1").getOrThrow()
        assertNotNull(module)
        assertEquals(0.5f, module.progress)
    }

    @Test
    fun `setModuleActive toggles is_active flag`() = runTest {
        repo.insertModule(sampleDescriptor()).getOrThrow()
        assertEquals(false, repo.getModuleById("test-module-1").getOrThrow()?.isActive)

        repo.setModuleActive("test-module-1", true).getOrThrow()
        assertEquals(true, repo.getModuleById("test-module-1").getOrThrow()?.isActive)

        repo.setModuleActive("test-module-1", false).getOrThrow()
        assertEquals(false, repo.getModuleById("test-module-1").getOrThrow()?.isActive)
    }

    @Test
    fun `getActiveModules returns only active modules`() = runTest {
        repo.insertModule(sampleDescriptor("mod-1")).getOrThrow()
        repo.insertModule(sampleDescriptor("mod-2")).getOrThrow()
        repo.setModuleActive("mod-1", true).getOrThrow()

        val active = repo.getActiveModules().getOrThrow()
        assertEquals(1, active.size)
        assertEquals("mod-1", active.first().moduleId)
    }

    @Test
    fun `getActiveModulesByType returns active modules of given type`() = runTest {
        repo.insertModule(sampleDescriptor("bible-1", DataModuleType.Bible)).getOrThrow()
        repo.insertModule(sampleDescriptor("dict-1", DataModuleType.Dictionary)).getOrThrow()
        repo.setModuleActive("bible-1", true).getOrThrow()
        repo.setModuleActive("dict-1", true).getOrThrow()

        val bibles = repo.getActiveModulesByType(DataModuleType.Bible).getOrThrow()
        assertEquals(1, bibles.size)
        assertEquals("bible-1", bibles.first().moduleId)
    }
}
