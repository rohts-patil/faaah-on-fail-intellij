package com.github.rohtspatil.faaahonfailintellij.services

import com.github.rohtspatil.faaahonfailintellij.settings.FaaaahSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import javax.sound.sampled.*

enum class FaaaahSound(val resource: String) {
    FAAAAH("/sounds/faaaah.wav"),
    FATALITY("/sounds/fatality.wav"),
    JOKER("/sounds/joker.wav"),
    CUSTOM(""); // placeholder – actual path comes from settings

    companion object {
        fun random(): FaaaahSound = listOf(FAAAAH, FATALITY, JOKER).random()

        fun fromName(name: String): FaaaahSound = when (name) {
            "fatality" -> FATALITY
            "joker" -> JOKER
            "random" -> random()
            "custom" -> CUSTOM
            else -> FAAAAH
        }
    }
}

object SoundPlayer {

    private val log = thisLogger()

    private val activeClip = AtomicReference<Clip?>(null)
    private val activeLine = AtomicReference<SourceDataLine?>(null)
    private val activeThread = AtomicReference<Thread?>(null)

    /** Stop any currently playing sound. */
    fun stop() {
        activeThread.getAndSet(null)?.interrupt()
        activeClip.getAndSet(null)?.runCatching { stop(); close() }
        activeLine.getAndSet(null)?.runCatching { stop(); flush(); close() }
    }

    /** Play a bundled resource sound (WAV). */
    fun play(sound: FaaaahSound = FaaaahSound.FAAAAH) {
        stop()
        launchThread {
            try {
                val stream = SoundPlayer::class.java.getResourceAsStream(sound.resource)
                    ?: run { log.warn("Sound resource not found: ${sound.resource}"); return@launchThread }
                stream.use { raw ->
                    AudioSystem.getAudioInputStream(raw.buffered()).use { playClip(it) }
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                log.warn("Failed to play FAAAAH sound: ${e.message}")
            }
        }
    }

    /**
     * Play a sound from an absolute file path.
     * WAV: AudioSystem directly. MP3: MpegAudioFileReader (bypasses IntelliJ's SPI block).
     *
     * Shows a cancellable background task in the IDE progress strip — clicking ✕ stops playback.
     *
     * @throws IllegalArgumentException if file is missing or blank.
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
        stop()

        launchThread {
            try {
                if (filePath.lowercase().endsWith(".mp3")) playMp3(file)
                else AudioSystem.getAudioInputStream(file).use { playClip(it) }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                log.warn("Failed to play '$filePath': ${e.message}")
            }
        }

        // Show "🎺 FAAAAH is playing… [✕]" in the IDE background-tasks strip.
        // Clicking ✕ sets isCanceled = true → we call stop().
        ApplicationManager.getApplication().executeOnPooledThread {
            ProgressManager.getInstance().run(
                object : Task.Backgroundable(null, "🎺 FAAAAH is playing…", /* canBeCancelled= */ true) {
                    override fun run(indicator: ProgressIndicator) {
                        indicator.isIndeterminate = true
                        while (activeThread.get()?.isAlive == true) {
                            if (indicator.isCanceled) {
                                stop()
                                return
                            }
                            Thread.sleep(100)
                        }
                    }
                }
            )
        }
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private fun launchThread(block: () -> Unit) {
        val thread = Thread {
            try {
                block()
            } finally {
                activeThread.compareAndSet(Thread.currentThread(), null)
            }
        }
        thread.isDaemon = true
        activeThread.set(thread) // set BEFORE start to avoid race with stop()
        thread.start()
    }

    /** Decode MP3 via MpegAudioFileReader directly (bypasses IntelliJ's classloader SPI block). */
    private fun playMp3(file: File) {
        val mp3Stream = javazoom.spi.mpeg.sampled.file.MpegAudioFileReader().getAudioInputStream(file)
        val baseFormat = mp3Stream.format
        val pcmFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED, baseFormat.sampleRate, 16,
            baseFormat.channels, baseFormat.channels * 2, baseFormat.sampleRate, false
        )
        val pcmStream = javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider()
            .getAudioInputStream(pcmFormat, mp3Stream)

        val line = AudioSystem.getSourceDataLine(pcmFormat)
        line.open(pcmFormat, 8192)
        line.start()
        activeLine.set(line)
        try {
            val buffer = ByteArray(8192)
            pcmStream.use { stream ->
                var n = stream.read(buffer)
                while (n != -1 && !Thread.currentThread().isInterrupted) {
                    line.write(buffer, 0, n)
                    n = stream.read(buffer)
                }
            }
            if (!Thread.currentThread().isInterrupted) line.drain()
        } finally {
            activeLine.compareAndSet(line, null)
            line.runCatching { stop(); flush(); close() }
        }
    }

    /** Play a WAV via Clip, polling for interruption every 50 ms. */
    private fun playClip(audioStream: AudioInputStream) {
        val clip = AudioSystem.getClip()
        clip.open(audioStream)
        activeClip.set(clip)
        try {
            clip.start()
            val durationMs = clip.microsecondLength / 1000 + 200
            var elapsed = 0L
            while (elapsed < durationMs && !Thread.currentThread().isInterrupted) {
                Thread.sleep(50)
                elapsed += 50
            }
        } finally {
            activeClip.compareAndSet(clip, null)
            clip.runCatching { stop(); close() }
        }
    }

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
