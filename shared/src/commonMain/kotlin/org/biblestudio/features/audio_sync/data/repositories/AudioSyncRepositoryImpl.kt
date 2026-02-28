package org.biblestudio.features.audio_sync.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.audio_sync.data.mappers.toAudioSyncPoint
import org.biblestudio.features.audio_sync.data.mappers.toAudioTrack
import org.biblestudio.features.audio_sync.domain.entities.AudioSyncPoint
import org.biblestudio.features.audio_sync.domain.entities.AudioTrack
import org.biblestudio.features.audio_sync.domain.repositories.AudioSyncRepository

internal class AudioSyncRepositoryImpl(
    private val database: BibleStudioDatabase
) : AudioSyncRepository {

    override suspend fun getTrackForChapter(
        bibleId: String, bookNumber: Int, chapterNumber: Int
    ): Result<AudioTrack?> = runCatching {
        database.audioSyncQueries
            .getTrackForChapter(bibleId, bookNumber.toLong(), chapterNumber.toLong())
            .executeAsOneOrNull()
            ?.toAudioTrack()
    }

    override suspend fun getTracksForBook(bibleId: String, bookNumber: Int): Result<List<AudioTrack>> = runCatching {
        database.audioSyncQueries
            .getTracksForBook(bibleId, bookNumber.toLong())
            .executeAsList()
            .map { it.toAudioTrack() }
    }

    override suspend fun getTrack(trackId: Long): Result<AudioTrack?> = runCatching {
        database.audioSyncQueries
            .getTrackById(trackId)
            .executeAsOneOrNull()
            ?.toAudioTrack()
    }

    override suspend fun getSyncPoints(trackId: Long): Result<List<AudioSyncPoint>> = runCatching {
        database.audioSyncQueries
            .getSyncPointsForTrack(trackId)
            .executeAsList()
            .map { it.toAudioSyncPoint() }
    }

    override suspend fun getSyncPointForVerse(trackId: Long, globalVerseId: Long): Result<AudioSyncPoint?> = runCatching {
        database.audioSyncQueries
            .getSyncPointForVerse(trackId, globalVerseId)
            .executeAsOneOrNull()
            ?.toAudioSyncPoint()
    }

    override suspend fun getSyncPointAtTime(trackId: Long, positionMs: Long): Result<AudioSyncPoint?> = runCatching {
        database.audioSyncQueries
            .getSyncPointAtTime(trackId, positionMs)
            .executeAsOneOrNull()
            ?.toAudioSyncPoint()
    }

    override suspend fun getTrackCount(): Result<Long> = runCatching {
        database.audioSyncQueries
            .trackCount()
            .executeAsOne()
    }
}
