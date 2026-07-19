---
name: gametime-kmp
description: "Use when: working on the GameTime/SkyTimes Kotlin Multiplatform app — adding screens, navigation routes, platform-specific code (expect/actual), repositories, DI wiring, reminders, onboarding flows, theme settings, or understanding project architecture and conventions. Covers Compose Multiplatform, Navigation 3, multiplatform-settings, kotlinx.datetime/serialization, Ktor, Coil, and Material Kolor."
argument-hint: "[task] — what you want to build or understand in the GameTime KMP project"
---

# GameTime / SkyTimes — KMP Project Skill

## Project Overview

GameTime (package `com.imnaiyar.skytimes`) is a **Kotlin Multiplatform** application with **Compose Multiplatform** shared UI. It tracks in-game events (Geyser, Grandma, Turtle, etc.) with countdown timers, reminders, and quests for the game *Sky: Children of the Light*.

### Targets

| Target | Module | UI Framework |
|--------|--------|-------------|
| Android | `androidApp/` | Jetpack Compose (Android) |
| iOS | `iosApp/` | SwiftUI wrapper → shared Compose |
| Web (JS) | `webApp/` | Compose for Web (JS target) |
| Web (Wasm) | `webApp/` | Compose for Web (Wasm target) |

All shared logic lives in `shared/` with source sets: `commonMain`, `androidMain`, `iosMain`, `jsMain`, `wasmJsMain`.

---

## Architecture at a Glance

```
commonMain/kotlin/com/imnaiyar/skytimes/
├── App.kt                  # Root composable + theme/startup wiring
├── Platform.kt             # expect declaration
├── constants/              # EventData model, EventKey enum, API URLs, timezone
│   ├── Common.kt
│   └── Event.kt
├── di/                     # Manual DI container
│   ├── AppContainer.kt
│   └── LocalAppContainer.kt
├── nav/                    # Navigation 3 routes + NavDisplay
│   ├── AppNavigation.kt
│   └── Routes.kt
├── onboarding/             # Tutorial/onboarding system
│   ├── TutorialManager.kt
│   ├── TutorialDefinition.kt
│   ├── TutorialHost.kt
│   └── AppTutorial.kt
├── reminders/              # Cross-platform reminder scheduling
│   ├── Reminder.kt         # ReminderScheduler interface + expect funs
│   ├── ReminderRepository.kt
│   ├── NoOpReminderScheduler.kt
│   └── ui/                 # Shared reminder flow UI
├── repositories/           # Data layer (StateFlow-backed)
│   ├── SettingsRepository.kt
│   ├── QuestRepository.kt
│   └── ClockTickerRepository.kt
├── screens/                # Full-screen composables
│   ├── Screen.kt           # Screen enum
│   ├── Home.kt, MainScreen.kt, Quests.kt
│   ├── Settings.kt, ThemeSetting.kt
│   └── SplashScreen.kt, Shards.kt
├── startup/                # App initialization
│   ├── AppInitializer.kt
│   ├── StartupTask.kt
│   └── AppState.kt
├── theme/                  # Material 3 + material-kolor
│   ├── Theme.kt
│   └── Typography.kt
├── ui/                     # Reusable composable components
│   ├── ActionBar.kt, Card.kt, ClockDisplay.kt, Grid.kt
│   ├── Image.kt, Loading.kt, Switch.kt, Timer.kt, ...
├── utils/                  # Utility objects
│   └── EventTimeUtils.kt   # Event occurrence calculation
├── vault_archive/          # Vault archive feature
└── views/                  # ViewModels
    ├── AppViewModel.kt
    ├── SettingsViewModel.kt
    └── QuestsViewModel.kt
```

---

## Key Patterns & Conventions

### 1. expect/actual for Platform Code

Declare an `expect` function or composable in `commonMain`, provide `actual` in each platform source set.

**Example — Platform identity:**
- `commonMain/Platform.kt`: `expect fun getPlatform(): Platform`
- `androidMain/Platform.android.kt`: `actual fun getPlatform(): Platform = AndroidPlatform()`
- `iosMain/Platform.ios.kt`: `actual fun getPlatform(): Platform = IOSPlatform()`

**Example — Composable expect (Android needs Activity context):**
- `commonMain/reminders/Reminder.kt`: `@Composable expect fun rememberNotificationPermissionRequester(): ...`
- Android `actual` wraps Activity result APIs; iOS `actual` is simpler (no Activity needed).

**When adding platform-specific code:**
1. Define the interface/expect in `commonMain`
2. Add `actual` in each platform source set
3. For Android-only needs, consider a default no-op in common and override only in `androidMain`

### 2. Manual DI via AppContainer

