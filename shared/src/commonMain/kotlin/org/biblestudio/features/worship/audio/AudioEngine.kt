package org.biblestudio.features.worship.audio

/**
 * Platform-specific audio playback engine.
 *
 * Desktop uses javax.sound, Android uses MediaPlayer, iOS uses AVAudioPlayer.
 * The engine manages a single audio stream and exposes position/duration
 * for the [WorshipPlayer] to poll.
 */
expect class AudioEngine() {

    /** Prepares a file for playback. */
    fun prepare(filePath: String)

    fun play()
    fun pause()
    fun stop()
    fun seekTo(positionMs: Long)
    fun setVolume(volume: Float)

    /** Current playback position in milliseconds. */
    val positionMs: Long

    /** Total duration of the loaded track in milliseconds. */
    val durationMs: Long

    /** Whether a track is currently loaded and ready. */
    val isReady: Boolean

    /** Whether audio is currently playing. */
    val isPlaying: Boolean

    /** Releases all resources held by this engine. */
    fun release()
}
