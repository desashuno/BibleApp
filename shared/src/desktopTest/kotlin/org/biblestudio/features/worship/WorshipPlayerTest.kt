package org.biblestudio.features.worship

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.biblestudio.features.worship.audio.AudioEngine
import org.biblestudio.features.worship.data.repositories.WorshipRepositoryImpl
import org.biblestudio.features.worship.domain.entities.RepeatMode
import org.biblestudio.features.worship.domain.entities.Song
import org.biblestudio.test.TestDatabase

class WorshipPlayerTest {

    private lateinit var testDb: TestDatabase
    private lateinit var player: WorshipPlayer

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        val repository = WorshipRepositoryImpl(testDb.database)
        player = WorshipPlayer(AudioEngine(), repository)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun testSong(id: Long = 1, title: String = "Test Song") = Song(
        id = id,
        title = title,
        artist = "Test Artist",
        album = "Test Album",
        genre = "Worship",
        language = "en",
        durationMs = 180_000,
        filePath = "nonexistent.wav",
        coverArtPath = "",
        trackNumber = 1,
        year = 2024,
        isUserImport = false
    )

    @Test
    fun `initial state is idle with no song`() {
        val state = player.state.value
        assertEquals(PlaybackStatus.Idle, state.playbackState)
        assertNull(state.currentSong)
    }

    @Test
    fun `toggleRepeatMode cycles through modes`() {
        assertEquals(RepeatMode.Off, player.state.value.repeatMode)
        player.toggleRepeatMode()
        assertEquals(RepeatMode.All, player.state.value.repeatMode)
        player.toggleRepeatMode()
        assertEquals(RepeatMode.One, player.state.value.repeatMode)
        player.toggleRepeatMode()
        assertEquals(RepeatMode.Off, player.state.value.repeatMode)
    }

    @Test
    fun `toggleShuffle toggles shuffle state`() {
        assertEquals(false, player.state.value.isShuffleOn)
        player.toggleShuffle()
        assertEquals(true, player.state.value.isShuffleOn)
        player.toggleShuffle()
        assertEquals(false, player.state.value.isShuffleOn)
    }

    @Test
    fun `setVolume clamps between 0 and 1`() {
        player.setVolume(0.5f)
        assertEquals(0.5f, player.state.value.volume)
        player.setVolume(2.0f)
        assertEquals(1.0f, player.state.value.volume)
        player.setVolume(-1.0f)
        assertEquals(0.0f, player.state.value.volume)
    }

    @Test
    fun `play sets queue and current song`() {
        val songs = listOf(testSong(1, "Song A"), testSong(2, "Song B"))
        // Play will fail to load file but should still update state
        player.play(songs[0], songs)
        val state = player.state.value
        assertEquals(2, state.queue.size)
        assertEquals(0, state.queueIndex)
    }

    @Test
    fun `stop resets position`() {
        player.stop()
        assertEquals(0L, player.state.value.positionMs)
        assertEquals(PlaybackStatus.Idle, player.state.value.playbackState)
    }

    @Test
    fun `pause without song is safe`() {
        player.pause()
        assertEquals(PlaybackStatus.Paused, player.state.value.playbackState)
    }
}
