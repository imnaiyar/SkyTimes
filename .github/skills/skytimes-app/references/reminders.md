# Reminder System

## Overview

Cross-platform event reminder scheduling. The shared `commonMain` defines the interface and flow controller; each platform provides its own scheduling implementation.

## Architecture

```
commonMain/reminders/
├── Reminder.kt              # ReminderScheduler interface, Reminder data class, expect funs
├── ReminderRepository.kt    # Persistence for reminder configs (Settings-backed)
├── NoOpReminderScheduler.kt # Fallback for web targets
└── ui/
    └── ReminderFlow.kt      # ReminderFlowController — shared UI logic

androidMain/reminders/
├── AndroidReminderScheduler.kt  # AlarmManager-based scheduling
├── BroadcastReceiver.kt         # AlarmReceiver + BootReceiver
└── ContextHolder.kt             # Static context holder for receivers

iosMain/reminders/
└── IosReminderScheduler.kt      # UNUserNotificationCenter-based scheduling
```

## Core Interface: ReminderScheduler

```kotlin
interface ReminderScheduler {
    suspend fun refresh()              // Reschedule all enabled reminders
    suspend fun scheduleReminder(reminder: Reminder)
    suspend fun cancelReminder(eventId: String)
    suspend fun cancelAll()
    suspend fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
    fun hasExactAlarm(): Boolean       // Android-specific
    fun requestExactAlarm(): Unit      // Android-specific
}
```

## Reminder Data Model

```kotlin
@Serializable
data class Reminder(
    val id: String,           // Typically the EventKey name
    val eventId: EventKey,    // Which event this is for
    val enabled: Boolean,
    val offsetMinutes: Int,   // How many minutes before the event to fire (0-15)
)
```

## Platform Implementations

### Android (`AndroidReminderScheduler`)

- **Scheduling**: `AlarmManager.setExact()` with `PendingIntent` → `ReminderAlarmReceiver`
- **Permission model**: Two-tier — notification permission (runtime) + exact alarm permission (system setting)
- **Notification permission**: Checked via `NotificationManagerCompat.areNotificationsEnabled()`; requested via Activity result launcher (hence `rememberNotificationPermissionRequester` is a `@Composable expect`)
- **Exact alarm**: Checked via `AlarmManager.canScheduleExactAlarms()`; user must grant in system settings
- **Boot persistence**: `ReminderBootReceiver` reschedules all reminders on device reboot
- **Channel**: Uses a dedicated notification channel

### iOS (`IosReminderScheduler`)

- **Scheduling**: `UNUserNotificationCenter` with `UNTimeIntervalNotificationTrigger`
- **Rolling window**: Schedules up to `MAX_PENDING_REQUESTS` (64) notifications ahead; refreshes the window as old notifications fire
- **Permission**: Single-step via `UNUserNotificationCenter.requestAuthorization()`
- **Background refresh**: Uses `BGAppRefreshTask` to reschedule when app is backgrounded

### Web (`NoOpReminderScheduler`)

All methods are no-ops. Web targets don't support local notifications.

## expect/actual Declarations

Two `expect` declarations in `commonMain/reminders/Reminder.kt`:

1. **`expect fun getReminderSchedular(...)`** — Factory that returns the platform-specific `ReminderScheduler`. Android wires in `Context`; iOS wires in `UNUserNotificationCenter`.

2. **`@Composable expect fun rememberNotificationPermissionRequester()`** — Returns a callback-based permission requester. Android needs this because `rememberLauncherForActivityResult` requires `@Composable` context and Activity. iOS could inline it, but the expect keeps a consistent API.

## ReminderFlowController (Shared UI)

`ReminderFlowController` in `reminders/ui/ReminderFlow.kt` is the shared UI controller for the reminder setup flow. It manages:

| Dialog/State | Purpose |
|---|---|
| `ReminderOffsetDialog` | Lets user pick 0-15 minute offset before event |
| `ExactAlarmDialog` | Android-only: prompts user to grant exact alarm permission |
| Notification permission | Delegates to platform-specific `rememberNotificationPermissionRequester` |
| Save/Remove | Persists via `ReminderRepository`, schedules via `ReminderScheduler` |

### Flow Sequence

```
User taps reminder icon on event
  → requestReminderEditor(eventData)
    → ensureNotificationPermission()    // Ask if not granted
      → [Android] promptExactAlarmIfNeeded()  // Check exact alarm
        → Show ReminderOffsetDialog
          → On confirm: saveReminder() → upsert + schedule + enable notifications
          → On remove: removeReminder() → cancel + remove from repo
```

### setNotificationsEnabled Flow

```
Settings toggle
  → If disabling: cancelAll() + persist false
  → If enabling: ensureNotificationPermission() → [Android] exact alarm check → persist true + refresh()
```

## Adding Reminder Support for a New Platform

1. Implement `ReminderScheduler` interface
2. Provide `actual fun getReminderSchedular(...)` returning your implementation
3. Provide `actual @Composable fun rememberNotificationPermissionRequester()`
4. If the platform doesn't support notifications, use `NoOpReminderScheduler` and default the expect to it

## Key Design Decisions

- **Notifications enabled defaults to `false`** — opt-in, not opt-out
- **Settings and Home screens route through `ReminderFlowController`** rather than calling the scheduler directly — ensures consistent permission flow
- **Reminders are identified by `EventKey.name`** — one reminder per event type
- **Offset max is 15 minutes** — enforced by the slider in `ReminderOffsetDialog`
