package com.github.rohtspatil.faaahonfailintellij.services

import com.github.rohtspatil.faaahonfailintellij.settings.FaaaahSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

enum class FaaaahSound(val resource: String) {
    FAAAAH("/sounds/faaaah.wav"),
    FATALITY("/sounds/fatality.wav"),
    JOKER("/sounds/joker.wav"),
    CUSTOM(""); // placeholder – actual path comes from settings

    companion object {
        fun random(): FaaaahSound = listOf(FAAAAH, FATALITY, JOKER).random()

        fun fromName(name: String): FaaaahSound = when (name) {
            "fatality" -> FATALITY
            "joker"    -> JOKER
            "random"   -> random()
            "custom"   -> CUSTOM
            else       -> FAAAAH
        }
    }
}

object SoundPlayer {

    private val log = thisLogger()

    /** Play a bundled resource sound (WAV). */
    fun play(sound: FaaaahSound = FaaaahSound.FAAAAH) {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val stream = SoundPlayer::class.java.getResourceAsStream(sound.resource)
                    ?: run {
                        log.warn("Could not find sound resource: ${sound.resource}")
                        return@executeOnPooledThread
                    }
                stream.use { rawStream ->
                    AudioSystem.getAudioInputStream(rawStream.buffered()).use { audioStream ->
                        playClip(audioStream)
                    }
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                log.warn("Failed to play FAAAAH sound: ${e.message}")
            }
        }
    }

    /**
     * Play a sound from an absolute file path on the local filesystem.
     * WAV files are played via AudioSystem directly.
     * MP3 files are decoded via MpegAudioFileReader (mp3spi) without relying on
     * the Java SPI lookup, which is blocked by IntelliJ's plugin classloader.
     *
     * @throws IllegalArgumentException if the file is missing or blank.
     */
    @Throws(IllegalArgumentException::class)
    fun playFromFile(filePath: String) {
        if (filePath.isBlank()) {
            throw IllegalArgumentException("No custom sound file selected. Please choose a sound file in Settings.")
        }
        val file = File(filePath)
        if (!file.exists() || !file.isFile) {
            throw IllegalArgumentException("File not found: $filePath")
        }
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                if (filePath.lowercase().endsWith(".mp3")) {
                    playMp3(file)
                } else {
                    AudioSystem.getAudioInputStream(file).use { audioStream ->
                        playClip(audioStream)
                    }
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                log.warn("Failed to play '$filePath': ${e.message}")
            }
        }
    }

    /**
     * Decode and stream an MP3 file directly using MpegAudioFileReader,
     * bypassing the SPI registry that IntelliJ's classloader blocks.
     * Uses SourceDataLine for streaming (Clip requires a known frame count).
     */
    private fun playMp3(file: File) {
        // Directly instantiate the mp3spi reader — no SPI lookup needed
        val mp3Stream = javazoom.spi.mpeg.sampled.file.MpegAudioFileReader()
            .getAudioInputStream(file)

        val baseFormat = mp3Stream.format
        // Decode from MP3 to PCM_SIGNED (16-bit) for playback
        val pcmFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            baseFormat.sampleRate,
            16,
            baseFormat.channels,
            baseFormat.channels * 2,
            baseFormat.sampleRate,
            false
        )
        val pcmStream = javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider()
            .getAudioInputStream(pcmFormat, mp3Stream)

        // Stream via SourceDataLine — frame count from decoded MP3 is unknown (-1),
        // so Clip.open() would fail; SourceDataLine handles streaming correctly.
        val line = AudioSystem.getSourceDataLine(pcmFormat)
        line.open(pcmFormat, 8192)
        line.start()
        try {
            val buffer = ByteArray(8192)
            var bytesRead: Int
            pcmStream.use { stream ->
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    line.write(buffer, 0, bytesRead)
                }
            }
            line.drain() // wait for the buffer to finish playing
        } finally {
            line.close()
        }
    }

    /** Play an AudioInputStream via Clip (suitable for WAV with known frame count). */
    private fun playClip(audioStream: javax.sound.sampled.AudioInputStream) {
        val clip = AudioSystem.getClip()
        try {
            clip.open(audioStream)
            clip.start()
            Thread.sleep(clip.microsecondLength / 1000 + 200)
        } finally {
            clip.close()
        }
    }

    /**
     * Central dispatch: reads soundName + customSoundPath from settings and
     * plays the correct sound. Errors from custom file playback are logged.
     */
    fun playBySettings(state: FaaaahSettings.State) {
        if (state.soundName == "custom") {
            try {
                playFromFile(state.customSoundPath)
            } catch (e: IllegalArgumentException) {
                log.warn("FAAAAH: custom sound skipped — ${e.message}")
            }
        } else {
            play(FaaaahSound.fromName(state.soundName))
        }
    }
}
