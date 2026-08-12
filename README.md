This is a Kotlin Multiplatform project targeting Android, iOS, Web.

The project is split into Gradle modules by concern:

* [/app](./app) — composition root: `App()`, manual DI (`AppContainer` extending `CoreContainer`), navigation, and the iOS framework (`Shared`) consumed by the Xcode project.
* [/core](./core) — shared business logic, common UI components, theme, and utils (repositories, startup, onboarding framework, compose resources).
* [/feature](./feature) — feature modules (`home`, `quests`, `settings`, `reminders`, `vault`), each owning its screens, ViewModels and feature-specific logic.
* [/iosApp](./iosApp) — the iOS application entry point (SwiftUI wrapper around the shared Compose UI).
* [/androidApp](./androidApp) — the Android application entry point + Glance widget.
* [/webApp](./webApp) — the web entry point.

Dependency direction: `core` ← `feature/*` ← `app` ← `androidApp`/`webApp`/`iosApp`.

### Running the apps

- Android app: `./gradlew :androidApp:assembleDebug`
- Web app:
  - Wasm target (faster, modern browsers): `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
  - JS target (slower, supports older browsers): `./gradlew :webApp:jsBrowserDevelopmentRun`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there (first run `./gradlew syncIosConfig`).

### Running tests

Tests run per Gradle module (no test sources exist yet):

- Android host tests: `./gradlew :core:testAndroidHostTest` (or `:app:`, `:feature:*`)
- Web tests: `./gradlew :core:jsTest` / `:core:wasmJsTest` (or `:app:`, `:feature:*`)
- iOS tests: `./gradlew :core:iosSimulatorArm64Test` (macOS only; requires the iOS simulator)

### Adding a new feature

1. Create `feature/<name>/build.gradle.kts` applying `alias(conventions.plugins.skytimes.kmp.library)` (see `build-logic/`).
2. Add `include(":feature:<name>")` to `settings.gradle.kts`.
3. Add the module as a dependency of `:app` (and of other features that need it).
