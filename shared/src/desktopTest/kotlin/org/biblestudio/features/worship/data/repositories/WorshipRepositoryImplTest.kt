package org.biblestudio.features.worship.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class WorshipRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: WorshipRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = WorshipRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun seedSong(title: String = "Amazing Grace", artist: String = "John Newton"): Long {
        testDb.database.worshipQueries.insertSong(
            title = title,
            artist = artist,
            album = "Classic Hymns",
            genre = "ClassicHymns",
            language = "en",
            durationMs = 240_000,
            filePath = "worship/amazing_grace.wav",
            coverArtPath = "",
            trackNumber = 1,
            year = 1779,
            isUserImport = 0
        )
        return testDb.database.worshipQueries.lastInsertSongId().executeAsOne()
    }

    @Test
    fun `getAllSongs returns inserted songs`() = runTest {
        seedSong()
        seedSong("How Great Thou Art", "Stuart Hine")
        val songs = repo.getAllSongs().getOrThrow()
        assertEquals(2, songs.size)
    }

    @Test
    fun `getSongById returns correct song`() = runTest {
        val id = seedSong()
        val song = repo.getSongById(id).getOrThrow()
        assertNotNull(song)
        assertEquals("Amazing Grace", song.title)
        assertEquals("John Newton", song.artist)
    }

    @Test
    fun `searchSongs finds by title`() = runTest {
        seedSong()
        seedSong("Be Thou My Vision", "Irish Hymn")
        val results = repo.searchSongs("Amazing").getOrThrow()
        assertEquals(1, results.size)
        assertEquals("Amazing Grace", results.first().title)
    }

    @Test
    fun `createPlaylist and getPlaylistWithSongs`() = runTest {
        val playlist = repo.createPlaylist("My Favorites").getOrThrow()
        assertNotNull(playlist)
        assertEquals("My Favorites", playlist.name)

        val songId = seedSong()
        repo.addSongToPlaylist(playlist.id, songId, 0).getOrThrow()

        val loaded = repo.getPlaylistWithSongs(playlist.id).getOrThrow()
        assertNotNull(loaded)
        assertEquals(1, loaded.songs.size)
        assertEquals("Amazing Grace", loaded.songs.first().title)
    }

    @Test
    fun `toggleFavorite adds and removes`() = runTest {
        val songId = seedSong()
        assertFalse(repo.isFavorite(songId).getOrThrow())

        val nowFav = repo.toggleFavorite(songId).getOrThrow()
        assertTrue(nowFav)
        assertTrue(repo.isFavorite(songId).getOrThrow())

        val removed = repo.toggleFavorite(songId).getOrThrow()
        assertFalse(removed)
        assertFalse(repo.isFavorite(songId).getOrThrow())
    }

    @Test
    fun `getFavorites returns favorited songs`() = runTest {
        val id1 = seedSong("Song A")
        seedSong("Song B")
        repo.toggleFavorite(id1).getOrThrow()

        val favs = repo.getFavorites().getOrThrow()
        assertEquals(1, favs.size)
        assertEquals("Song A", favs.first().title)
    }

    @Test
    fun `recordPlay and getRecentHistory`() = runTest {
        val id = seedSong()
        repo.recordPlay(id).getOrThrow()
        repo.recordPlay(id).getOrThrow()

        val history = repo.getRecentHistory().getOrThrow()
        assertEquals(2, history.size)
    }

    @Test
    fun `clearHistory empties play history`() = runTest {
        val id = seedSong()
        repo.recordPlay(id).getOrThrow()
        repo.clearHistory().getOrThrow()

        val history = repo.getRecentHistory().getOrThrow()
        assertTrue(history.isEmpty())
    }

    @Test
    fun `getLyricLines returns inserted lines`() = runTest {
        val songId = seedSong()
        testDb.database.worshipQueries.insertLyricLine(
            songId = songId,
            lineIndex = 0,
            startMs = 0,
            endMs = 4000,
            text = "Amazing grace, how sweet the sound"
        )
        testDb.database.worshipQueries.insertLyricLine(
            songId = songId,
            lineIndex = 1,
            startMs = 4000,
            endMs = 8000,
            text = "That saved a wretch like me"
        )

        val lines = repo.getLyricLines(songId).getOrThrow()
        assertEquals(2, lines.size)
        assertEquals("Amazing grace, how sweet the sound", lines.first().text)
    }

    @Test
    fun `getLyricLineAtTime returns correct line`() = runTest {
        val songId = seedSong()
        testDb.database.worshipQueries.insertLyricLine(
            songId = songId,
            lineIndex = 0,
            startMs = 0,
            endMs = 4000,
            text = "Line 1"
        )
        testDb.database.worshipQueries.insertLyricLine(
            songId = songId,
            lineIndex = 1,
            startMs = 4000,
            endMs = 8000,
            text = "Line 2"
        )

        val line = repo.getLyricLineAtTime(songId, 5000).getOrThrow()
        assertNotNull(line)
        assertEquals("Line 2", line.text)
    }

    @Test
    fun `getSongsForVerse returns linked songs`() = runTest {
        val songId = seedSong()
        testDb.database.worshipQueries.insertVerseLink(songId, 49003006)

        val songs = repo.getSongsForVerse(49003006).getOrThrow()
        assertEquals(1, songs.size)
        assertEquals("Amazing Grace", songs.first().title)
    }

    @Test
    fun `deletePlaylist removes playlist`() = runTest {
        val playlist = repo.createPlaylist("To Delete").getOrThrow()
        repo.deletePlaylist(playlist.id).getOrThrow()

        val all = repo.getAllPlaylists().getOrThrow()
        assertTrue(all.none { it.id == playlist.id })
    }
}
