package org.biblestudio.features.audio_sync.domain.repositories

import org.biblestudio.features.audio_sync.domain.entities.AudioSyncPoint
import org.biblestudio.features.audio_sync.domain.entities.AudioTrack

/**
 * Repository for querying audio tracks and their verse-level sync points.
 */
interface AudioSyncRepository {

    /** Returns the audio track for a specific chapter. */
    suspend fun getTrackForChapter(bibleId: String, bookNumber: Int, chapterNumber: Int): Result<AudioTrack?>

    /** Returns all tracks for a book. */
    suspend fun getTracksForBook(bibleId: String, bookNumber: Int): Result<List<AudioTrack>>

    /** Returns a single track by ID. */
    suspend fun getTrack(trackId: Long): Result<AudioTrack?>

    /** Returns all sync points for a track, ordered by start time. */
    suspend fun getSyncPoints(trackId: Long): Result<List<AudioSyncPoint>>

    /** Returns the sync point for a specific verse within a track. */
    suspend fun getSyncPointForVerse(trackId: Long, globalVerseId: Long): Result<AudioSyncPoint?>

    /** Returns the sync point active at a given playback position. */
    suspend fun getSyncPointAtTime(trackId: Long, positionMs: Long): Result<AudioSyncPoint?>

    /** Returns total track count. */
    suspend fun getTrackCount(): Result<Long>
}
