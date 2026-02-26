package com.github.rohtspatil.faaahonfailintellij.listeners

import com.github.rohtspatil.faaahonfailintellij.services.FaaaahSound
import com.github.rohtspatil.faaahonfailintellij.services.SoundPlayer
import com.github.rohtspatil.faaahonfailintellij.settings.FaaaahSettings
import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment

/**
 * Listens for any IntelliJ run/build process that exits with a non-zero code.
 * This covers:
 *  - Maven goals run from the Maven tool window (mvn test, mvn clean install, etc.)
 *  - Gradle tasks run from the Gradle tool window
 *  - Any other run configuration that fails at the process level
 *
 * JUnit test failures handled separately by FaaaahTestStatusListener.
 * Registered as a project-level listener in plugin.xml, so no startup activity needed.
 */
class FaaaahExecutionListener : ExecutionListener {

    override fun processTerminated(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler,
        exitCode: Int
    ) {
        if (exitCode == 0) return
        val settings = FaaaahSettings.getInstance()
        if (!settings.state.enabled || !settings.state.onBuildFailure) return
        SoundPlayer.play(resolveSound(settings.state.soundName))
    }

    private fun resolveSound(name: String): FaaaahSound = when (name) {
        "fatality" -> FaaaahSound.FATALITY
        "joker" -> FaaaahSound.JOKER
        "random" -> FaaaahSound.random()
        else -> FaaaahSound.FAAAAH
    }
}
