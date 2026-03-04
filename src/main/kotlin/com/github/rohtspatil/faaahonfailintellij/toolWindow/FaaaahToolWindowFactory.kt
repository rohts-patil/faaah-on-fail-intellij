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
                add(JBLabel("⚠️ Could not load FAAAAH panel: ${e.message}"))
            }
            toolWindow.contentManager.addContent(
                ContentFactory.getInstance().createContent(fallback, "", false)
            )
        }
    }

    /** Immediately persist a partial settings update via the canonical loadState path. */
    private fun save(block: FaaaahSettings.State.() -> Unit) {
        val settings = FaaaahSettings.getInstance()
        val updated = settings.state.copy().also(block)
        settings.loadState(updated)
    }

    private fun buildPanel(): JBPanel<*> {
        val settings = FaaaahSettings.getInstance()
        val state = settings.state

        val panel = JBPanel<JBPanel<*>>(GridBagLayout())
        val gc = GridBagConstraints().apply {
            anchor = GridBagConstraints.NORTHWEST
            fill = GridBagConstraints.HORIZONTAL
            gridx = 0; gridy = 0; weightx = 1.0
            insets = Insets(10, 10, 4, 10)
        }

        // ── Title ──────────────────────────────────────────────────────────
        panel.add(JBLabel("🎺 FAAAAH on Fail").apply {
            font = font.deriveFont(Font.BOLD, 14f)
        }, gc)

        // ── Enable ─────────────────────────────────────────────────────────
        gc.gridy++; gc.insets = Insets(6, 10, 2, 10)
        val enabledBox = JCheckBox("Enable FAAAAH on Fail", state.enabled)
        enabledBox.addActionListener { save { enabled = enabledBox.isSelected } }
        panel.add(enabledBox, gc)

        // ── Separator ──────────────────────────────────────────────────────
        gc.gridy++; gc.insets = Insets(6, 10, 2, 10)
        panel.add(JSeparator(), gc)

        // ── Triggers ───────────────────────────────────────────────────────
        gc.gridy++; gc.insets = Insets(4, 10, 2, 10)
        panel.add(JBLabel("Triggers:").apply { font = font.deriveFont(Font.BOLD) }, gc)

        gc.insets = Insets(2, 18, 2, 10)

        val testBox = JCheckBox("🧪 JUnit / test failure", state.onTestFailure)
        testBox.addActionListener { save { onTestFailure = testBox.isSelected } }
        gc.gridy++; panel.add(testBox, gc)

        val buildBox = JCheckBox("🔨 Build failure (Gradle/Maven)", state.onBuildFailure)
        buildBox.addActionListener { save { onBuildFailure = buildBox.isSelected } }
        gc.gridy++; panel.add(buildBox, gc)

        val runBox = JCheckBox("▶️  Run / process failure", state.onTerminalError)
        runBox.addActionListener { save { onTerminalError = runBox.isSelected } }
        gc.gridy++; panel.add(runBox, gc)

        // ── Separator ──────────────────────────────────────────────────────
        gc.gridy++; gc.insets = Insets(6, 10, 2, 10)
        panel.add(JSeparator(), gc)

        // ── Sound selector ─────────────────────────────────────────────────
        gc.gridy++; gc.insets = Insets(4, 10, 2, 10)
        panel.add(JBLabel("Sound:").apply { font = font.deriveFont(Font.BOLD) }, gc)

        gc.gridy++; gc.insets = Insets(2, 10, 2, 10)
        val soundCombo = JComboBox(arrayOf("faaaah", "fatality", "joker", "random", "custom"))
        soundCombo.selectedItem = state.soundName
        panel.add(soundCombo, gc)

        // ── Custom sound row ───────────────────────────────────────────────
        val customPathField = JTextField(state.customSoundPath, 20)
        customPathField.isEditable = false

        val browseBtn = JButton("Browse…")
        browseBtn.addActionListener {
            val chooser = JFileChooser().apply {
                dialogTitle = "Select a sound file"
                fileFilter = FileNameExtensionFilter("Sound files (*.wav, *.mp3)", "wav", "mp3")
                isAcceptAllFileFilterUsed = false
                val cur = customPathField.text.trim()
                if (cur.isNotBlank()) {
                    val f = java.io.File(cur)
                    if (f.exists()) currentDirectory = f.parentFile
                }
            }
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                val path = chooser.selectedFile.absolutePath
                customPathField.text = path
                save { customSoundPath = path }
            }
        }

        val customRow = JBPanel<JBPanel<*>>(GridBagLayout()).apply {
            val cg = GridBagConstraints().apply {
                gridy = 0; anchor = GridBagConstraints.WEST; insets = Insets(0, 0, 0, 4)
            }
            cg.gridx = 0; cg.fill = GridBagConstraints.HORIZONTAL; cg.weightx = 1.0
            add(customPathField, cg)
            cg.gridx = 1; cg.fill = GridBagConstraints.NONE; cg.weightx = 0.0
            add(browseBtn, cg)
        }

        fun updateCustomRowVisibility(name: String) {
            customRow.isVisible = name == "custom"
            panel.revalidate(); panel.repaint()
        }
        updateCustomRowVisibility(state.soundName)

        soundCombo.addActionListener {
            val selected = soundCombo.selectedItem as String
            save { soundName = selected }
            updateCustomRowVisibility(selected)
        }

        gc.gridy++; gc.insets = Insets(2, 10, 2, 10)
        panel.add(customRow, gc)

        // ── Separator ──────────────────────────────────────────────────────
        gc.gridy++; gc.insets = Insets(8, 10, 4, 10)
        panel.add(JSeparator(), gc)

        // ── Buttons ────────────────────────────────────────────────────────
        gc.gridy++; gc.insets = Insets(4, 10, 2, 10)
        val testSoundBtn = JButton("🎺 Test Sound")
        testSoundBtn.addActionListener {
            val s = FaaaahSettings.getInstance().state
            if (s.soundName == "custom") {
                try {
                    SoundPlayer.playFromFile(s.customSoundPath)
                } catch (e: IllegalArgumentException) {
                    JOptionPane.showMessageDialog(panel, e.message, "Cannot Play Sound", JOptionPane.ERROR_MESSAGE)
                }
            } else {
                SoundPlayer.play(FaaaahSound.fromName(s.soundName))
            }
        }
        panel.add(testSoundBtn, gc)

        gc.gridy++; gc.insets = Insets(2, 10, 10, 10); gc.weighty = 1.0
        gc.anchor = GridBagConstraints.NORTHWEST
        val stopBtn = JButton("🔇 Stop Sound")
        stopBtn.addActionListener { SoundPlayer.stop() }
        panel.add(stopBtn, gc)

        return panel
    }
}
