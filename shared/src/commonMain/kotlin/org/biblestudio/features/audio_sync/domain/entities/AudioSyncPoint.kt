package org.biblestudio.features.audio_sync.domain.entities

/**
 * Maps a verse to a time range within an audio track.
 *
 * @param id Database primary key.
 * @param trackId Foreign key to [AudioTrack].
 * @param globalVerseId BBCCCVVV-encoded verse identifier.
 * @param startMs Start time in milliseconds within the track.
 * @param endMs End time in milliseconds within the track.
 */
data class AudioSyncPoint(
    val id: Long,
    val trackId: Long,
    val globalVerseId: Long,
    val startMs: Long,
    val endMs: Long
)
