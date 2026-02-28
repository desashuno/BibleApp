package org.biblestudio.features.audio_sync.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class AudioSyncRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: AudioSyncRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = AudioSyncRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun insertTrack(
        title: String = "Genesis 1",
        filePath: String = "/audio/gen1.mp3",
        bibleId: String = "KJV",
        bookNumber: Int = 1,
        chapterNumber: Int = 1,
        durationMs: Long = 180000,
        narrator: String? = "John",
        language: String = "en"
    ): Long {
        testDb.database.audioSyncQueries.insertTrack(
            title = title,
            filePath = filePath,
            bibleId = bibleId,
            bookNumber = bookNumber.toLong(),
            chapterNumber = chapterNumber.toLong(),
            durationMs = durationMs,
            narrator = narrator,
            language = language
        )
        return testDb.database.audioSyncQueries.lastInsertTrackId().executeAsOne()
    }

    private fun insertSyncPoint(trackId: Long, globalVerseId: Long, startMs: Long, endMs: Long) {
        testDb.database.audioSyncQueries.insertSyncPoint(
            trackId = trackId,
            globalVerseId = globalVerseId,
            startMs = startMs,
            endMs = endMs
        )
    }

    @Test
    fun `getTrackForChapter returns correct track`() = runTest {
        insertTrack()

        val track = repo.getTrackForChapter("KJV", 1, 1).getOrThrow()
        assertNotNull(track)
        assertEquals("Genesis 1", track.title)
        assertEquals(180000L, track.durationMs)
    }

    @Test
    fun `getTrackForChapter returns null when not found`() = runTest {
        val track = repo.getTrackForChapter("KJV", 99, 99).getOrThrow()
        assertNull(track)
    }

    @Test
    fun `getTracksForBook returns all chapter tracks`() = runTest {
        insertTrack(title = "Genesis 1", chapterNumber = 1)
        insertTrack(title = "Genesis 2", chapterNumber = 2)
        insertTrack(title = "Genesis 3", chapterNumber = 3)

        val tracks = repo.getTracksForBook("KJV", 1).getOrThrow()
        assertEquals(3, tracks.size)
    }

    @Test
    fun `getSyncPoints returns ordered sync points`() = runTest {
        val trackId = insertTrack()
        insertSyncPoint(trackId, 1001001, 0, 5000)
        insertSyncPoint(trackId, 1001002, 5000, 10000)
        insertSyncPoint(trackId, 1001003, 10000, 15000)

        val points = repo.getSyncPoints(trackId).getOrThrow()
        assertEquals(3, points.size)
        assertEquals(0L, points[0].startMs)
        assertEquals(5000L, points[1].startMs)
        assertEquals(10000L, points[2].startMs)
    }

    @Test
    fun `getSyncPointForVerse returns correct sync point`() = runTest {
        val trackId = insertTrack()
        insertSyncPoint(trackId, 1001001, 0, 5000)
        insertSyncPoint(trackId, 1001002, 5000, 10000)

        val sp = repo.getSyncPointForVerse(trackId, 1001002).getOrThrow()
        assertNotNull(sp)
        assertEquals(5000L, sp.startMs)
        assertEquals(10000L, sp.endMs)
    }

    @Test
    fun `getSyncPointAtTime returns correct sync point for position`() = runTest {
        val trackId = insertTrack()
        insertSyncPoint(trackId, 1001001, 0, 5000)
        insertSyncPoint(trackId, 1001002, 5000, 10000)
        insertSyncPoint(trackId, 1001003, 10000, 15000)

        val sp = repo.getSyncPointAtTime(trackId, 7500).getOrThrow()
        assertNotNull(sp)
        assertEquals(1001002, sp.globalVerseId)
    }

    @Test
    fun `getTrackCount returns correct count`() = runTest {
        insertTrack(title = "A", chapterNumber = 1)
        insertTrack(title = "B", chapterNumber = 2)

        val count = repo.getTrackCount().getOrThrow()
        assertEquals(2, count)
    }
}
