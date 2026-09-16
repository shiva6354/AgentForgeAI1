# AgentForge AI — Android Studio Project (Kotlin & Jetpack Compose)

This is the complete, standalone Android Studio project for **AgentForge AI**, a mobile-first AI coding assistant powered by Gemini 3.8 Flash.

---

## 🛠️ Requirements & Tooling

| Requirement | Supported Version |
| :--- | :--- |
| **Android Studio** | Ladybug (2024.2.1+) or Koala / Iguana / Hedgehog |
| **JDK** | Java 17 or Java 21 (bundled in Android Studio) |
| **Android Gradle Plugin** | 8.7.3 |
| **Kotlin** | 2.0.21 (with Compose Compiler 2.0.21) |
| **Min SDK** | API 26 (Android 8.0 Oreo) |
| **Target / Compile SDK** | API 35 (Android 15) |

---

## 🚀 Step-by-Step Android Studio Setup

### 1. Open the Project in Android Studio
1. Launch **Android Studio**.
2. Click **Open** (or `File -> Open...`).
3. Select the `android` directory of this repository (the folder containing `settings.gradle.kts` and `app`).
4. Wait for Gradle Sync to complete. If prompted to trust the project, click **Trust Project**.

### 2. Verify JDK Configuration
1. Go to `Settings` (or `Preferences` on macOS) -> `Build, Execution, Deployment` -> `Build Tools` -> `Gradle`.
2. Under **Gradle JDK**, ensure **jbr-17** or **jbr-21** (JetBrains Runtime) is selected.

### 3. Run the Backend Proxy
The Android app communicates with the Gemini API through the local proxy server (running on port `3000`), ensuring your Gemini API key is never exposed in the APK:
```bash
npm run dev
```
- In the **Android Studio Emulator**, the app automatically connects to `http://10.0.2.2:3000` (which routes to your host machine's port 3000).
- If running on a **physical device over Wi-Fi**, open **Settings** inside the app and set the Backend URL to your machine's LAN IP (e.g. `http://192.168.1.50:3000`).

### 4. Build and Run
- Select `app` in the run configuration dropdown.
- Select your target device or emulator.
- Press **Shift + F10** (or click the green **Run ▶** button).

---

## 📂 Project Architecture

```
android/
├── settings.gradle.kts          # Module and repository configuration
├── build.gradle.kts             # Top-level Gradle plugins
├── gradle.properties            # JVM args & AndroidX enablement
└── app/
    ├── build.gradle.kts         # App dependencies (Compose BOM, Retrofit, Datastore)
    ├── proguard-rules.pro       # Release obfuscation & serialization rules
    └── src/
        └── main/
            ├── AndroidManifest.xml
            ├── res/
            │   └── values/
            │       ├── colors.xml
            │       └── strings.xml
            └── java/com/agentforge/ai/
                ├── MainActivity.kt               # Single Activity Compose entry point
                ├── data/
                │   ├── model/Models.kt           # Project, File, Plan, Check, AutoFix models
                │   ├── remote/GeminiApiService.kt# Retrofit & OkHttp client for backend proxy
                │   └── local/ProjectFileManager.kt# Sandboxed project file storage & ZIP exporter
                ├── domain/
                │   └── repository/GeminiRepository.kt # Coroutines repository & network error handler
                ├── navigation/
                │   ├── Screen.kt                 # Sealed routes & navigation paths
                │   └── NavGraph.kt               # Bottom navigation & screen composables
                ├── ui/
                │   ├── theme/                    # Material 3 dark/light colors, typography
                │   ├── components/               # Common buttons, cards, code blocks, loading
                │   ├── home/HomeScreen.kt        # Dashboard & quick actions
                │   ├── chat/ChatScreen.kt        # AI coding chat with voice input & code blocks
                │   ├── builder/BuilderScreen.kt  # Prompt-to-app generator (Android & Web)
                │   ├── agent/AgentScreen.kt      # 6-step autonomous pipeline with cancel loop
                │   ├── projects/ProjectsScreen.kt# Project list, duplicate, share, and ZIP export
                │   ├── editor/CodeEditorScreen.kt# Mobile code editor with Gemini Auto-Fix
                │   ├── preview/PreviewScreen.kt  # Live WebView sandbox with console logs
                │   └── settings/SettingsScreen.kt# Backend URL config & security health check
                └── utils/
                    └── VoiceInputManager.kt      # Native Android SpeechRecognizer integration
```

---

## 🔒 Security Guarantee
- **Gemini API Key:** Handled solely by the Express backend proxy (`GEMINI_API_KEY`).
- **Zero API Key Leakage:** No keys are stored in `local.properties`, `BuildConfig`, or the compiled APK.
- **Sandboxed File Access:** All user projects operate in private application internal storage (`context.filesDir/agentforge_projects`).
