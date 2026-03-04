package org.biblestudio.features.worship.audio

import android.media.MediaPlayer
import io.github.aakira.napier.Napier

/**
 * Android [AudioEngine] using [MediaPlayer].
 */
@Suppress("TooManyFunctions")
actual class AudioEngine actual constructor() {

    private var player: MediaPlayer? = null
    private var prepared = false

    actual fun prepare(filePath: String) {
        release()
        try {
            val mp = MediaPlayer()
            mp.setDataSource(filePath)
            mp.prepare()
            prepared = true
            player = mp
        } catch (
            @Suppress("TooGenericExceptionCaught")
            e: Exception
        ) {
            Napier.e("AudioEngine.prepare failed", e)
        }
    }

    actual fun play() {
        if (prepared) player?.start()
    }

    actual fun pause() {
        if (prepared) player?.pause()
    }

    actual fun stop() {
        if (prepared) {
            player?.stop()
            player?.prepare()
        }
    }

    actual fun seekTo(positionMs: Long) {
        if (prepared) player?.seekTo(positionMs.toInt())
    }

    actual fun setVolume(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        player?.setVolume(v, v)
    }

    actual val positionMs: Long
        get() = if (prepared) player?.currentPosition?.toLong() ?: 0L else 0L

    actual val durationMs: Long
        get() = if (prepared) player?.duration?.toLong() ?: 0L else 0L

    actual val isReady: Boolean
        get() = prepared

    actual val isPlaying: Boolean
        get() = prepared && (player?.isPlaying == true)

    actual fun release() {
        player?.release()
        player = null
        prepared = false
    }
}
