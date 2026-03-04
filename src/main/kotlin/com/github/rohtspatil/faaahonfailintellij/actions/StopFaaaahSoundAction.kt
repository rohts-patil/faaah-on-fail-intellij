package com.github.rohtspatil.faaahonfailintellij.actions

import com.github.rohtspatil.faaahonfailintellij.services.SoundPlayer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class StopFaaaahSoundAction : AnAction("🔇 Stop FAAAAH Sound") {
    override fun actionPerformed(e: AnActionEvent) {
        SoundPlayer.stop()
    }
}
