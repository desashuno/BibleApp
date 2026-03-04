package org.biblestudio.features.settings.component

import org.biblestudio.test.testComponentContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.biblestudio.features.settings.data.repositories.SettingsRepositoryImpl
import org.biblestudio.features.workspace.domain.entities.Workspace
import org.biblestudio.features.workspace.domain.entities.WorkspaceLayout
import org.biblestudio.features.workspace.domain.repositories.WorkspaceRepository
import org.biblestudio.test.TestDatabase

class DefaultSettingsComponentTest {

    private val fakeWorkspaceRepo = object : WorkspaceRepository {
        override suspend fun getAll(): Result<List<Workspace>> = Result.success(emptyList())
        override suspend fun getActive(): Result<Workspace?> = Result.success(null)
        override suspend fun getByUuid(uuid: String): Result<Workspace?> = Result.success(null)
        override suspend fun create(workspace: Workspace): Result<Unit> = Result.success(Unit)
        override suspend fun update(workspace: Workspace): Result<Unit> = Result.success(Unit)
        override suspend fun setActive(uuid: String, updatedAt: String, deviceId: String): Result<Unit> =
            Result.success(Unit)
        override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> = Result.success(Unit)
        override suspend fun getLayout(workspaceId: String): Result<WorkspaceLayout?> = Result.success(null)
        override suspend fun saveLayout(layout: WorkspaceLayout): Result<Unit> = Result.success(Unit)
        override fun watchAll(): Flow<List<Workspace>> = emptyFlow()
    }

    private fun createComponent(): Pair<TestDatabase, DefaultSettingsComponent> {
        val testDb = TestDatabase()
        val context = testComponentContext()
        val repo = SettingsRepositoryImpl(testDb.database)
        val component = DefaultSettingsComponent(
            componentContext = context,
            repository = repo,
            workspaceRepository = fakeWorkspaceRepo
        )
        return testDb to component
    }

    @Test
    fun setFontSizeUpdatesState() {
        val (testDb, component) = createComponent()
        try {
            component.setFontSize(20)
            assertEquals(20, component.state.value.fontSize)
        } finally {
            testDb.close()
        }
    }

    @Test
    fun setThemeUpdatesState() {
        val (testDb, component) = createComponent()
        try {
            component.setTheme(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, component.state.value.theme)
        } finally {
            testDb.close()
        }
    }

    @Test
    fun setDefaultBibleUpdatesState() {
        val (testDb, component) = createComponent()
        try {
            component.setDefaultBible("ASV")
            assertEquals("ASV", component.state.value.defaultBible)
        } finally {
            testDb.close()
        }
    }

    @Test
    fun defaultFontSizeIsCorrect() {
        val (testDb, component) = createComponent()
        try {
            assertEquals(SettingsState.DEFAULT_FONT_SIZE, component.state.value.fontSize)
        } finally {
            testDb.close()
        }
    }
}
