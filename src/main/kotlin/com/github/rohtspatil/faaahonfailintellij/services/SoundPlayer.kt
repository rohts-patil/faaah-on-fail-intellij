package com.github.rohtspatil.faaahonfailintellij.services

import com.github.rohtspatil.faaahonfailintellij.settings.FaaaahSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import java.io.File
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

    /** Play a bundled resource sound. */
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
                        val clip = AudioSystem.getClip()
                        try {
                            clip.open(audioStream)
                            clip.start()
                            Thread.sleep(clip.microsecondLength / 1000 + 200)
                        } finally {
                            clip.close()
                        }
                    }
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                log.warn("Failed to play FAAAAH sound: ${e.message}")
            }
        }
    }

    /** Play a sound from an absolute file path on the local filesystem. */
    fun playFromFile(filePath: String) {
        if (filePath.isBlank()) {
            log.warn("Custom sound path is empty, falling back to default sound")
            play(FaaaahSound.FAAAAH)
            return
        }
        val file = File(filePath)
        if (!file.exists() || !file.isFile) {
            log.warn("Custom sound file not found: $filePath, falling back to default sound")
            play(FaaaahSound.FAAAAH)
            return
        }
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                AudioSystem.getAudioInputStream(file).use { audioStream ->
                    val clip = AudioSystem.getClip()
                    try {
                        clip.open(audioStream)
                        clip.start()
                        Thread.sleep(clip.microsecondLength / 1000 + 200)
                    } finally {
                        clip.close()
                    }
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                log.warn("Failed to play custom sound file '$filePath': ${e.message}")
            }
        }
    }

    /**
     * Central dispatch: reads soundName + customSoundPath from settings and
     * plays the correct sound. All listeners should call this.
     */
    fun playBySettings(state: FaaaahSettings.State) {
        if (state.soundName == "custom") {
            playFromFile(state.customSoundPath)
        } else {
            play(FaaaahSound.fromName(state.soundName))
        }
    }
}
