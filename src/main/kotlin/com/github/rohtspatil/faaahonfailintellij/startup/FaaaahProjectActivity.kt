package com.github.rohtspatil.faaahonfailintellij.startup

import com.github.rohtspatil.faaahonfailintellij.listeners.FaaaahExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Startup activity that subscribes listeners to the PROJECT-level message bus.
 *
 * ExecutionManager.EXECUTION_TOPIC is @ProjectLevel — it fires on the project bus,
 * NOT the application bus. Using declarative <listeners> in plugin.xml puts listeners
 * on the app bus and would miss these events. We must subscribe here explicitly.
 */
class FaaaahProjectActivity : ProjectActivity {

    private val log = thisLogger()

    override suspend fun execute(project: Project) {
        log.info("FAAAAH on Fail: initializing for project '${project.name}'")
        project.messageBus.connect().subscribe(
            ExecutionManager.EXECUTION_TOPIC,
            FaaaahExecutionListener()
        )
        log.info("FAAAAH on Fail: ExecutionListener subscribed on project bus")
    }
}
