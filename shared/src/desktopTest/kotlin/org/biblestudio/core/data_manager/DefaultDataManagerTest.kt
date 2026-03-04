package org.biblestudio.core.data_manager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleStatus
import org.biblestudio.core.data_manager.model.DataModuleType

class DefaultDataManagerTest {

    private fun sampleDescriptor(
        moduleId: String = "test-1",
        type: DataModuleType = DataModuleType.Bible,
        status: DataModuleStatus = DataModuleStatus.Available
    ) = DataModuleDescriptor(
        moduleId = moduleId,
        name = "Test Module",
        type = type,
        status = status
    )

    /** In-memory fake repository for testing DataManager logic. */
    private class FakeDataModuleRepository : DataModuleRepository {
        val modules = mutableMapOf<String, DataModuleDescriptor>()

        override suspend fun getAllModules(): Result<List<DataModuleDescriptor>> =
            Result.success(modules.values.toList())

        override suspend fun getModulesByType(type: DataModuleType): Result<List<DataModuleDescriptor>> =
            Result.success(modules.values.filter { it.type == type })

        override suspend fun getModulesByStatus(status: DataModuleStatus): Result<List<DataModuleDescriptor>> =
            Result.success(modules.values.filter { it.status == status })

        override suspend fun getModuleById(moduleId: String): Result<DataModuleDescriptor?> =
            Result.success(modules[moduleId])

        override suspend fun insertModule(descriptor: DataModuleDescriptor): Result<Unit> {
            modules[descriptor.moduleId] = descriptor
            return Result.success(Unit)
        }

        override suspend fun updateStatus(moduleId: String, status: DataModuleStatus): Result<Unit> {
            modules[moduleId] = modules[moduleId]!!.copy(status = status)
            return Result.success(Unit)
        }

        override suspend fun updateProgress(moduleId: String, progress: Float): Result<Unit> {
            modules[moduleId] = modules[moduleId]!!.copy(progress = progress)
            return Result.success(Unit)
        }

        override suspend fun markInstalled(moduleId: String): Result<Unit> {
            modules[moduleId] = modules[moduleId]!!.copy(
                status = DataModuleStatus.Installed,
                progress = 1f
            )
            return Result.success(Unit)
        }

        override suspend fun markRemoved(moduleId: String): Result<Unit> {
            modules[moduleId] = modules[moduleId]!!.copy(
                status = DataModuleStatus.Available,
                progress = 0f
            )
            return Result.success(Unit)
        }

        override suspend fun deleteModule(moduleId: String): Result<Unit> {
            modules.remove(moduleId)
            return Result.success(Unit)
        }

        override suspend fun getActiveModules(): Result<List<DataModuleDescriptor>> =
            Result.success(modules.values.filter { it.isActive })

        override suspend fun getActiveModulesByType(type: DataModuleType): Result<List<DataModuleDescriptor>> =
            Result.success(modules.values.filter { it.type == type && it.isActive })

        override suspend fun setModuleActive(moduleId: String, isActive: Boolean): Result<Unit> {
            modules[moduleId] = modules[moduleId]!!.copy(isActive = isActive)
            return Result.success(Unit)
        }

        override suspend fun autoPopulateFromExistingData(): Result<Int> = Result.success(0)
    }

    /** Fake handler that tracks calls. */
    private class FakeHandler(
        override val supportedType: DataModuleType = DataModuleType.Bible
    ) : DataModuleHandler {
        var installCalled = false
        var removeCalled = false
        var validateCalled = false

        override suspend fun install(
            descriptor: DataModuleDescriptor,
            progressCallback: (Float) -> Unit
        ): Result<Unit> {
            installCalled = true
            progressCallback(0.5f)
            progressCallback(1.0f)
            return Result.success(Unit)
        }

        override suspend fun remove(descriptor: DataModuleDescriptor): Result<Unit> {
            removeCalled = true
            return Result.success(Unit)
        }

        override suspend fun validate(descriptor: DataModuleDescriptor): Result<Boolean> {
            validateCalled = true
            return Result.success(true)
        }
    }

    @Test
    fun `loadModules populates state`() = runTest {
        val repo = FakeDataModuleRepository()
        repo.modules["test-1"] = sampleDescriptor()
        val dm = DefaultDataManager(repo)

        // Wait for init to complete
        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (dm.state.value.modules.isEmpty() && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertEquals(1, dm.state.value.modules.size)
        assertEquals("test-1", dm.state.value.modules.first().moduleId)
    }

    @Test
    fun `installModule calls handler and marks installed`() = runTest {
        val repo = FakeDataModuleRepository()
        repo.modules["test-1"] = sampleDescriptor()
        val handler = FakeHandler()
        val dm = DefaultDataManager(repo)
        dm.registerHandler(handler)

        // Wait for init
        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (dm.state.value.modules.isEmpty() && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        dm.installModule("test-1")

        // Wait for install to complete (handler called + status updated)
        val start2 = System.currentTimeMillis()
        while (repo.modules["test-1"]?.status != DataModuleStatus.Installed &&
            System.currentTimeMillis() - start2 < timeout
        ) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertTrue(handler.installCalled)
        assertEquals(DataModuleStatus.Installed, repo.modules["test-1"]?.status)
    }

    @Test
    fun `removeModule calls handler and marks removed`() = runTest {
        val repo = FakeDataModuleRepository()
        repo.modules["test-1"] = sampleDescriptor(status = DataModuleStatus.Installed)
        val handler = FakeHandler()
        val dm = DefaultDataManager(repo)
        dm.registerHandler(handler)

        // Wait for init
        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (dm.state.value.modules.isEmpty() && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        dm.removeModule("test-1")

        // Wait for remove to complete
        val start2 = System.currentTimeMillis()
        while (!handler.removeCalled && System.currentTimeMillis() - start2 < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertTrue(handler.removeCalled)
        assertEquals(DataModuleStatus.Available, repo.modules["test-1"]?.status)
    }

    @Test
    fun `getModulesByType filters from state`() = runTest {
        val repo = FakeDataModuleRepository()
        repo.modules["bible-1"] = sampleDescriptor("bible-1", DataModuleType.Bible)
        repo.modules["dict-1"] = sampleDescriptor("dict-1", DataModuleType.Dictionary)
        val dm = DefaultDataManager(repo)

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (dm.state.value.modules.size < 2 && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        val bibles = dm.getModulesByType(DataModuleType.Bible)
        assertEquals(1, bibles.size)
        assertEquals("bible-1", bibles.first().moduleId)
    }

    @Test
    fun `installModule with unknown moduleId sets error`() = runTest {
        val repo = FakeDataModuleRepository()
        val dm = DefaultDataManager(repo)

        dm.installModule("nonexistent")

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (dm.state.value.error == null && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertTrue(dm.state.value.error?.contains("not found") == true)
    }
}
