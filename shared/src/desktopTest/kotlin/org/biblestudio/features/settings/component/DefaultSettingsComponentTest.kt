package org.biblestudio.features.settings.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import org.biblestudio.features.settings.data.repositories.SettingsRepositoryImpl
import org.biblestudio.test.TestDatabase

class DefaultSettingsComponentTest {

    private fun createComponent(): Pair<TestDatabase, DefaultSettingsComponent> {
        val testDb = TestDatabase()
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle = lifecycle)
        val repo = SettingsRepositoryImpl(testDb.database)
        val component = DefaultSettingsComponent(
            componentContext = context,
            repository = repo
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
