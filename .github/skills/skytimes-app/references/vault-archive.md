# Vault Archive Feature

## Status: Placeholder / Work-in-Progress

The vault archive feature is currently a **placeholder screen**. It has a route and navigation wired up but contains only stub UI.

## Current Implementation

### Route (`nav/Routes.kt`)
```kotlin
@Serializable
data object VaultRoute : AppRoute()
```

### Navigation (`nav/AppNavigation.kt`)
Registered in the polymorphic `SerializersModule` and has an `entry<VaultRoute>` block that renders `MainArchive()`.

### Screen (`vault_archive/Main.kt`)
```kotlin
@Composable
fun MainArchive() {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = "This will be the Vault Archive in It's Glory",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
```

### Navigation Trigger
In `MainScreen`, a button navigates to `VaultRoute` via the `onOpenVault` callback.

## Planned Implementation Notes

When building out the vault archive:
- **Package**: `com.imnaiyar.skytimes.vault_archive` — already created
- **Navigation**: Already wired — just replace `MainArchive()` content
- **Data source**: Likely needs a new repository or could use existing data from the SkyHelper API
- **Pattern to follow**: Create a ViewModel in `views/`, add factory to `AppContainer`, use `StateFlow` for state
- **Screen**: Replace the placeholder with real UI following existing screen patterns (see `screens/Home.kt`, `screens/Quests.kt`)
- **If it needs API calls**: Use Ktor client (already in `commonMain.dependencies`)
- **If it needs persistence**: Follow `SettingsRepository` pattern with `multiplatform-settings` or add a new storage mechanism
