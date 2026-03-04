package org.biblestudio.features.worship.audio

import io.github.aakira.napier.Napier
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL

/**
 * iOS [AudioEngine] using [AVAudioPlayer].
 */
@Suppress("TooManyFunctions")
actual class AudioEngine actual constructor() {

    private var player: AVAudioPlayer? = null

    actual fun prepare(filePath: String) {
        release()
        val url = NSURL.fileURLWithPath(filePath)
        val data = NSData.dataWithContentsOfURL(url) ?: return
        @Suppress("UNCHECKED_CAST")
        val errorPtr = null as NSError?
        val ap = AVAudioPlayer(data = data, error = null)
        if (ap != null) {
            ap.prepareToPlay()
            player = ap
        } else {
            Napier.e("AudioEngine.prepare failed for $filePath")
        }
    }

    actual fun play() {
        player?.play()
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun stop() {
        player?.stop()
        player?.currentTime = 0.0
    }

    @Suppress("MagicNumber")
    actual fun seekTo(positionMs: Long) {
        player?.currentTime = positionMs / 1000.0
    }

    actual fun setVolume(volume: Float) {
        player?.volume = volume.coerceIn(0f, 1f)
    }

    @Suppress("MagicNumber")
    actual val positionMs: Long
        get() = ((player?.currentTime ?: 0.0) * 1000.0).toLong()

    @Suppress("MagicNumber")
    actual val durationMs: Long
        get() = ((player?.duration ?: 0.0) * 1000.0).toLong()

    actual val isReady: Boolean
        get() = player != null

    actual val isPlaying: Boolean
        get() = player?.isPlaying() == true

    actual fun release() {
        player?.stop()
        player = null
    }
}
