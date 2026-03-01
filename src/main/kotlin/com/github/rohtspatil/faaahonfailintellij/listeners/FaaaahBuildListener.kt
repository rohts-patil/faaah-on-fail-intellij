package com.github.rohtspatil.faaahonfailintellij.listeners

import com.github.rohtspatil.faaahonfailintellij.services.FaaaahSound
import com.github.rohtspatil.faaahonfailintellij.services.SoundPlayer
import com.github.rohtspatil.faaahonfailintellij.settings.FaaaahSettings
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener

/**
 * Listens for Gradle / Maven external build task failures and plays the FAAAAH sound.
 * Registered via the ExternalSystemTaskNotificationListener.EP_NAME extension point in plugin.xml.
 */
class FaaaahBuildListener : ExternalSystemTaskNotificationListener {

    override fun onFailure(id: ExternalSystemTaskId, e: Exception) {
        val settings = FaaaahSettings.getInstance()
        if (!settings.state.enabled || !settings.state.onBuildFailure) return
        SoundPlayer.play(FaaaahSound.fromName(settings.state.soundName))
    }
}
