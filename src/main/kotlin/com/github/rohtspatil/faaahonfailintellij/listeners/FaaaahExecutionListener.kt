package com.github.rohtspatil.faaahonfailintellij.listeners

import com.github.rohtspatil.faaahonfailintellij.services.FaaaahSound
import com.github.rohtspatil.faaahonfailintellij.services.SoundPlayer
import com.github.rohtspatil.faaahonfailintellij.settings.FaaaahSettings
import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment

/**
 * Listens for IntelliJ processes that end with a non-zero exit code.
 * Build-like executions are handled by FaaaahBuildListener to avoid duplicate playback,
 * while non-build run/process failures are gated by the onTerminalError setting.
 *
 * JUnit test failures handled separately by FaaaahTestStatusListener.
 */
class FaaaahExecutionListener : ExecutionListener {

    override fun processTerminated(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler,
        exitCode: Int
    ) {
        val settings = FaaaahSettings.getInstance()
        val isBuildLike = FaaaahTriggerPolicy.isBuildLikeExecution(
            executorId = executorId,
            runProfileName = env.runProfile.name,
            runProfileClassName = env.runProfile.javaClass.name
        )
        val shouldPlay = FaaaahTriggerPolicy.shouldPlayForExecution(
            exitCode = exitCode,
            enabled = settings.state.enabled,
            onTerminalError = settings.state.onTerminalError,
            isBuildLike = isBuildLike
        )
        if (!shouldPlay) return
        SoundPlayer.play(FaaaahSound.fromName(settings.state.soundName))
    }
}