`AppContainer` (in `di/AppContainer.kt`) is the single DI container. It:
- Creates all repositories and schedulers
- Holds the shared `CoroutineScope` (`applicationScope`)
- Exposes factory methods for ViewModels (`createAppViewModel()`, `createSettingsViewModel()`, etc.)
- Uses `CompositionLocalProvider` in `App.kt` to provide `LocalAppContainer`

**When adding a new dependency:**
1. Add the repository/scheduler as a property in `AppContainer`
2. Provide it via `LocalAppContainer` if needed in composables
3. Create ViewModel factories if the screen needs one

### 3. Repository Pattern (StateFlow)

Repositories expose state via `StateFlow<DataType>` and mutate through suspend functions with a `Mutex` lock.

**Pattern from `SettingsRepository`:**
```kotlin
class SettingsRepository(...) {
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    private val updateMutex = Mutex()

    suspend fun updateTheme(mode: ThemeMode) {
        update { current -> current.copy(themeMode = mode) }
    }

    private suspend inline fun update(transform: (T) -> T) {
        updateMutex.withLock {
            val next = transform(_settings.value)
            saveChangedSettings(...)
            _settings.value = next
        }
    }
}
```

Key principles:
- **Persist before publish** — never emit state that isn't durable
- **Mutex-guarded writes** — prevents race conditions
- **Immutable data classes** — use `copy()` for updates

### 4. Navigation 3 with Typed Routes

Uses `compose-navigation3` (`1.1.1`). Routes are typed `NavKey` subclasses defined in `nav/Routes.kt`.

**Key files:**
- `nav/Routes.kt` — Route definitions (`MainRoute`, `VaultRoute`, `ThemeSettingsRoute`)
- `nav/AppNavigation.kt` — `NavDisplay` with `entryProvider`, slide animations, and `SavedStateConfiguration` with polymorphic serializer registration

**When adding a new screen:**
1. Define a new route class extending `AppRoute` (or `NavKey`) in `Routes.kt`
2. Register it in the polymorphic `SerializersModule` in `AppNavigation.kt`
3. Add an `entry<YourRoute>` block in `entryProvider`
4. Add navigation trigger in the parent screen

### 5. Reminder Scheduling (Cross-Platform)

> See [references/reminders.md](./references/reminders.md) for full details.

- **Interface**: `ReminderScheduler` in `commonMain/reminders/Reminder.kt`
- **Factory**: `expect fun getReminderSchedular(...)` — each platform provides its `actual`
- **Android**: Uses `AlarmManager` + `ReminderAlarmReceiver` + `ReminderBootReceiver`; two-tier permission (notification + exact alarm)
- **iOS**: Uses `UNUserNotificationCenter` + `BGAppRefreshTask`; rolling window of up to 64 notifications
- **Fallback**: `NoOpReminderScheduler` for web targets
- **Permission**: `rememberNotificationPermissionRequester()` is an `@Composable expect` because Android needs Activity context
- **Shared UI**: `ReminderFlowController` manages the full flow — permission checks, offset dialog, exact alarm disclaimer (Android), save/remove

### 6. Startup System

`AppInitializer` runs a list of `StartupTask` implementations sequentially. Each task has:
- `name: String`
- `critical: Boolean` — if true, failure blocks the app
- `suspend fun initialize()`

`AppState` sealed class: `Loading`, `Ready`, `Error(message)`. The `App` composable switches on this state.

### 7. Onboarding / Tutorial System

> See [references/onboarding.md](./references/onboarding.md) for full details.

A reusable, screen-agnostic coaching-mark framework:
- **`TutorialStep`** — Interface implemented by the app's enum (`AppTutorialStep`); each step declares a `targetId`
- **`TutorialFlow`** — Ordered list of step definitions with a unique flow `id`
- **`TutorialManager`** — State machine managing active flow, step progression, and completion persistence
- **`TutorialTarget`** — Composable wrapper that registers bounds in root coordinates for the overlay
- **`TutorialHost`** + **`TutorialLifecycle`** — Root composable that auto-starts after a configurable delay
- **Persistence** — Via `TutorialProgressRepository` (implemented by `SettingsRepository`); completion is derived, so new steps auto-activate
- **Adding steps** — Add enum value + `TutorialDefinition` + `TutorialTarget` wrapper; no framework changes needed

### 8. Theme System

- `ThemeMode` enum: `LIGHT`, `DARK`, `SYSTEM`
- `material-kolor` generates dynamic color schemes from a seed color
- `ThemeController` manages theme state in `AppContainer`
- `AppTheme` composable wraps Material 3 with the computed scheme

---

## Common Workflows

### Adding a New Screen

