# Contributing

Thanks for your interest in improving SkyTimes. This project is a Kotlin Multiplatform app, so small, focused changes are easiest to review and test across Android, iOS/iPadOS, and web.

## Before You Start

- Check existing issues and pull requests before starting larger work.
- Keep pull requests focused on one fix, feature, or cleanup.
- Avoid unrelated formatting or refactors in feature and bug-fix PRs.
- For user-facing changes, describe the behavior clearly and include screenshots when UI changes are visible.

## Development Setup

### Prerequisites

- Android Studio with Kotlin Multiplatform support.
- JDK compatible with the configured Android Gradle Plugin and Kotlin version. CI currently uses Temurin JDK 25.
- Android SDK 36 for Android builds.
- Xcode on macOS for iOS/iPadOS builds.

### Build Commands

Build the Android debug APK:

```bash
./gradlew :androidApp:assembleDebug
```

Run the web development build:

```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Build the web production distribution:

```bash
./gradlew :webApp:wasmJsBrowserDistribution
```

Sync iOS configuration:

```bash
./gradlew syncIosConfig
```

For iOS/iPadOS development, open `iosApp/` in Xcode and run the `iosApp` scheme.

## Technology Stack

- Kotlin 2.4
- Kotlin Multiplatform
- Compose Multiplatform
- Jetpack Compose / Material 3
- Kotlin/Wasm for the web target
- Kotlinx Coroutines
- Kotlinx Serialization
- Kotlinx Datetime
- Ktor Client
- Multiplatform Settings
- AndroidX Glance AppWidget
- AndroidX WorkManager
- AndroidX Navigation 3
- Coil 3
- Gradle with convention build logic

## Project Layout

```text
SkyTimes/
├── app/                         # Shared app composition root, navigation, DI, startup UI
├── androidApp/                  # Android entry point, manifest, widget, Android resources
├── iosApp/                      # iOS/iPadOS Xcode project and Swift entry point
├── webApp/                      # Kotlin/Wasm browser entry point
├── core/                        # Shared platform, data, domain, navigation, onboarding, and UI modules
├── features/                    # Home, quests, reminders, settings, and vault feature modules
├── build-logic/                 # Gradle convention plugin
└── .github/workflows/           # Release automation
```

## Code Guidelines

- Follow the existing module boundaries and naming patterns.
- Keep shared logic in KMP modules when it applies to more than one platform.
- Put Android-only behavior in Android source sets or `androidApp/`.
- Put iOS-only behavior in iOS source sets or `iosApp/`.
- Do not make `core/*` depend on feature modules.
- Keep feature internals private or `internal` unless the app composition root needs them.

## Pull Request Checklist

- The change is scoped to the requested behavior.
- Relevant build commands have been run, or the PR explains why they were not run.
- UI changes include screenshots or screen recordings when practical.
- Documentation is updated when behavior, setup, releases, or platform support changes.
- New public behavior is covered by tests where the project has a suitable test surface.

## Release Notes

For release-facing changes, include a concise note describing what changed for users. Mention platform-specific behavior when a change affects only Android, iOS/iPadOS, or web.
