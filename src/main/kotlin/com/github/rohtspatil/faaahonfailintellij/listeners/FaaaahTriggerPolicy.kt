package com.github.rohtspatil.faaahonfailintellij.listeners

object FaaaahTriggerPolicy {

    fun isBuildLikeExecution(
        executorId: String,
        runProfileName: String,
        runProfileClassName: String
    ): Boolean {
        val executor = executorId.lowercase()
        val profileName = runProfileName.lowercase()
        val profileClass = runProfileClassName.lowercase()

        return executor.contains("gradle") ||
                executor.contains("maven") ||
                executor.contains("external") ||
                profileName.contains("gradle") ||
                profileName.contains("maven") ||
                profileName.contains("external") ||
                profileClass.contains("gradle") ||
                profileClass.contains("maven") ||
                profileClass.contains("external")
    }

    fun shouldPlayForExecution(
        exitCode: Int,
        enabled: Boolean,
        onTerminalError: Boolean,
        isBuildLike: Boolean
    ): Boolean {
        if (exitCode == 0 || !enabled) return false

        return if (isBuildLike) {
            false
        } else {
            onTerminalError
        }
    }
}
