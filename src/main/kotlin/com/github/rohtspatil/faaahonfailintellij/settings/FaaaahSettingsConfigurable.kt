package com.github.rohtspatil.faaahonfailintellij.settings

import com.github.rohtspatil.faaahonfailintellij.services.FaaaahSound
import com.github.rohtspatil.faaahonfailintellij.services.SoundPlayer
import com.intellij.openapi.options.Configurable
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class FaaaahSettingsConfigurable : Configurable {

    private var enabledCheckBox: JCheckBox? = null
    private var onTestFailureCheckBox: JCheckBox? = null
    private var onBuildFailureCheckBox: JCheckBox? = null
    private var onTerminalErrorCheckBox: JCheckBox? = null
    private var soundComboBox: JComboBox<String>? = null

    // Custom sound row widgets
    private var customSoundPathField: JTextField? = null
    private var browseButton: JButton? = null
    private var customSoundRow: JPanel? = null

    private var panel: JPanel? = null

    override fun getDisplayName(): String = "FAAAAH on Fail 🎺"

    override fun createComponent(): JComponent {
        val settings = FaaaahSettings.getInstance().state

        enabledCheckBox = JCheckBox("Enable FAAAAH on Fail", settings.enabled)
        onTestFailureCheckBox = JCheckBox("Play on JUnit/test failure", settings.onTestFailure)
        onBuildFailureCheckBox = JCheckBox("Play on build failure (Maven/Gradle)", settings.onBuildFailure)
        onTerminalErrorCheckBox = JCheckBox("Play on run/process failure (exit code != 0)", settings.onTerminalError)

        soundComboBox = JComboBox(arrayOf("faaaah", "fatality", "joker", "random", "custom")).also {
            it.selectedItem = settings.soundName
        }

        // --- Custom sound file row ---
        customSoundPathField = JTextField(settings.customSoundPath, 30).apply {
            isEditable = false
            toolTipText = "Absolute path to a .wav or .mp3 file"
        }
        browseButton = JButton("Browse…").apply {
            addActionListener {
                val chooser = JFileChooser().apply {
                    dialogTitle = "Select a sound file"
                    fileFilter = FileNameExtensionFilter("Sound files (*.wav, *.mp3)", "wav", "mp3")
                    isAcceptAllFileFilterUsed = false
                    // Pre-populate with existing path if valid
                    val current = customSoundPathField?.text?.trim() ?: ""
                    if (current.isNotBlank()) {
                        val f = java.io.File(current)
                        if (f.exists()) currentDirectory = f.parentFile
                    }
                }
                if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                    customSoundPathField?.text = chooser.selectedFile.absolutePath
                }
            }
        }

        customSoundRow = JPanel(GridBagLayout()).apply {
            val gc2 = GridBagConstraints().apply {
                anchor = GridBagConstraints.WEST
                insets = Insets(0, 0, 0, 4)
                gridx = 0; gridy = 0
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
            add(customSoundPathField!!, gc2)
            gc2.gridx = 1; gc2.fill = GridBagConstraints.NONE; gc2.weightx = 0.0
            add(browseButton!!, gc2)
        }
        updateCustomRowVisibility(settings.soundName)

        soundComboBox!!.addActionListener {
            updateCustomRowVisibility(soundComboBox!!.selectedItem as String)
        }

        // --- Main panel layout ---
        val p = JPanel(GridBagLayout())
        val gc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            insets = Insets(4, 4, 4, 4)
            gridx = 0; gridy = 0; gridwidth = 2
        }

        p.add(enabledCheckBox!!, gc)

        gc.gridy++
        gc.insets = Insets(8, 4, 2, 4)
        p.add(JLabel("Triggers:"), gc)

        gc.gridy++; gc.insets = Insets(2, 16, 2, 4)
        p.add(onTestFailureCheckBox!!, gc)

        gc.gridy++
        p.add(onBuildFailureCheckBox!!, gc)

        gc.gridy++
        p.add(onTerminalErrorCheckBox!!, gc)

        gc.gridy++; gc.insets = Insets(8, 4, 2, 4); gc.gridwidth = 1
        p.add(JLabel("Sound:"), gc)
        gc.gridx = 1
        p.add(soundComboBox!!, gc)

        // Custom file row (hidden unless "custom" is selected)
        gc.gridx = 0; gc.gridy++; gc.gridwidth = 2; gc.insets = Insets(2, 16, 2, 4)
        gc.fill = GridBagConstraints.HORIZONTAL
        p.add(customSoundRow!!, gc)
        gc.fill = GridBagConstraints.NONE

        gc.gridx = 0; gc.gridy++; gc.gridwidth = 2; gc.insets = Insets(4, 4, 4, 4)
        val buttonRow = JPanel().apply {
            val testButton = JButton("🎺 Test Sound").apply {
                addActionListener {
                    val selected = soundComboBox!!.selectedItem as String
                    if (selected == "custom") {
                        try {
                            SoundPlayer.playFromFile(customSoundPathField?.text?.trim() ?: "")
                        } catch (e: IllegalArgumentException) {
                            JOptionPane.showMessageDialog(
                                panel,
                                e.message,
                                "FAAAAH — Cannot Play Sound",
                                JOptionPane.ERROR_MESSAGE
                            )
                        }
                    } else {
                        SoundPlayer.play(FaaaahSound.fromName(selected))
                    }
                }
            }
            val stopButton = JButton("🔇 Stop Sound").apply {
                addActionListener { SoundPlayer.stop() }
            }
            add(testButton)
            add(stopButton)
        }
        p.add(buttonRow, gc)

        panel = p
        return p
    }

    private fun updateCustomRowVisibility(soundName: String) {
        val visible = soundName == "custom"
        customSoundRow?.isVisible = visible
        customSoundPathField?.isVisible = visible
        browseButton?.isVisible = visible
    }

    override fun isModified(): Boolean {
        val state = FaaaahSettings.getInstance().state
        return enabledCheckBox?.isSelected != state.enabled ||
                onTestFailureCheckBox?.isSelected != state.onTestFailure ||
                onBuildFailureCheckBox?.isSelected != state.onBuildFailure ||
                onTerminalErrorCheckBox?.isSelected != state.onTerminalError ||
                soundComboBox?.selectedItem != state.soundName ||
                customSoundPathField?.text?.trim() != state.customSoundPath
    }

    override fun apply() {
        val settings = FaaaahSettings.getInstance()
        settings.state.enabled = enabledCheckBox?.isSelected ?: true
        settings.state.onTestFailure = onTestFailureCheckBox?.isSelected ?: true
        settings.state.onBuildFailure = onBuildFailureCheckBox?.isSelected ?: true
        settings.state.onTerminalError = onTerminalErrorCheckBox?.isSelected ?: true
        settings.state.soundName = soundComboBox?.selectedItem as? String ?: "faaaah"
        settings.state.customSoundPath = customSoundPathField?.text?.trim() ?: ""
    }

    override fun reset() {
        val state = FaaaahSettings.getInstance().state
        enabledCheckBox?.isSelected = state.enabled
        onTestFailureCheckBox?.isSelected = state.onTestFailure
        onBuildFailureCheckBox?.isSelected = state.onBuildFailure
        onTerminalErrorCheckBox?.isSelected = state.onTerminalError
        soundComboBox?.selectedItem = state.soundName
        customSoundPathField?.text = state.customSoundPath
        updateCustomRowVisibility(state.soundName)
    }

    override fun disposeUIResources() {
        panel = null
        enabledCheckBox = null
        onTestFailureCheckBox = null
        onBuildFailureCheckBox = null
        onTerminalErrorCheckBox = null
        soundComboBox = null
        customSoundPathField = null
        browseButton = null
        customSoundRow = null
    }
}
