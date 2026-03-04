package com.github.rohtspatil.faaahonfailintellij.listeners

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FaaaahTriggerPolicyTest {

    @Test
    fun `build-like execution is detected from executor id`() {
        val isBuildLike = FaaaahTriggerPolicy.isBuildLikeExecution(
            executorId = "ExternalSystemTaskExecutor",
            runProfileName = "Run Anything",
            runProfileClassName = "com.intellij.execution.impl.UnknownRunProfile"
        )

        assertTrue(isBuildLike)
    }

    @Test
    fun `build-like execution is detected from run profile metadata`() {
        val isBuildLike = FaaaahTriggerPolicy.isBuildLikeExecution(
            executorId = "Run",
            runProfileName = "Gradle: test",
            runProfileClassName = "org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration"
        )

        assertTrue(isBuildLike)
    }

    @Test
    fun `normal run execution is not classified as build-like`() {
        val isBuildLike = FaaaahTriggerPolicy.isBuildLikeExecution(
            executorId = "Run",
            runProfileName = "MyApp",
            runProfileClassName = "com.intellij.execution.application.ApplicationConfiguration"
        )

        assertFalse(isBuildLike)
    }

    @Test
    fun `execution sound is suppressed for successful exit`() {
        val shouldPlay = FaaaahTriggerPolicy.shouldPlayForExecution(
            exitCode = 0,
            enabled = true,
            onBuildFailure = true,
            onTerminalError = true,
            isBuildLike = false
        )

        assertFalse(shouldPlay)
    }

    @Test
    fun `execution sound plays for build-like failure when onBuildFailure is enabled`() {
        val shouldPlay = FaaaahTriggerPolicy.shouldPlayForExecution(
            exitCode = 1,
            enabled = true,
            onBuildFailure = true,
            onTerminalError = false,
            isBuildLike = true
        )

        assertTrue(shouldPlay)
    }

    @Test
    fun `execution sound is suppressed for build-like failure when onBuildFailure is disabled`() {
        val shouldPlay = FaaaahTriggerPolicy.shouldPlayForExecution(
            exitCode = 1,
            enabled = true,
            onBuildFailure = false,
            onTerminalError = true,
            isBuildLike = true
        )

        assertFalse(shouldPlay)
    }

    @Test
    fun `execution sound respects run process toggle for non-build failures`() {
        val disabled = FaaaahTriggerPolicy.shouldPlayForExecution(
            exitCode = 1,
            enabled = true,
            onBuildFailure = false,
            onTerminalError = false,
            isBuildLike = false
        )
        val enabled = FaaaahTriggerPolicy.shouldPlayForExecution(
            exitCode = 1,
            enabled = true,
            onBuildFailure = false,
            onTerminalError = true,
            isBuildLike = false
        )

        assertFalse(disabled)
        assertTrue(enabled)
    }
}
