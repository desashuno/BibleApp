package org.biblestudio.features.settings.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.biblestudio.features.settings.domain.entities.AppSetting
import org.biblestudio.test.TestDatabase

class SettingsRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: SettingsRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = SettingsRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun setting(
        key: String = "theme",
        value: String = "dark",
        type: String = "string",
        category: String = "appearance"
    ) = AppSetting(key = key, value = value, type = type, category = category)

    @Test
    fun `set and get setting`() = runTest {
        repo.setSetting(setting()).getOrThrow()

        val result = repo.getSetting("theme").getOrThrow()
        assertNotNull(result)
        assertEquals("dark", result.value)
        assertEquals("appearance", result.category)
    }

    @Test
    fun `getAll returns all stored settings`() = runTest {
        repo.setSetting(setting("key1", "val1")).getOrThrow()
        repo.setSetting(setting("key2", "val2")).getOrThrow()

        val all = repo.getAll().getOrThrow()
        assertEquals(2, all.size)
    }

    @Test
    fun `getByCategory filters correctly`() = runTest {
        repo.setSetting(setting("a", "1", category = "appearance")).getOrThrow()
        repo.setSetting(setting("b", "2", category = "sync")).getOrThrow()
        repo.setSetting(setting("c", "3", category = "appearance")).getOrThrow()

        val appearance = repo.getByCategory("appearance").getOrThrow()
        assertEquals(2, appearance.size)
    }

    @Test
    fun `delete removes setting`() = runTest {
        repo.setSetting(setting()).getOrThrow()
        repo.delete("theme").getOrThrow()

        val result = repo.getSetting("theme").getOrThrow()
        assertNull(result)
    }

    @Test
    fun `setSetting upserts on conflict`() = runTest {
        repo.setSetting(setting("theme", "dark")).getOrThrow()
        repo.setSetting(setting("theme", "light")).getOrThrow()

        val all = repo.getAll().getOrThrow()
        assertEquals(1, all.size)
        assertEquals("light", all.first().value)
    }
}
