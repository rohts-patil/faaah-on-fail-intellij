package com.github.rohtspatil.faaahonfailintellij.listeners

import com.github.rohtspatil.faaahonfailintellij.services.SoundPlayer
import com.github.rohtspatil.faaahonfailintellij.settings.FaaaahSettings
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskType
import java.util.concurrent.ConcurrentHashMap

/**
 * Listens for Gradle / Maven external build task failures and plays the FAAAAH sound.
 * Registered via the ExternalSystemTaskNotificationListener.EP_NAME extension point in plugin.xml.
 *
 * Strategy:
 *  - Override BOTH the new (projectPath, id) AND old (id) method signatures so the listener
 *    works regardless of which IntelliJ version invokes which overload. The new methods call
 *    the old ones via default delegation only when NOT overridden — by overriding both we
 *    prevent double-calling while still catching all call paths.
 *  - Track task outcomes:
 *      succeededIds      – filled by onSuccess; task completed without error
 *      cancelledIds      – filled by onCancel; user cancelled, no sound
 *      handledByFailure  – filled by onFailure; sound already played, onEnd should skip
 *  - onFailure fires for explicit exception failures; onEnd is the terminal event for all tasks.
 *    Playing in BOTH (with a guard set) means we catch failure regardless of which one fires.
 *  - Only EXECUTE_TASK tasks are considered; RESOLVE_PROJECT (Gradle sync) is ignored.
 */
class FaaaahBuildListener : ExternalSystemTaskNotificationListener {

    private val log = thisLogger()
    private val succeededIds = ConcurrentHashMap.newKeySet<ExternalSystemTaskId>()
    private val cancelledIds = ConcurrentHashMap.newKeySet<ExternalSystemTaskId>()
    private val handledByFailure = ConcurrentHashMap.newKeySet<ExternalSystemTaskId>()

    // ── internal helpers ─────────────────────────────────────────────────────

    private fun isExecuteTask(id: ExternalSystemTaskId) =
        id.type == ExternalSystemTaskType.EXECUTE_TASK

    private fun onFailed(id: ExternalSystemTaskId) {
        if (!isExecuteTask(id)) return
        if (cancelledIds.remove(id)) return   // user cancelled — no sound
        succeededIds.remove(id)               // shouldn't be there, but clean up anyway
        if (handledByFailure.add(id)) {       // play exactly once even if both overloads fire
            log.info("FAAAAH build: onFailure for task $id → playing sound")
            playSound()
        }
    }

    private fun onEnded(id: ExternalSystemTaskId) {
        if (!isExecuteTask(id)) return
        val succeeded = succeededIds.remove(id)
        val cancelled = cancelledIds.remove(id)
        val alreadyHandled = handledByFailure.remove(id)
        log.info("FAAAAH build: onEnd task=$id succeeded=$succeeded cancelled=$cancelled alreadyHandled=$alreadyHandled")
        if (!succeeded && !cancelled && !alreadyHandled) {
            log.info("FAAAAH build: failure inferred from onEnd (no prior onSuccess/onFailure) → playing sound")
            playSound()
        }
    }

    private fun playSound() {
        val s = FaaaahSettings.getInstance().state
        if (s.enabled && s.onBuildFailure) SoundPlayer.playBySettings(s)
    }

    // ── NEW API (IntelliJ 2024+) — projectPath is the first parameter ────────

    override fun onSuccess(projectPath: String, id: ExternalSystemTaskId) {
        log.info("FAAAAH build: onSuccess (new) task=$id")
        succeededIds.add(id)
    }

    override fun onCancel(projectPath: String, id: ExternalSystemTaskId) {
        log.info("FAAAAH build: onCancel (new) task=$id")
        cancelledIds.add(id)
    }

    override fun onFailure(projectPath: String, id: ExternalSystemTaskId, exception: Exception) {
        log.info("FAAAAH build: onFailure (new) task=$id error=${exception.message}")
        onFailed(id)
    }

    override fun onEnd(projectPath: String, id: ExternalSystemTaskId) = onEnded(id)

    // ── OLD API (deprecated) — some IntelliJ versions call these directly ────

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onSuccess(id: ExternalSystemTaskId) {
        log.info("FAAAAH build: onSuccess (old) task=$id")
        succeededIds.add(id)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onCancel(id: ExternalSystemTaskId) {
        log.info("FAAAAH build: onCancel (old) task=$id")
        cancelledIds.add(id)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onFailure(id: ExternalSystemTaskId, e: Exception) {
        log.info("FAAAAH build: onFailure (old) task=$id error=${e.message}")
        onFailed(id)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onEnd(id: ExternalSystemTaskId) = onEnded(id)
}
