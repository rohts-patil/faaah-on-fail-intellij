package com.github.rohtspatil.faaahonfailintellij.toolWindow

import com.github.rohtspatil.faaahonfailintellij.services.FaaaahSound
import com.github.rohtspatil.faaahonfailintellij.services.SoundPlayer
import com.github.rohtspatil.faaahonfailintellij.settings.FaaaahSettings
import com.github.rohtspatil.faaahonfailintellij.settings.FaaaahSettingsListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class FaaaahToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        try {
            val twPanel = FaaaahToolWindowPanel()

            // Subscribe to settings changes from other panels (e.g. Settings dialog).
            // Tie the connection lifetime to the project so it is cleaned up automatically.
            val connection = ApplicationManager.getApplication().messageBus.connect(project)
            connection.subscribe(FaaaahSettingsListener.TOPIC, FaaaahSettingsListener { state ->
                twPanel.loadFromState(state)
            })

            val content = ContentFactory.getInstance().createContent(twPanel.panel, "", false)
            toolWindow.contentManager.addContent(content)
        } catch (e: Exception) {
            val fallback = JBPanel<JBPanel<*>>().apply {
                add(JBLabel("⚠️ Could not load panel: ${e.message}"))
            }
            toolWindow.contentManager.addContent(
                ContentFactory.getInstance().createContent(fallback, "", false)
            )
        }
    }
}

/**
 * Holds all widgets for the FAAAAH tool-window panel and wires up two-way sync:
 * - User interactions → persist to [FaaaahSettings] → publish [FaaaahSettingsListener.TOPIC]
 * - Incoming [loadFromState] calls (from the message bus) → update widget state without
 *   re-triggering [applySettings] (guarded by [isUpdating]).
 */
class FaaaahToolWindowPanel {

    // ── Widgets ──────────────────────────────────────────────────────────────
    private val enabledBox = JCheckBox("Enable FAAAAH on Fail")
    private val testBox = JCheckBox("🧪 JUnit / test failure")
    private val buildBox = JCheckBox("🔨 Build failure (Gradle/Maven)")
    private val runBox = JCheckBox("▶️  Run / process failure")
    private val soundCombo = JComboBox(arrayOf("faaaah", "fatality", "joker", "random", "custom"))
    private val customPath = JTextField(20).apply { isEditable = false }
    private val browseBtn = JButton("Browse…")
    private val customRow = JBPanel<JBPanel<*>>(GridBagLayout())

    /** Set to true while we are programmatically updating widgets to avoid re-publishing. */
    @Volatile
    private var isUpdating = false

    val panel: JBPanel<*>

    init {
        // Load initial state
        loadFromState(FaaaahSettings.getInstance().state)

        // ── Browse ────────────────────────────────────────────────────────
        browseBtn.addActionListener {
            val chooser = JFileChooser().apply {
                dialogTitle = "Select a sound file"
                fileFilter = FileNameExtensionFilter("Sound files (*.wav, *.mp3)", "wav", "mp3")
                isAcceptAllFileFilterUsed = false
                val cur = customPath.text.trim()
                if (cur.isNotBlank()) {
                    val f = java.io.File(cur); if (f.exists()) currentDirectory = f.parentFile
                }
            }
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                customPath.text = chooser.selectedFile.absolutePath
                applySettings()
            }
        }

        // ── Custom-path row ───────────────────────────────────────────────
        val cg = GridBagConstraints().apply { gridy = 0; insets = Insets(0, 0, 0, 4) }
        cg.gridx = 0; cg.fill = GridBagConstraints.HORIZONTAL; cg.weightx = 1.0; customRow.add(customPath, cg)
        cg.gridx = 1; cg.fill = GridBagConstraints.NONE; cg.weightx = 0.0; customRow.add(browseBtn, cg)

        // ── Listeners that write back to settings ─────────────────────────
        enabledBox.addActionListener { applySettings() }
        testBox.addActionListener { applySettings() }
        buildBox.addActionListener { applySettings() }
        runBox.addActionListener { applySettings() }
        soundCombo.addActionListener {
            if (!isUpdating) {
                customRow.isVisible = soundCombo.selectedItem == "custom"
                applySettings()
            }
        }

        panel = buildLayout()
    }

    // ── Persist widget state → settings → notify ──────────────────────────
    private fun applySettings() {
        if (isUpdating) return
        val s = FaaaahSettings.getInstance().state
        s.enabled = enabledBox.isSelected
        s.onTestFailure = testBox.isSelected
        s.onBuildFailure = buildBox.isSelected
        s.onTerminalError = runBox.isSelected
        s.soundName = soundCombo.selectedItem as String
        s.customSoundPath = customPath.text.trim()
        FaaaahSettings.getInstance().notifyChanged()
    }

    /** Called by the message-bus listener to refresh this panel from saved state. */
    fun loadFromState(state: FaaaahSettings.State) {
        isUpdating = true
        try {
            enabledBox.isSelected = state.enabled
            testBox.isSelected = state.onTestFailure
            buildBox.isSelected = state.onBuildFailure
            runBox.isSelected = state.onTerminalError
            soundCombo.selectedItem = state.soundName
            customPath.text = state.customSoundPath
            customRow.isVisible = state.soundName == "custom"
        } finally {
            isUpdating = false
        }
    }

    // ── Layout ────────────────────────────────────────────────────────────
    private fun buildLayout(): JBPanel<*> {
        val p = JBPanel<JBPanel<*>>(GridBagLayout())
        val gc = GridBagConstraints().apply {
            anchor = GridBagConstraints.NORTHWEST
            fill = GridBagConstraints.HORIZONTAL
            gridx = 0; gridy = 0; weightx = 1.0
        }

        fun row(inTop: Int = 2, inSide: Int = 10) = gc.also { gc.gridy++; gc.insets = Insets(inTop, inSide, 2, inSide) }

        gc.insets = Insets(10, 10, 4, 10)
        p.add(JBLabel("🎺 FAAAAH on Fail").apply { font = font.deriveFont(Font.BOLD, 14f) }, gc)
        p.add(enabledBox, row(6))
        p.add(JSeparator(), row(6))
        p.add(JBLabel("Triggers:").apply { font = font.deriveFont(Font.BOLD) }, row(4))
        p.add(testBox, row(2, 18))
        p.add(buildBox, row(2, 18))
        p.add(runBox, row(2, 18))
        p.add(JSeparator(), row(6))
        p.add(JBLabel("Sound:").apply { font = font.deriveFont(Font.BOLD) }, row(4))
        p.add(soundCombo, row())
        p.add(customRow, row())
        p.add(JSeparator(), row(8))
        p.add(JButton("🎺 Test Sound").apply {
            addActionListener {
                val s = FaaaahSettings.getInstance().state
                if (s.soundName == "custom") {
                    try {
                        SoundPlayer.playFromFile(s.customSoundPath)
                    } catch (e: IllegalArgumentException) {
                        JOptionPane.showMessageDialog(null, e.message, "Cannot Play", JOptionPane.ERROR_MESSAGE)
                    }
                } else {
                    SoundPlayer.play(FaaaahSound.fromName(s.soundName))
                }
            }
        }, row(4))

        gc.gridy++; gc.insets = Insets(2, 10, 10, 10); gc.weighty = 1.0
        gc.anchor = GridBagConstraints.NORTHWEST
        p.add(JButton("🔇 Stop Sound").apply { addActionListener { SoundPlayer.stop() } }, gc)
        return p
    }
}
