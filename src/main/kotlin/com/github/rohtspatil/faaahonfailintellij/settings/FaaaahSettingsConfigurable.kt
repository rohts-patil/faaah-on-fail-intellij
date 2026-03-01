package com.github.rohtspatil.faaahonfailintellij.settings

import com.github.rohtspatil.faaahonfailintellij.services.FaaaahSound
import com.github.rohtspatil.faaahonfailintellij.services.SoundPlayer
import com.intellij.openapi.options.Configurable
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class FaaaahSettingsConfigurable : Configurable {

    private var enabledCheckBox: JCheckBox? = null
    private var onTestFailureCheckBox: JCheckBox? = null
    private var onBuildFailureCheckBox: JCheckBox? = null
    private var onTerminalErrorCheckBox: JCheckBox? = null
    private var soundComboBox: JComboBox<String>? = null
    private var panel: JPanel? = null

    override fun getDisplayName(): String = "FAAAAH on Fail 🎺"

    override fun createComponent(): JComponent {
        val settings = FaaaahSettings.getInstance().state

        enabledCheckBox = JCheckBox("Enable FAAAAH on Fail", settings.enabled)
        onTestFailureCheckBox = JCheckBox("Play on JUnit/test failure", settings.onTestFailure)
        onBuildFailureCheckBox = JCheckBox("Play on build failure (Maven/Gradle)", settings.onBuildFailure)
        onTerminalErrorCheckBox = JCheckBox("Play on run/process failure (exit code != 0)", settings.onTerminalError)
        soundComboBox = JComboBox(arrayOf("faaaah", "fatality", "joker", "random")).also {
            it.selectedItem = settings.soundName
        }

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

        gc.gridx = 0; gc.gridy++; gc.gridwidth = 2; gc.insets = Insets(4, 4, 4, 4)
        val testButton = JButton("🎺 Test Sound").apply {
            addActionListener {
                val selected = soundComboBox!!.selectedItem as String
                val sound = FaaaahSound.fromName(selected)
                SoundPlayer.play(sound)
            }
        }
        p.add(testButton, gc)

        panel = p
        return p
    }

    override fun isModified(): Boolean {
        val state = FaaaahSettings.getInstance().state
        return enabledCheckBox?.isSelected != state.enabled ||
                onTestFailureCheckBox?.isSelected != state.onTestFailure ||
                onBuildFailureCheckBox?.isSelected != state.onBuildFailure ||
                onTerminalErrorCheckBox?.isSelected != state.onTerminalError ||
                soundComboBox?.selectedItem != state.soundName
    }

    override fun apply() {
        val settings = FaaaahSettings.getInstance()
        settings.state.enabled = enabledCheckBox?.isSelected ?: true
        settings.state.onTestFailure = onTestFailureCheckBox?.isSelected ?: true
        settings.state.onBuildFailure = onBuildFailureCheckBox?.isSelected ?: true
        settings.state.onTerminalError = onTerminalErrorCheckBox?.isSelected ?: true
        settings.state.soundName = soundComboBox?.selectedItem as? String ?: "faaaah"
    }

    override fun reset() {
        val state = FaaaahSettings.getInstance().state
        enabledCheckBox?.isSelected = state.enabled
        onTestFailureCheckBox?.isSelected = state.onTestFailure
        onBuildFailureCheckBox?.isSelected = state.onBuildFailure
        onTerminalErrorCheckBox?.isSelected = state.onTerminalError
        soundComboBox?.selectedItem = state.soundName
    }

    override fun disposeUIResources() {
        panel = null
        enabledCheckBox = null
        onTestFailureCheckBox = null
        onBuildFailureCheckBox = null
        onTerminalErrorCheckBox = null
        soundComboBox = null
    }
}
