package com.github.rohtspatil.faaahonfailintellij.toolWindow

import com.github.rohtspatil.faaahonfailintellij.services.FaaaahSound
import com.github.rohtspatil.faaahonfailintellij.services.SoundPlayer
import com.github.rohtspatil.faaahonfailintellij.settings.FaaaahSettings
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
            val panel = buildPanel()
            val content = ContentFactory.getInstance().createContent(panel, "", false)
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

    private fun buildPanel(): JBPanel<*> {
        val initialState = FaaaahSettings.getInstance().state

        // ── Widgets ────────────────────────────────────────────────────────
        val enabledBox = JCheckBox("Enable FAAAAH on Fail", initialState.enabled)
        val testBox = JCheckBox("🧪 JUnit / test failure", initialState.onTestFailure)
        val buildBox = JCheckBox("🔨 Build failure (Gradle/Maven)", initialState.onBuildFailure)
        val runBox = JCheckBox("▶️  Run / process failure", initialState.onTerminalError)
        val soundCombo = JComboBox(arrayOf("faaaah", "fatality", "joker", "random", "custom"))
        soundCombo.selectedItem = initialState.soundName
        val customPath = JTextField(initialState.customSoundPath, 20).apply { isEditable = false }
        val browseBtn = JButton("Browse…")

        // ── Save helper: identical to FaaaahSettingsConfigurable.apply() ──
        fun applySettings() {
            val s = FaaaahSettings.getInstance().state
            s.enabled = enabledBox.isSelected
            s.onTestFailure = testBox.isSelected
            s.onBuildFailure = buildBox.isSelected
            s.onTerminalError = runBox.isSelected
            s.soundName = soundCombo.selectedItem as String
            s.customSoundPath = customPath.text.trim()
        }

        enabledBox.addActionListener { applySettings() }
        testBox.addActionListener { applySettings() }
        buildBox.addActionListener { applySettings() }
        runBox.addActionListener { applySettings() }
        soundCombo.addActionListener { applySettings() }

        // ── Browse ─────────────────────────────────────────────────────────
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

        // ── Custom row ─────────────────────────────────────────────────────
        val customRow = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
            val cg = GridBagConstraints().apply { gridy = 0; insets = Insets(0, 0, 0, 4) }
            cg.gridx = 0; cg.fill = GridBagConstraints.HORIZONTAL; cg.weightx = 1.0; add(customPath, cg)
            cg.gridx = 1; cg.fill = GridBagConstraints.NONE; cg.weightx = 0.0; add(browseBtn, cg)
        }

        fun updateCustomRow() {
            customRow.isVisible = (soundCombo.selectedItem as String) == "custom"
        }
        updateCustomRow()
        soundCombo.addActionListener { updateCustomRow() }

        // ── Layout ─────────────────────────────────────────────────────────
        val panel = JBPanel<JBPanel<*>>(GridBagLayout())
        val gc = GridBagConstraints().apply {
            anchor = GridBagConstraints.NORTHWEST
            fill = GridBagConstraints.HORIZONTAL
            gridx = 0; gridy = 0; weightx = 1.0
        }

        fun row(inTop: Int = 2, inSide: Int = 10) = gc.also {
            gc.gridy++; gc.insets = Insets(inTop, inSide, 2, inSide)
        }

        gc.insets = Insets(10, 10, 4, 10)
        panel.add(JBLabel("🎺 FAAAAH on Fail").apply { font = font.deriveFont(Font.BOLD, 14f) }, gc)
        panel.add(enabledBox, row(6))
        panel.add(JSeparator(), row(6))
        panel.add(JBLabel("Triggers:").apply { font = font.deriveFont(Font.BOLD) }, row(4))
        panel.add(testBox, row(2, 18))
        panel.add(buildBox, row(2, 18))
        panel.add(runBox, row(2, 18))
        panel.add(JSeparator(), row(6))
        panel.add(JBLabel("Sound:").apply { font = font.deriveFont(Font.BOLD) }, row(4))
        panel.add(soundCombo, row())
        panel.add(customRow, row())
        panel.add(JSeparator(), row(8))
        panel.add(JButton("🎺 Test Sound").apply {
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
        panel.add(JButton("🔇 Stop Sound").apply { addActionListener { SoundPlayer.stop() } }, gc)

        return panel
    }
}
