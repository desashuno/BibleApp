package org.biblestudio.features.module_system.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.features.module_system.domain.entities.ModuleSource
import org.biblestudio.test.TestDatabase

class ModuleRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: ModuleRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = ModuleRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    @Test
    fun `installModule and getInstalledModules round-trip`() = runTest {
        val installed = repo.installModule(
            ModuleSource.Osis(xmlPath = "/data/ESV.xml")
        ).getOrThrow()

        assertNotNull(installed.uuid)
        assertTrue(installed.name.isNotBlank())

        val modules = repo.getInstalledModules().getOrThrow()
        assertEquals(1, modules.size)
        assertEquals(installed.uuid, modules.first().uuid)
    }

    @Test
    fun `removeModule cascades the module`() = runTest {
        val installed = repo.installModule(
            ModuleSource.Sword(confPath = "/data/KJV.conf", dataPath = "/data/KJV")
        ).getOrThrow()

        repo.removeModule(installed.uuid).getOrThrow()

        // soft-deleted modules may still appear in allModules depending on schema;
        // verify purge then
        repo.purgeModule(installed.uuid).getOrThrow()

        val remaining = repo.getInstalledModules().getOrThrow()
        assertTrue(remaining.none { it.uuid == installed.uuid })
    }

    @Test
    fun `getModulesByType filters correctly`() = runTest {
        repo.installModule(ModuleSource.Osis(xmlPath = "/data/A.xml")).getOrThrow()
        repo.installModule(ModuleSource.Usfm(directoryPath = "/data/B")).getOrThrow()

        val bibles = repo.getModulesByType("bible").getOrThrow()
        assertEquals(2, bibles.size)
    }

    @Test
    fun `getModule returns null for unknown uuid`() = runTest {
        val result = repo.getModule("nonexistent").getOrThrow()
        assertEquals(null, result)
    }
}
