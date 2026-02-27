package org.biblestudio.features.resource_library.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class ResourceRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: ResourceRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = ResourceRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    @Test
    fun `getAll returns empty list when no resources exist`() = runTest {
        val result = repo.getAllResources().getOrThrow()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `searchEntries returns empty for unknown query`() = runTest {
        val result = repo.searchEntries("nonexistentquery", 10).getOrThrow()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAll returns result successfully`() = runTest {
        val result = repo.getAllResources()
        assertTrue(result.isSuccess)
    }
}
