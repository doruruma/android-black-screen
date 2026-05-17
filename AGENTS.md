# Black Screen Application - Reference & Guidelines

Welcome to the **Black Screen Application** project. This document serves as an exhaustive reference for developer agents and engineers working on the codebase, ensuring consistency, high architectural standards, and compliance with the project's vision and requirements.

---

## 1. About the Application

The **Black Screen Application** is a utility designed to provide a completely black, distraction-free full-screen overlay, simulating a "turned-off" screen state while keeping the device active and displaying essential information.

### Core Features & Interaction Flow
*   **Simulated Off Screen (Full-Screen Overlay):** Renders a complete black overlay across the entire device screen, overlaying even system UI elements (bars, status indicators) where permitted.
*   **Minimalistic Info Overlay:** Inside the black overlay, the UI strictly displays only two components:
    1.  **A live clock/time display** to let the user check the time at a glance.
    2.  **A "Hide Overlay" button** to dismiss the black screen and restore the regular screen state.
*   **Overlay Activation Sources:** The black screen overlay can be triggered from two distinct touchpoints:
    1.  **Main Application UI:** A prominent activation button located on the main screen of the application.
    2.  **System Notification:** A persistent or active notification action button, enabling the user to launch the overlay quickly from anywhere within the OS without opening the main application window.

---

## 2. Tech Stack & Dependencies

The project is built using a modern, reactive Android stack, leveraging Jetpack Compose, Navigation 3, and state-of-the-art libraries. Below is the summarized configuration defined in `libs.versions.toml` and `app/build.gradle.kts`:

### Platform Configuration
*   **Minimum SDK:** 24 (Android 7.0 / Nougat)
*   **Target SDK:** 36 (Android 15+)
*   **Compile SDK:** 36
*   **JVM Toolchain / Java Version:** Java 17

### Core Technologies & Libraries
*   **Language:** Kotlin (v2.3.20) with Kotlin Serialization enabled.
*   **UI Framework:** **Jetpack Compose** using the curated **Material 3** component library and Compose BOM `2026.03.01`.
*   **Navigation:** **Jetpack Navigation 3** (`androidx.navigation3:navigation3-runtime` & `androidx.navigation3:navigation3-ui` v1.0.1) for modern type-safe compose routing using `@Serializable` data objects as keys.
*   **Architecture & Lifecycle:** AndroidX Lifecycle Components (v2.10.0), including:
    *   `lifecycle-runtime-compose` & `lifecycle-viewmodel-compose` for state streams.
    *   `lifecycle-viewmodel-navigation3` for scoped lifecycle support in modern navigation.
*   **Asynchronous Processing:** Kotlin Coroutines (v1.10.2) and Flow APIs for reactive data management.

### Testing Stack
*   **Unit Tests:** JUnit 4 (`junit:junit:4.13.2`) and Kotlinx Coroutines Test for unit testing ViewModels, repositories, and state machines.
*   **Instrumented / UI Tests:** AndroidX Test Core, AndroidX Test Runner, Espresso Core, and Compose UI Test (`androidx.compose.ui:ui-test-junit4` & `ui-test-manifest`) for UI and integration testing.

---

## 3. Coding Guidelines & Standards

To maintain high software quality, all agents and contributors must strictly adhere to these architectural rules:

### Clean Code & Architecture
*   **Clean Code:** Prioritize readability, meaningful variable and function naming, and short, focused methods. Avoid magic numbers and hardcoded strings (use resources or constants).
*   **Single Responsibility Principle (SRP):** Each class, component, and file must serve exactly one dedicated purpose:
    *   **UI Layer (Compose):** Strictly handles layout rendering, receiving state, and delegating user interactions to event handlers.
    *   **ViewModel Layer:** Manages UI states and interacts with domain or data layers. Do not reference Android Views, Contexts, or direct UI elements inside ViewModels.
    *   **Data Layer (Repository):** Standardizes data ingestion and storage, exposing clean, reactive Kotlin Flow API streams to consumers.
    *   **Background Services:** Handle the persistent notification management and the creation/removal of the overlay window safely.

### Documentation (KDoc)
*   **Mandatory Comments:** Every public/internal class, interface, method, helper function, and custom Composable function **must** be fully documented using the KDoc format (triple slash `/** ... */`).
*   **Clear Context:** The documentation should explain *why* a component exists, details about its parameters (using `@param`), and what it returns (using `@return`).
*   **Example format:**
    ```kotlin
    /**
     * Renders the full-screen black overlay containing the clock and dismiss controls.
     *
     * @param currentTime A string representation of the current live time to display.
     * @param onDismiss Invoked when the user triggers the "Hide Overlay" action.
     * @param modifier Modifier applied to the parent overlay container.
     */
    @Composable
    fun BlackScreenOverlay(
        currentTime: String,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
    ) { ... }
    ```

### Testing Requirements
*   **Unit Testing First:** Any new business logic, state emission, repository flow, or view model integration must be covered by comprehensive JUnit unit tests.
*   **Coroutine Scoping:** Use `runTest` and test dispatchers when verifying asynchronous code to prevent flaky tests.
*   **Clean Assertions:** Use explicit, human-readable assertions, validating state changes, state flow transitions, and error handling paths.
