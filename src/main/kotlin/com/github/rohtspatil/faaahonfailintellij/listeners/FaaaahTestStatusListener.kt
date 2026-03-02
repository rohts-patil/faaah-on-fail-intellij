package com.github.rohtspatil.faaahonfailintellij.listeners

import com.github.rohtspatil.faaahonfailintellij.services.SoundPlayer
import com.github.rohtspatil.faaahonfailintellij.settings.FaaaahSettings
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.TestStatusListener

class FaaaahTestStatusListener : TestStatusListener() {

    override fun testSuiteFinished(root: AbstractTestProxy?) {
        val settings = FaaaahSettings.getInstance()
        if (!settings.state.enabled || !settings.state.onTestFailure) return
        if (root != null && !root.isPassed) {
            SoundPlayer.playBySettings(settings.state)
        }
    }
}
