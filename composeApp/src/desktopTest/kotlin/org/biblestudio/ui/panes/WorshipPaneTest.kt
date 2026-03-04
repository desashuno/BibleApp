package org.biblestudio.ui.panes

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import org.biblestudio.features.worship.component.WorshipState
import org.biblestudio.features.worship.component.WorshipTab
import org.biblestudio.features.worship.domain.entities.Song

@OptIn(ExperimentalTestApi::class)
class WorshipPaneTest {

    private val testSong = Song(
        id = 1,
        title = "Amazing Grace",
        artist = "John Newton",
        album = "Classic Hymns",
        genre = "ClassicHymns",
        language = "en",
        durationMs = 240_000,
        filePath = "test.wav",
        coverArtPath = "",
        trackNumber = 1,
        year = 1779,
        isUserImport = false
    )

    @Test
    fun `worship pane shows tabs`() = runComposeUiTest {
        val flow = MutableStateFlow(WorshipState())

        setContent {
            WorshipPane(
                stateFlow = flow,
                onTabSelected = {},
                onSearchQueryChanged = {},
                onSongSelected = {},
                onPlayAll = {},
                onToggleFavorite = {},
                onCreatePlaylist = {},
                onDeletePlaylist = {},
                onClearHistory = {}
            )
        }

        onNodeWithText("Library").assertIsDisplayed()
        onNodeWithText("Playlists").assertIsDisplayed()
        onNodeWithText("Favorites").assertIsDisplayed()
        onNodeWithText("Queue").assertIsDisplayed()
        onNodeWithText("History").assertIsDisplayed()
    }

    @Test
    fun `library tab shows song list`() = runComposeUiTest {
        val flow = MutableStateFlow(
            WorshipState(
                activeTab = WorshipTab.Library,
                songs = listOf(testSong)
            )
        )

        setContent {
            WorshipPane(
                stateFlow = flow,
                onTabSelected = {},
                onSearchQueryChanged = {},
                onSongSelected = {},
                onPlayAll = {},
                onToggleFavorite = {},
                onCreatePlaylist = {},
                onDeletePlaylist = {},
                onClearHistory = {}
            )
        }

        onNodeWithText("Amazing Grace").assertIsDisplayed()
        onNodeWithText("1 songs").assertIsDisplayed()
        onNodeWithText("Play All").assertIsDisplayed()
    }

    @Test
    fun `empty library shows search field`() = runComposeUiTest {
        val flow = MutableStateFlow(WorshipState())

        setContent {
            WorshipPane(
                stateFlow = flow,
                onTabSelected = {},
                onSearchQueryChanged = {},
                onSongSelected = {},
                onPlayAll = {},
                onToggleFavorite = {},
                onCreatePlaylist = {},
                onDeletePlaylist = {},
                onClearHistory = {}
            )
        }

        onNodeWithText("Search songs...", useUnmergedTree = true).assertIsDisplayed()
    }
}
