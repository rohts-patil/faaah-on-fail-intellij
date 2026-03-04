package com.github.rohtspatil.faaahonfailintellij.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Service.Level.APP)
@State(
    name = "FaaaahOnFailSettings",
    storages = [Storage("faaaahOnFail.xml")]
)
class FaaaahSettings : PersistentStateComponent<FaaaahSettings.State> {

    data class State(
        var enabled: Boolean = true,
        var soundName: String = "faaaah",    // "faaaah", "fatality", "joker", "random", "custom"
        var customSoundPath: String = "",    // absolute path to user-supplied file when soundName == "custom"
        var onTestFailure: Boolean = true,
        var onBuildFailure: Boolean = true,
        var onTerminalError: Boolean = true
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    /** Broadcast the current state to all [FaaaahSettingsListener] subscribers. */
    fun notifyChanged() {
        ApplicationManager.getApplication().messageBus
            .syncPublisher(FaaaahSettingsListener.TOPIC)
            .settingsChanged(myState)
    }

    companion object {
        fun getInstance(): FaaaahSettings =
            ApplicationManager.getApplication().getService(FaaaahSettings::class.java)
    }
}
