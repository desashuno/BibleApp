package org.biblestudio.features.audio_sync.component

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.util.componentScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.audio_sync.domain.repositories.AudioSyncRepository
import org.biblestudio.features.settings.domain.repositories.SettingsRepository

/**
 * Default [AudioSyncComponent] managing track loading, sync point lookup, and playback state.
 *
 * Actual platform audio playback is delegated to the UI layer; this component
 * manages logical state only (play/pause/seek/verse-jump).
 */
internal class DefaultAudioSyncComponent(
    componentContext: ComponentContext,
    private val repository: AudioSyncRepository,
    private val verseBus: VerseBus,
    private val settingsRepository: SettingsRepository
) : AudioSyncComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(AudioPlayerState())
    override val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

    private var currentBibleId: String = "KJV"

    init {
        loadDefaultBible()
        observeVerseBus()
    }

    private fun loadDefaultBible() {
        scope.launch {
            settingsRepository.getSetting("default_bible")
                .onSuccess { setting ->
                    setting?.value?.takeIf { it.isNotBlank() }?.let {
                        currentBibleId = it
                    }
                }
        }
    }

    override fun onPlay() {
        if (_state.value.track == null) return
        _state.update { it.copy(playbackState = PlaybackState.Playing) }
    }

    override fun onPause() {
        _state.update { it.copy(playbackState = PlaybackState.Paused) }
    }

    override fun onSeek(positionMs: Long) {
        _state.update { it.copy(positionMs = positionMs) }
        val trackId = _state.value.track?.id ?: return
        scope.launch {
            repository.getSyncPointAtTime(trackId, positionMs)
                .onSuccess { syncPoint ->
                    _state.update { it.copy(currentSyncPoint = syncPoint) }
                    syncPoint?.let {
                        verseBus.publish(LinkEvent.VerseSelected(it.globalVerseId.toInt()))
                    }
                }
        }
    }

    override fun onJumpToVerse(globalVerseId: Long) {
        val trackId = _state.value.track?.id ?: return
        scope.launch {
            repository.getSyncPointForVerse(trackId, globalVerseId)
                .onSuccess { syncPoint ->
                    syncPoint?.let { sp ->
                        _state.update {
                            it.copy(
                                positionMs = sp.startMs,
                                currentSyncPoint = sp,
                                playbackState = PlaybackState.Playing
                            )
                        }
                    }
                }
                .onFailure { e -> Napier.e("Failed to jump to verse", e) }
        }
    }

    override fun onStop() {
        _state.update {
            it.copy(
                playbackState = PlaybackState.Idle,
                positionMs = 0L,
                currentSyncPoint = null
            )
        }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { event ->
                    loadTrackForVerse(event.globalVerseId)
                }
        }
    }

    private fun loadTrackForVerse(globalVerseId: Int) {
        // Decode BBCCCVVV
        val book = globalVerseId / 1_000_000
        val chapter = (globalVerseId % 1_000_000) / 1_000

        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getTrackForChapter(currentBibleId, book, chapter)
                .onSuccess { track ->
                    if (track != null) {
                        repository.getSyncPoints(track.id)
                            .onSuccess { syncPoints ->
                                val currentSync = syncPoints.firstOrNull { sp ->
                                    sp.globalVerseId == globalVerseId.toLong()
                                }
                                _state.update {
                                    it.copy(
                                        track = track,
                                        syncPoints = syncPoints,
                                        currentSyncPoint = currentSync,
                                        durationMs = track.durationMs,
                                        positionMs = currentSync?.startMs ?: 0L,
                                        isLoading = false
                                    )
                                }
                            }
                            .onFailure { e ->
                                Napier.e("Failed to load sync points", e)
                                _state.update { it.copy(error = e.message, isLoading = false) }
                            }
                    } else {
                        _state.update {
                            it.copy(
                                track = null,
                                syncPoints = emptyList(),
                                isLoading = false
                            )
                        }
                    }
                }
                .onFailure { e ->
                    Napier.e("Failed to load audio track", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
