# SkyTimes (GameTime)

Kotlin Multiplatform + Compose Multiplatform app showing daily game times (events/quests/shards/clock) for Sky: Children of the Light. Targets Android, iOS, Web (JS + Wasm); Android Glance widget. Package root: `com.imnaiyar.skytimes`.

## Project
- Stack: Kotlin 2.4.0, Compose Multiplatform 1.11.1, AGP 9.0.1, Gradle 9.1, Material3, Ktor 3, kotlinx-serialization, Navigation3, multiplatform-settings, material-kolor.
- Modular KMP: `core/` ← `feature/*` ← `app/` ← `androidApp`/`webApp`/`iosApp`. All KMP library modules (`app`, `core`, `feature/*`) apply the `skytimes.kmp.library` convention plugin from `build-logic/` (targets: android library, iosArm64, iosSimulatorArm64, js, wasmJs; only `:app` produces the iOS framework, baseName `Shared`).
- `app/` is the composition root: `App()`, `AppContainer` (extends `CoreContainer`), nav. `core/` holds business logic, common UI, theme, utils, compose resources. Each `feature/<name>/` owns its screens, ViewModels and feature-specific logic.
- Version catalog: `gradle/libs.versions.toml`. App id/name/version come from `gradle.properties` (`app.id`, `app.name`, …) — read via `project.findProperty`, synced to iOS `iosApp/Configuration/Config.xcconfig` by the root `syncIosConfig` task.

## Commands
- Android debug APK: `./gradlew :androidApp:assembleDebug`
- Android release: `./gradlew :androidApp:assembleRelease` (signing via env `KEYSTORE_FILE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`; see `.github/workflows/release.yml`)
- Web dev server: `./gradlew :webApp:wasmJsBrowserDevelopmentRun` (Wasm) / `:webApp:jsBrowserDevelopmentRun` (JS)
- iOS: build/run from Xcode (`iosApp/`, links the `Shared` framework from `:app`); sync versions first with `./gradlew syncIosConfig`
- Tests (no test sources exist yet): `./gradlew :core:testAndroidHostTest` (or `:app:`/`:feature:*:`), `:core:jsTest`, `:core:wasmJsTest`, `:core:iosSimulatorArm64Test` (macOS only)
- Gradle properties: `org.gradle.configuration-cache=true`; JVM 11 target for Android.

## Architecture
- `core/src/commonMain/kotlin/com/imnaiyar/skytimes/` — `constants/` (EventKey/EventData), `repositories/` (`SettingsRepository`, `ClockTickerRepository`), `startup/` (`AppInitializer`, `StartupTask`, `AppState`), `theme/` (`AppTheme`, `ThemeController`), `ui/` shared components, `utils/`, `onboarding/` framework, `views/AppViewModel`, `di/CoreContainer` + `LocalCoreContainer`/`LocalTutorialManager`, `home/HomeScreens` enum, `Platform` expect + actuals, `composeResources/` (drawables/fonts — features use them via `skytimes.core.generated.resources`).
- `feature/` — `home/` (MainScreen tab host, Splash, skytimes/*, Shards), `quests/` (QuestsScreen, QuestsViewModel, QuestRepository, VideoPlayer expect/actuals), `settings/` (SettingsScreen, ThemePage, SettingsViewModel), `reminders/` (ReminderScheduler expect/actuals, ReminderRepository, ReminderFlowController, ContextHolder androidMain), `vault/` (VaultArchive placeholder).
- `app/src/commonMain/kotlin/com/imnaiyar/skytimes/` — `App.kt` (startup state machine + theme + reminder refresh wiring), `di/AppContainer` (extends `CoreContainer`; creates feature ViewModels), `nav/` (Navigation3 routes + `AppNavigation`), `onboarding/AppTutorial.kt`. `iosMain/MainViewController.kt` is the iOS entry.
- `androidApp/` — `MainActivity` + Glance widget (`widgets/`); depends on `projects.app` + `projects.core` + `projects.feature.reminders`. `webApp/src/webMain/.../main.kt` is the web entry.

## Conventions
- expect/actual per platform: `Platform.kt`, `VideoPlayer.kt`, `Reminder.kt` have common expect + android/ios/js/wasmJs actuals in the module that owns them.
- Compose Multiplatform UI, Material3, no XML layouts. `@OptIn(ExperimentalMaterial3Api::class)` used broadly.
- Manual DI only (CoreContainer/AppContainer + CompositionLocal) — no Koin/Hilt. Screens receive ViewModels/dependencies as plain parameters from the composition root (`AppNavigation`); features never read `AppContainer`.
- Repository state: immutable data class + `StateFlow`; expose with `.asStateFlow()`.
- Result modeling: sealed interfaces for load results (e.g. `QuestLoadResult.Success/Failure/RefreshSkipped`); `Result`/`runCatching` + `Throwable.userMessage()` for user-facing errors; always rethrow `CancellationException`.
- Networking: Ktor with per-platform engine (OkHttp/Darwin/JS), `Json { ignoreUnknownKeys = true }`; feature-level repositories keep their engine deps in their own module.
- Routes are `@Serializable`; navigation3 `subclass(...)` registration for polymorphic routes.
- Follow the version catalog for all deps (`libs.*`); type-safe project accessors enabled (`projects.feature.quests`, …).
- Android: minSdk 33, compileSdk/targetSdk 36.

## Notes
- No tests yet — test source sets are empty.
- Sandbox quirks (read-only `~/.gradle`, `~/.konan`, `~/.kotlin`): use `GRADLE_USER_HOME=/home/nyr/.cache/gradle-home` (has `kotlin.user.home` property), `KONAN_DATA_DIR=/home/nyr/.cache/konan`, and the gradle dist at `/home/nyr/.cache/gradle-home/gradle-dist/bin/gradle`.
- `kotlin-js-store/` holds the yarn lockfile for web (npm) dependencies.
