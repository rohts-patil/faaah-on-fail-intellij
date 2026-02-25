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
                val audioStream = AudioSystem.getAudioInputStream(stream.buffered())
                val clip = AudioSystem.getClip()
                clip.open(audioStream)
                clip.start()
                // Wait for clip to finish before closing
                Thread.sleep(clip.microsecondLength / 1000 + 200)
                clip.close()
                audioStream.close()
            } catch (e: Exception) {
                log.warn("Failed to play FAAAAH sound: ${e.message}")
            }
        }
    }
}
