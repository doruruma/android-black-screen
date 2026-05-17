# Black Screen Application

[![Android Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-v2.3.20-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue.svg?style=flat)](https://developer.android.com/about/versions/15)
[![UI Framework](https://img.shields.io/badge/Compose-BOM%202026.03.01-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

A clean, modern utility application designed to provide a simulated "turned-off" screen overlay while keeping the Android device active and displaying essential information.

---

## 1. Application Goals

The **Black Screen Application** is a specialized utility that helps block distractions, prevent screen burn-in on OLED displays, or save battery while keeping tasks active in the background.

### Core Features
*   **Simulated Turned-Off Screen (Full-Screen Overlay):** Draws a completely black, immersive overlay that covers the entire screen, including system bars, simulating a turned-off screen state.
*   **Minimalist Interactive Components:** The overlay displays only two essential components:
    1.  **A Live Clock:** Displays the current live time ticking in real-time (formatted as `HH:mm:ss`).
    2.  **A "Hide Overlay" Button:** Instantly dismisses the overlay and returns the screen to its original state.
*   **Flexible Overlay Activation:**
    1.  **Main Application Screen:** A clear, elegant control dashboard with status indicators and start/stop controls.
    2.  **Persistent System Notification:** An active status bar notification action button, allowing you to activate/toggle the overlay quickly from anywhere inside the operating system without opening the app dashboard.

---

## 2. Tech Stack

The application leverages a modern, state-of-the-art Android development stack, emphasizing reactive flows, clean architecture, and type-safety:

| Component | Library / Version | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin `2.3.20` | Core programming language |
| **Min / Target SDK** | Min SDK `24` / Target SDK `36` | Operating system compatibility |
| **UI Framework** | Jetpack Compose (BOM `2026.03.01`) | Declarative, modern UI design |
| **Design System** | Material 3 | Curated layout and typography components |
| **Navigation** | Jetpack Navigation 3 (`1.0.1`) | Type-safe Composable-based routing |
| **State & Lifecycle** | AndroidX Lifecycle (`2.10.0`) | Scoped lifecycles and ViewModels |
| **Concurrency** | Kotlin Coroutines & Flow | Asynchronous time tracking and service states |
| **Service Control** | Foreground Service (`specialUse`) | Managing persistent status and overlay layouts |
| **Testing** | JUnit 4, Instrumented Compose UI Test | Solid test suites for screens and states |

---

## 3. How to Build the APK

The project uses Gradle Wrapper to manage dependencies and build targets. Follow the steps below to compile the project and build the APK.

### Prerequisites
*   Java Development Kit (JDK) 17 installed
*   Android SDK command-line tools or Android Studio installed

### Building Debug APK
The debug APK compiles quickly, is pre-signed with a debug key, and is ready to be loaded onto emulators or test devices.

1.  Open your terminal or PowerShell in the project root directory.
2.  Run the build task:
    *   **On Windows (PowerShell/CMD):**
        ```powershell
        .\gradlew.bat assembleDebug
        ```
    *   **On macOS / Linux:**
        ```bash
        ./gradlew assembleDebug
        ```
3.  Once the build completes successfully, find the generated APK at:
    ```
    app/build/outputs/apk/debug/app-debug.apk
    ```

### Building Release APK (Unsigned)
To generate the production-ready build for publishing, compile the release version.

1.  Run the release build task:
    *   **On Windows (PowerShell/CMD):**
        ```powershell
        .\gradlew.bat assembleRelease
        ```
    *   **On macOS / Linux:**
        ```bash
        ./gradlew assembleRelease
        ```
2.  Once the build completes, find the release APK at:
    ```
    app/build/outputs/apk/release/app-release-unsigned.apk
    ```

### Installing the APK on a Device
If you have a physical device or emulator connected with ADB (Android Debug Bridge) enabled:

```bash
# Install the debug build directly onto your connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```
