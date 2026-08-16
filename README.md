This is a Kotlin Multiplatform project targeting Android, iOS and Web (Wasm), modularized into
a thin composition root, reusable core modules, and isolated feature modules.

## Project structure

```
app/                         # Composition root: App.kt, AppContainer, navigation graph, startup UI
├── core/
│   ├── common/               # Generic utilities, Platform abstraction, startup framework
│   ├── domain/               # Game event models + time calculations (EventData, EventTimeUtils)
│   ├── data/                 # Shared data infrastructure (ClockRepository)
│   ├── ui/                   # Shared Compose components, theme/design system, resources
│   ├── navigation/           # Navigation routes, AppTab, tutorial step contract
│   └── onboarding/           # Reusable coach-mark/tutorial framework
├── features/
│   ├── home/                 # Main screen, SkyTimes event grid, shards tab
│   ├── quests/               # Quests data, ViewModel and tab UI (+ video player)
│   ├── settings/
│   │   ├── api/              # SettingsRepository, SettingsViewModel, ThemeController, tab UI
│   │   └── implementation/   # Theme settings page
│   ├── reminders/
│   │   ├── api/              # Reminder contracts, repositories, reminder flow UI
│   │   └── implementation/   # Platform schedulers (Android AlarmManager, iOS UserNotifications)
│   └── vault/                # Vault archive screen
├── androidApp/               # Android entry point, widget, platform wiring
├── webApp/                   # Wasm entry point
├── iosApp/                   # iOS entry point (Xcode project, imports the Shared framework)
└── build-logic/              # Convention plugin `skytimes.kmp.library` (typesafe-conventions)
```

## Architecture rules

- `core/*` must never depend on a feature.
- Features depend on `core/*` and, when necessary, on other features' **api** modules only.
- `app` is the composition root: it owns `AppContainer`, provides all `CompositionLocal`s and
  assembles the navigation graph.
- Feature internals are `internal`; only entries required by the application are public.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- Web app:
  - Wasm target (faster, modern browsers): `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.
  The Xcode build invokes `./gradlew :app:embedAndSignAppleFrameworkForXcode` and imports the
  `Shared` framework produced by the `:app` module.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).