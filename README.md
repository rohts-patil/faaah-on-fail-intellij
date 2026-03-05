# 🎺 FAAAAH on Fail

> *Because silent failures are cowardly failures.*

![Build](https://github.com/rohts-patil/faaah-on-fail-intellij/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/30372-faaaah-on-fail.svg)](https://plugins.jetbrains.com/plugin/30372-faaaah-on-fail)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/30372-faaaah-on-fail.svg)](https://plugins.jetbrains.com/plugin/30372-faaaah-on-fail)

---

<!-- Plugin description -->
You're deep in the zone. Headphones on. Coffee in hand. You kick off a build and go refill your cup.  
You come back. The build failed 37 seconds ago. You've been sitting in silence, oblivious.

**FAAAAH on Fail fixes that.**

It plays a loud, unmissable sound every time your IDE encounters a failure — so you *always* know the moment something breaks, even when you're not watching the screen.

Detects failures from:
- 🧪 JUnit / test framework runs
- 🔨 Gradle / Maven / external system builds
- ▶️ Run configurations (application, scripts, processes)

Pick from 4 built-in sounds or **choose your own file** — configurable from **Settings → Tools → FAAAAH on Fail 🎺**.
<!-- Plugin description end -->

---

## 🔊 What Gets Triggered

| Event | What happened |
|---|---|
| 🧪 **JUnit / Test failure** | Your tests turned red |
| 🔨 **Gradle / Maven build failure** | The build exploded |
| ▶️ **Run configuration failure** | Your app/script exited with a non-zero code |

---

## 🎵 Sound Options

Choose your weapon in **Settings → Tools → FAAAAH on Fail 🎺**:

| Sound | Vibe |
|---|---|
| `faaaah` *(default)* | The classic. Loud. Dramatic. Perfect. |
| `fatality` | Mortal Kombat energy. You have been destroyed. |
| `joker` | Why so serious? Your tests certainly aren't. |
| `random` | A surprise every time. Keep 'em guessing. |
| `custom` 🆕 | Pick **any `.wav` or `.mp3` from your machine** |

### 🗂️ Custom Sound

Select **`custom`** in the dropdown and click **Browse…** to choose any file from your local filesystem.  
The path is saved and persists across IDE restarts. You can preview it instantly with the **🎺 Test Sound** button.

---

## ⚙️ Settings

Everything is in one place: <kbd>Settings</kbd> → <kbd>Tools</kbd> → <kbd>FAAAAH on Fail 🎺</kbd>

- **Enable / disable** the plugin globally
- Toggle sound independently for each trigger type:
  - ✅ Test failures
  - 🔨 Build failures
  - ▶️ Run/process failures
- Choose from 4 built-in sounds **or pick your own file**
- Hit **🎺 Test Sound** any time to preview without triggering a failure

---

## 📦 Installation

**Via the IDE (recommended):**

<kbd>Settings</kbd> → <kbd>Plugins</kbd> → <kbd>Marketplace</kbd> → search **"FAAAAH on Fail"** → <kbd>Install</kbd>

**Via JetBrains Marketplace:**

Visit [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/30372-faaaah-on-fail/) and click **Install to …**

**Manually:**

Download the [latest release](https://github.com/rohts-patil/faaah-on-fail-intellij/releases/latest) and install via  
<kbd>Settings</kbd> → <kbd>Plugins</kbd> → <kbd>⚙️</kbd> → <kbd>Install plugin from disk…</kbd>

---

## 🛠️ Building from Source

```bash
git clone https://github.com/rohts-patil/faaah-on-fail-intellij.git
cd faaah-on-fail-intellij

# Run tests
./gradlew test

# Launch a sandbox IDE with the plugin loaded
./gradlew runIde
```

Requires **JDK 17+**. Compatible with IntelliJ IDEA 2025.2 and later.

---

## 🤝 Contributing

PRs and issues are very welcome. Got a hilarious sound suggestion? Open an issue with it. Want to add a new trigger? Fork away.

---

## 📄 License

[Apache 2.0](LICENSE)

---

*Built with 🎺 and mild frustration at silent CI failures.*  
Plugin based on the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template).
