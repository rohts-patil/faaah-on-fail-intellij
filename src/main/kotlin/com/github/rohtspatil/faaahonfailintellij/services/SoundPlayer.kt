package com.github.rohtspatil.faaahonfailintellij.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import javax.sound.sampled.AudioSystem

enum class FaaaahSound(val resource: String) {
    FAAAAH("/sounds/faaaah.wav"),
    FATALITY("/sounds/fatality.wav"),
    JOKER("/sounds/joker.wav");

    companion object {
        fun random(): FaaaahSound = entries.random()

        fun fromName(name: String): FaaaahSound = when (name) {
            "fatality" -> FATALITY
            "joker" -> JOKER
            "random" -> FaaaahSound.random()
            else -> FAAAAH
        }
    }
}

object SoundPlayer {

    private val log = thisLogger()

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
                            // Keep resources open until playback is done.
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
}
