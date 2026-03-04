package org.biblestudio.features.worship.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.features.worship.WorshipPlayer
import org.biblestudio.features.worship.audio.AudioEngine
import org.biblestudio.features.worship.data.repositories.WorshipRepositoryImpl
import org.biblestudio.features.worship.domain.repositories.WorshipRepository
import org.biblestudio.test.TestDatabase

class DefaultWorshipComponentTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repository: WorshipRepository
    private lateinit var player: WorshipPlayer
    private lateinit var component: DefaultWorshipComponent

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repository = WorshipRepositoryImpl(testDb.database)
        player = WorshipPlayer(AudioEngine(), repository)
        val lifecycle = LifecycleRegistry()
        lifecycle.onCreate()
        lifecycle.onStart()
        lifecycle.onResume()
        component = DefaultWorshipComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            repository = repository,
            player = player
        )
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    @Test
    fun `initial state has Library tab`() {
        assertEquals(WorshipTab.Library, component.state.value.activeTab)
    }

    @Test
    fun `onTabSelected changes active tab`() {
        component.onTabSelected(WorshipTab.Favorites)
        assertEquals(WorshipTab.Favorites, component.state.value.activeTab)
    }

    @Test
    fun `onSearchQueryChanged updates search query`() {
        component.onSearchQueryChanged("grace")
        assertEquals("grace", component.state.value.searchQuery)
    }

    @Test
    fun `onCreatePlaylist adds playlist`() = runTest {
        component.onCreatePlaylist("Worship Set")
        kotlinx.coroutines.delay(300)
        component.onTabSelected(WorshipTab.Playlists)
        kotlinx.coroutines.delay(300)
        assertTrue(component.state.value.playlists.isNotEmpty())
    }

    @Test
    fun `onClearHistory clears history`() = runTest {
        component.onClearHistory()
        kotlinx.coroutines.delay(200)
        assertTrue(component.state.value.history.isEmpty())
    }
}
