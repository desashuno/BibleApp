package org.biblestudio.features.resource_library.component

import org.biblestudio.test.testComponentContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.data_manager.DataManager
import org.biblestudio.core.data_manager.DataModuleHandler
import org.biblestudio.core.data_manager.model.DataManagerState
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleStatus
import org.biblestudio.core.data_manager.model.DataModuleType

class DefaultResourceLibraryComponentTest {

    private val testModule = DataModuleDescriptor(
        moduleId = "bible-kjv",
        name = "King James Version",
        description = "KJV Bible",
        type = DataModuleType.Bible,
        status = DataModuleStatus.Installed,
        language = "en"
    )

    private val testModule2 = DataModuleDescriptor(
        moduleId = "dict-easton",
        name = "Easton's Bible Dictionary",
        description = "Public domain dictionary",
        type = DataModuleType.Dictionary,
        status = DataModuleStatus.Available,
        language = "en"
    )

    private class FakeDataManager(
        initialModules: List<DataModuleDescriptor> = emptyList()
    ) : DataManager {
        private val _state = MutableStateFlow(
            DataManagerState(modules = initialModules)
        )
        override val state: StateFlow<DataManagerState> = _state

        var lastInstalledId: String? = null
        var lastRemovedId: String? = null
        var lastCancelledId: String? = null
        var lastToggledId: String? = null
        var lastToggledActive: Boolean? = null

        override suspend fun loadModules() {}

        override suspend fun installModule(moduleId: String) {
            lastInstalledId = moduleId
        }

        override suspend fun removeModule(moduleId: String) {
            lastRemovedId = moduleId
        }

        override fun cancelDownload(moduleId: String) {
            lastCancelledId = moduleId
        }

        override fun registerHandler(handler: DataModuleHandler) {}

        override fun getModulesByType(type: DataModuleType): List<DataModuleDescriptor> =
            _state.value.modules.filter { it.type == type }

        override suspend fun setModuleActive(moduleId: String, isActive: Boolean) {
            lastToggledId = moduleId
            lastToggledActive = isActive
            _state.value = _state.value.copy(
                modules = _state.value.modules.map {
                    if (it.moduleId == moduleId) it.copy(isActive = isActive) else it
                }
            )
        }

        override fun getActiveModulesByType(type: DataModuleType): List<DataModuleDescriptor> =
            _state.value.modules.filter { it.type == type && it.isActive }
    }

    private fun createComponent(
        dataManager: DataManager = FakeDataManager(listOf(testModule, testModule2))
    ): DefaultResourceLibraryComponent {
        val context = testComponentContext()
        return DefaultResourceLibraryComponent(
            componentContext = context,
            dataManager = dataManager
        )
    }

    @Test
    fun `initial state has no selected module`() {
        val component = createComponent()
        assertNull(component.state.value.selectedModule)
    }

    @Test
    fun `modules load from DataManager`() = runTest {
        val component = createComponent()

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.filteredModules.isEmpty() &&
            System.currentTimeMillis() - start < timeout
        ) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertEquals(2, component.state.value.modules.size)
        assertEquals(2, component.state.value.filteredModules.size)
    }

    @Test
    fun `onFilterTypeChanged filters modules`() = runTest {
        val component = createComponent()

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.modules.isEmpty() &&
            System.currentTimeMillis() - start < timeout
        ) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        component.onFilterTypeChanged(DataModuleType.Bible)
        assertEquals(DataModuleType.Bible, component.state.value.filterType)
        assertEquals(1, component.state.value.filteredModules.size)
        assertEquals("bible-kjv", component.state.value.filteredModules.first().moduleId)
    }

    @Test
    fun `onSearchQueryChanged filters by name`() = runTest {
        val component = createComponent()

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.modules.isEmpty() &&
            System.currentTimeMillis() - start < timeout
        ) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        component.onSearchQueryChanged("Easton")
        assertEquals("Easton", component.state.value.searchQuery)
        assertEquals(1, component.state.value.filteredModules.size)
        assertEquals("dict-easton", component.state.value.filteredModules.first().moduleId)
    }

    @Test
    fun `onModuleSelected sets selectedModule`() = runTest {
        val component = createComponent()

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.filteredModules.isEmpty() &&
            System.currentTimeMillis() - start < timeout
        ) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        component.onModuleSelected("bible-kjv")
        assertEquals("bible-kjv", component.state.value.selectedModule?.moduleId)
    }

    @Test
    fun `onInstallModule delegates to DataManager`() = runTest {
        val fakeManager = FakeDataManager(listOf(testModule))
        val component = createComponent(fakeManager)

        component.onInstallModule("bible-kjv")

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (fakeManager.lastInstalledId == null &&
            System.currentTimeMillis() - start < timeout
        ) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertEquals("bible-kjv", fakeManager.lastInstalledId)
    }

    @Test
    fun `onCancelDownload delegates to DataManager`() {
        val fakeManager = FakeDataManager(listOf(testModule))
        val component = createComponent(fakeManager)

        component.onCancelDownload("bible-kjv")
        assertEquals("bible-kjv", fakeManager.lastCancelledId)
    }

    @Test
    fun `onToggleModuleActive toggles active state`() = runTest {
        val activeModule = testModule.copy(isActive = true)
        val fakeManager = FakeDataManager(listOf(activeModule))
        val component = createComponent(fakeManager)

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.modules.isEmpty() &&
            System.currentTimeMillis() - start < timeout
        ) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        component.onToggleModuleActive("bible-kjv")

        val start2 = System.currentTimeMillis()
        while (fakeManager.lastToggledId == null &&
            System.currentTimeMillis() - start2 < timeout
        ) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertEquals("bible-kjv", fakeManager.lastToggledId)
        assertEquals(false, fakeManager.lastToggledActive)
    }
}
