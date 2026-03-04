package com.github.rohtspatil.faaahonfailintellij.settings

import com.intellij.util.messages.Topic

/**
 * Application-level message bus listener for FAAAAH settings changes.
 *
 * Publish via [FaaaahSettings.notifyChanged].
 * Subscribe via [com.intellij.openapi.application.ApplicationManager.getApplication].messageBus.connect().
 */
fun interface FaaaahSettingsListener {
    fun settingsChanged(state: FaaaahSettings.State)

    companion object {
        val TOPIC: Topic<FaaaahSettingsListener> =
            Topic.create("FaaaahSettingsChanged", FaaaahSettingsListener::class.java)
    }
}

