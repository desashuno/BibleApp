package org.biblestudio.features.worship

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.features.worship.audio.AudioEngine
import org.biblestudio.features.worship.domain.entities.LyricLine
import org.biblestudio.features.worship.domain.entities.RepeatMode
import org.biblestudio.features.worship.domain.entities.Song
import org.biblestudio.features.worship.domain.repositories.WorshipRepository

/**
 * Playback state exposed by [WorshipPlayer].
 */
data class WorshipPlayerState(
    val currentSong: Song? = null,
    val playbackState: PlaybackStatus = PlaybackStatus.Idle,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val volume: Float = 1.0f,
    val repeatMode: RepeatMode = RepeatMode.Off,
    val isShuffleOn: Boolean = false,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val currentLyricLine: LyricLine? = null,
    val error: String? = null
)

enum class PlaybackStatus {
    Idle,
    Playing,
    Paused
}

/**
 * Global singleton managing worship music playback.
 *
 * Wraps [AudioEngine] + [WorshipRepository]. Not per-pane — shared
 * across the entire application so music continues playing as the user
 * navigates between panes and workspaces.
 */
@Suppress("TooManyFunctions")
class WorshipPlayer(
    private val engine: AudioEngine,
    private val repository: WorshipRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(WorshipPlayerState())
    val state: StateFlow<WorshipPlayerState> = _state.asStateFlow()

    private var positionJob: Job? = null

    /**
     * Starts playback of [song] with an optional [queue].
     * If [queue] is empty, plays the single song.
     */
    fun play(song: Song, queue: List<Song> = emptyList()) {
        val effectiveQueue = queue.ifEmpty { listOf(song) }
        val index = effectiveQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        _state.update {
            it.copy(
                queue = effectiveQueue,
                queueIndex = index,
                error = null
            )
        }
        loadAndPlay(song)
    }

    fun resume() {
        if (_state.value.currentSong == null) return
        engine.play()
        _state.update { it.copy(playbackState = PlaybackStatus.Playing) }
        startPositionTracking()
    }

    fun pause() {
        engine.pause()
        _state.update { it.copy(playbackState = PlaybackStatus.Paused) }
        stopPositionTracking()
    }

    fun stop() {
        engine.stop()
        stopPositionTracking()
        _state.update {
            it.copy(
                playbackState = PlaybackStatus.Idle,
                positionMs = 0,
                currentLyricLine = null
            )
        }
    }

    fun skipNext() {
        val s = _state.value
        if (s.queue.isEmpty()) return
        val nextIndex = when (s.repeatMode) {
            RepeatMode.One -> s.queueIndex
            RepeatMode.All -> (s.queueIndex + 1) % s.queue.size
            RepeatMode.Off -> {
                val next = s.queueIndex + 1
                if (next >= s.queue.size) return
                next
            }
        }
        val nextSong = if (s.isShuffleOn) {
            s.queue.random()
        } else {
            s.queue.getOrNull(nextIndex) ?: return
        }
        _state.update { it.copy(queueIndex = nextIndex) }
        loadAndPlay(nextSong)
    }

    fun skipPrevious() {
        val s = _state.value
        if (s.queue.isEmpty()) return
        // If past 3 seconds, restart current track
        if (s.positionMs > RESTART_THRESHOLD_MS) {
            seekTo(0)
            return
        }
        val prevIndex = (s.queueIndex - 1).coerceAtLeast(0)
        val prevSong = s.queue.getOrNull(prevIndex) ?: return
        _state.update { it.copy(queueIndex = prevIndex) }
        loadAndPlay(prevSong)
    }

    fun seekTo(positionMs: Long) {
        engine.seekTo(positionMs)
        _state.update { it.copy(positionMs = positionMs) }
    }

    fun setVolume(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        engine.setVolume(v)
        _state.update { it.copy(volume = v) }
    }

    fun toggleRepeatMode() {
        _state.update {
            val next = when (it.repeatMode) {
                RepeatMode.Off -> RepeatMode.All
                RepeatMode.All -> RepeatMode.One
                RepeatMode.One -> RepeatMode.Off
            }
            it.copy(repeatMode = next)
        }
    }

    fun toggleShuffle() {
        _state.update { it.copy(isShuffleOn = !it.isShuffleOn) }
    }

    private fun loadAndPlay(song: Song) {
        stopPositionTracking()
        try {
            engine.prepare(song.filePath)
            engine.play()
            _state.update {
                it.copy(
                    currentSong = song,
                    playbackState = PlaybackStatus.Playing,
                    positionMs = 0,
                    durationMs = engine.durationMs,
                    currentLyricLine = null,
                    error = null
                )
            }
            startPositionTracking()
            recordPlayHistory(song.id)
        } catch (
            @Suppress("TooGenericExceptionCaught")
            e: Exception
        ) {
            Napier.e("Failed to play song: ${song.title}", e)
            _state.update {
                it.copy(
                    playbackState = PlaybackStatus.Idle,
                    error = e.message
                )
            }
        }
    }

    private fun startPositionTracking() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (true) {
                delay(POSITION_POLL_MS)
                if (!engine.isPlaying) {
                    // Track ended naturally
                    if (_state.value.playbackState == PlaybackStatus.Playing && engine.isReady) {
                        onTrackEnded()
                    }
                    break
                }
                val pos = engine.positionMs
                _state.update { it.copy(positionMs = pos) }
                updateLyricLine(pos)
            }
        }
    }

    private fun stopPositionTracking() {
        positionJob?.cancel()
        positionJob = null
    }

    private fun onTrackEnded() {
        val s = _state.value
        when (s.repeatMode) {
            RepeatMode.One -> {
                seekTo(0)
                resume()
            }
            RepeatMode.All -> skipNext()
            RepeatMode.Off -> {
                if (s.queueIndex < s.queue.size - 1) {
                    skipNext()
                } else {
                    stop()
                }
            }
        }
    }

    private fun updateLyricLine(positionMs: Long) {
        val songId = _state.value.currentSong?.id ?: return
        scope.launch {
            repository.getLyricLineAtTime(songId, positionMs)
                .onSuccess { line ->
                    if (line != _state.value.currentLyricLine) {
                        _state.update { it.copy(currentLyricLine = line) }
                    }
                }
        }
    }

    private fun recordPlayHistory(songId: Long) {
        scope.launch {
            repository.recordPlay(songId)
                .onFailure { e -> Napier.e("Failed to record play history", e) }
        }
    }

    companion object {
        private const val POSITION_POLL_MS = 250L
        private const val RESTART_THRESHOLD_MS = 3000L
    }
}
