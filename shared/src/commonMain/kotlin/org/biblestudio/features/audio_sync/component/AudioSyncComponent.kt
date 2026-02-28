package org.biblestudio.features.audio_sync.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.audio_sync.domain.entities.AudioSyncPoint
import org.biblestudio.features.audio_sync.domain.entities.AudioTrack

/**
 * Playback state for the audio player.
 */
enum class PlaybackState { Idle, Playing, Paused }

/**
 * Observable state for the Audio Sync pane.
 */
data class AudioPlayerState(
    val track: AudioTrack? = null,
    val syncPoints: List<AudioSyncPoint> = emptyList(),
    val currentSyncPoint: AudioSyncPoint? = null,
    val playbackState: PlaybackState = PlaybackState.Idle,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Business-logic boundary for the Audio Sync pane.
 *
 * Subscribes to [LinkEvent.VerseSelected] to auto-load a chapter audio track
 * and seek to the selected verse's sync point.
 */
interface AudioSyncComponent {

    val state: StateFlow<AudioPlayerState>

    /** Play or resume playback. */
    fun onPlay()

    /** Pause playback. */
    fun onPause()

    /** Seek to a position in milliseconds. */
    fun onSeek(positionMs: Long)

    /** Jump to a specific verse sync point. */
    fun onJumpToVerse(globalVerseId: Long)

    /** Stop playback and reset. */
    fun onStop()
}
