package org.biblestudio.features.worship.audio

import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl

/**
 * Desktop [AudioEngine] using javax.sound [Clip].
 *
 * Supports WAV and other formats natively supported by the JVM.
 * Position tracking uses [Clip.getMicrosecondPosition].
 */
@Suppress("TooManyFunctions")
actual class AudioEngine actual constructor() {

    private var clip: Clip? = null
    private var currentVolume: Float = 1.0f

    actual fun prepare(filePath: String) {
        release()
        val file = File(filePath)
        if (!file.exists()) return
        val audioStream: AudioInputStream = AudioSystem.getAudioInputStream(file)
        val baseFormat = audioStream.format
        // Decode to PCM if needed
        val decodedFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            baseFormat.sampleRate,
            16,
            baseFormat.channels,
            baseFormat.channels * 2,
            baseFormat.sampleRate,
            false
        )
        val decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream)
        val newClip = AudioSystem.getClip()
        newClip.open(decodedStream)
        clip = newClip
        setVolume(currentVolume)
    }

    actual fun play() {
        clip?.start()
    }

    actual fun pause() {
        clip?.stop()
    }

    actual fun stop() {
        clip?.stop()
        clip?.microsecondPosition = 0
    }

    actual fun seekTo(positionMs: Long) {
        clip?.microsecondPosition = positionMs * MICROS_PER_MS
    }

    @Suppress("MagicNumber")
    actual fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        val c = clip ?: return
        if (c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            val control = c.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            // Convert linear 0..1 to dB
            val dB = if (currentVolume > 0f) {
                (20.0 * kotlin.math.log10(currentVolume.toDouble())).toFloat()
            } else {
                control.minimum
            }
            control.value = dB.coerceIn(control.minimum, control.maximum)
        }
    }

    actual val positionMs: Long
        get() = (clip?.microsecondPosition ?: 0) / MICROS_PER_MS

    actual val durationMs: Long
        get() = (clip?.microsecondLength ?: 0) / MICROS_PER_MS

    actual val isReady: Boolean
        get() = clip?.isOpen == true

    actual val isPlaying: Boolean
        get() = clip?.isRunning == true

    actual fun release() {
        clip?.stop()
        clip?.close()
        clip = null
    }

    companion object {
        private const val MICROS_PER_MS = 1000L
    }
}
