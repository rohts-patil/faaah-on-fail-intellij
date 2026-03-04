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
                profileClass.contains("external") ||
                // Test runners are handled by FaaaahTestStatusListener; skip here to avoid
                // the execution listener playing when onTestFailure is disabled.
                profileClass.contains("junit") ||
                profileClass.contains("testng") ||
                profileClass.contains("kotest") ||
                profileName.contains("test")
    }

    /**
     * Whether to play the failure sound for a process that just terminated.
     *
     * Build-like executions (Maven, Gradle run configs, etc.) use the [onBuildFailure] setting.
     * Note: Gradle tasks run from the Gradle tool window go through ExternalSystem and are handled
     * by FaaaahBuildListener directly — they typically don't reach ExecutionListener at all.
     * Maven goals from the Maven tool window DO go through ExecutionListener (Maven is NOT part
     * of the ExternalSystem framework), so we must handle them here.
     *
     * Test runners are routed through FaaaahTestStatusListener — they are treated as build-like
     * here so that they are NOT double-played if they also fire a process termination event.
     */
    fun shouldPlayForExecution(
        exitCode: Int,
        enabled: Boolean,
        onBuildFailure: Boolean,
        onTerminalError: Boolean,
        isBuildLike: Boolean
    ): Boolean {
        if (exitCode == 0 || !enabled) return false
        return if (isBuildLike) onBuildFailure else onTerminalError
    }
}