1. **Create the screen composable** in `screens/YourScreen.kt`
2. **Add route** in `nav/Routes.kt`:
   ```kotlin
   @Serializable
   data object YourRoute : AppRoute()
   ```
3. **Register in navigation** in `nav/AppNavigation.kt`:
   - Add to `SerializersModule` polymorphic block
   - Add `entry<YourRoute>` in `entryProvider`
4. **Wire navigation trigger** from the parent screen (e.g., add button in `MainScreen.kt`)
5. **Create ViewModel** (if needed) in `views/YourViewModel.kt` and add factory to `AppContainer`
6. **Add to `Screen` enum** if it's a top-level tab destination

### Adding a New Repository

1. Create `repositories/YourRepository.kt`
2. Follow the StateFlow + Mutex pattern (see SettingsRepository)
3. If it needs initialization, implement `StartupTask`
4. Add it to `AppContainer` and the `AppInitializer` task list (if StartupTask)
5. Provide through `LocalAppContainer` if composables need direct access

### Adding a New Event

1. Add a new `EventKey` enum entry in `constants/Event.kt`
2. Add the `EventData` entry in the `events` list
3. Configure `offset`, `duration`, `interval`, `occursOn` rules as needed
4. The `EventTimeUtils` will automatically pick it up for time calculations

### Adding a New Tutorial / Onboarding Step

1. Add enum value to `AppTutorialStep` with unique `targetId` and relevant `Screen`
2. Add `TutorialDefinition` to the appropriate flow in `AppTutorial.kt` (or create a new `TutorialFlow`)
3. Wrap the target UI element with `TutorialTarget(id = AppTutorialStep.YourStep.targetId) { ... }`
4. If new flow: add it to `TutorialManager`'s `flows` list in `AppContainer`
5. Done — completion is derived, so the new step activates automatically. See [references/onboarding.md](./references/onboarding.md).

### Adding Platform-Specific Code

1. Define `expect` declaration in `commonMain`
2. Add `actual` implementations in each platform source set:
   - `androidMain/kotlin/com/imnaiyar/skytimes/`
   - `iosMain/kotlin/com/imnaiyar/skytimes/`
   - `jsMain/kotlin/com/imnaiyar/skytimes/`
   - `wasmJsMain/kotlin/com/imnaiyar/skytimes/`
3. Wire through `AppContainer` or `LocalAppContainer` as appropriate

### Adding a Dependency

1. Add version to `gradle/libs.versions.toml` under `[versions]` and `[libraries]`
2. Add to the appropriate source set in `shared/build.gradle.kts`:
   - `commonMain.dependencies` — shared code
   - `androidMain.dependencies` — Android only
   - `iosMain.dependencies` — iOS only
   - `jsMain.dependencies` / `wasmJsMain.dependencies` — web targets
3. Sync Gradle

---

## Key Libraries

| Library | Usage |
|---------|-------|
| `kotlinx.datetime` | Timezone-aware date/time, `Instant`, `LocalDate`, `DateTimeUnit` |
| `kotlinx.serialization` | JSON serialization, route state persistence |
| `multiplatform-settings` | Key-value persistence (no-arg + coroutines) |
| `Ktor` | HTTP client (OkHttp on Android, Darwin on iOS, JS fetch on web) |
| `Coil` | Cross-platform image loading |
| `material-kolor` | Dynamic Material You color generation |
| `compose-navigation3` | Type-safe navigation with serializable routes |
| `reorderable` | Drag-to-reorder lists |

---

## Important Conventions

- **Timezone**: All game events use `America/Los_Angeles` (Pacific Time, where TGC is based)
- **Time calculations**: Always use `kotlin.time.Instant` and `kotlin.time.Duration` through `EventTimeUtils`
- **Settings defaults**: `notificationsEnabled` defaults to `false`; reminders route through shared `ReminderFlowController`
- **File naming**: One class/enum per file generally; UI composables grouped by feature
- **Coroutine scope**: Use `AppContainer.applicationScope` for long-lived coroutines; `rememberCoroutineScope()` for composable-scoped work
- **State observation**: Always use `.collectAsState()` in composables, never collect manually
- **Vault archive**: Currently a placeholder screen; see [references/vault-archive.md](./references/vault-archive.md) for build-out plan

## Reference Docs

For deep dives into specific subsystems:
- [Onboarding / Tutorial System](./references/onboarding.md) — TutorialStep, TutorialFlow, TutorialManager, TutorialTarget, adding steps
- [Reminder System](./references/reminders.md) — ReminderScheduler, platform implementations, ReminderFlowController, permission flow
- [Vault Archive](./references/vault-archive.md) — Current placeholder state and build-out plan
