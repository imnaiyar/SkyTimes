# Onboarding / Tutorial System

## Overview

The tutorial system is a reusable, screen-agnostic onboarding framework. It renders spotlight overlays with tooltips that guide users through app features. The system is designed so that **adding a new tutorial step never requires changing the tutorial framework itself** — only the app-owned definitions.

## Architecture

```
onboarding/
├── TutorialDefinition.kt    # TutorialStep, TutorialFlow, TutorialDefinition data classes
├── TutorialManager.kt       # State machine: starts/stops flows, tracks completion
├── TutorialHost.kt          # Root composable: wires manager + overlay + lifecycle
├── TutorialLifecycle.kt     # Auto-starts tutorial after compose settles
├── TutorialOverlay.kt       # Renders spotlight cutout + tooltip + gesture hints
├── TutorialTarget.kt        # Composable wrapper that registers bounds in root coords
└── AppTutorial.kt           # App-owned step enum + flow definitions
```

## Key Concepts

### TutorialStep (Interface)

```kotlin
interface TutorialStep {
    val targetId: Any       // Matches a TutorialTarget composable
    val persistenceKey: String  // Stable key for tracking completion (default: targetId.toString())
}
```

The app defines an enum implementing `TutorialStep` (e.g., `AppTutorialStep`). Each enum value declares:
- `targetId` — a string that matches a `TutorialTarget(id = ...)` somewhere in the UI
- `screen` — which `Screen` the step belongs to (used for navigation context, not by the framework)

### TutorialFlow

An ordered list of `TutorialDefinition` steps with a unique `id`. Multiple flows can coexist — the manager starts the first flow with incomplete steps.

```kotlin
val MyFlow = TutorialFlow(
    id = "my_flow",
    steps = listOf(
        TutorialDefinition(
            step = AppTutorialStep.SomeFeature,
            title = "Feature title",
            description = "How to use this feature",
            preferredPlacement = TooltipPlacement.Below,
            gestureHint = TutorialGestureHint.Swipe(SwipeDirection.Down, "Pull down")
        )
    )
)
```

### TutorialTarget (Composable)

Wrap any UI element to make it a spotlight target:

```kotlin
TutorialTarget(id = "my_target_id") {
    // Your normal UI content here
}
```

The `TutorialTargetRegistry` (provided via `CompositionLocal`) collects bounds of all composed targets in root coordinates. The overlay reads these to position the spotlight cutout.

### TutorialManager (State Machine)

Manages state via `StateFlow<TutorialState<S>>`:

| Property | Meaning |
|----------|---------|
| `currentFlowId` | Which flow is active (null = none) |
| `currentStep` | The step being shown |
| `currentStepIndex` | Zero-based position in flow |
| `completedStepKeys` | All completed step persistence keys |
| `isTutorialCompleted` | All steps in all flows are done |
| `isRunning` | A flow is actively being shown |
| `isLoaded` | Initialization complete (completed keys loaded from storage) |
| `canGoPrevious` | User can go back to previous step |

Key methods:
- `start()` — starts first incomplete flow
- `start(flowId)` — starts a specific flow
- `next()` — advances to next step; marks current as complete
- `previous()` — goes back one step
- `skip()` — skips current flow entirely (marks all its steps complete)
- `definitionFor(step)` — looks up the `TutorialDefinition` for a step

### Persistence

`TutorialProgressRepository` is the persistence boundary. The app implements it (in this case, `SettingsRepository` implements it). Completed step keys are stored; completion is *derived* from comparing stored keys against configured definitions. This means **newly shipped steps automatically become eligible** on next launch.

### TutorialHost + TutorialLifecycle

`TutorialHost` wraps screen content and renders the overlay on top. It:
1. Creates a `TutorialTargetRegistry`
2. Optionally auto-starts via `TutorialLifecycle`
3. Renders `content()` below `TutorialOverlay`

`TutorialLifecycle` waits for `isLoaded` and a configurable delay (`DefaultTutorialStartDelayMillis = 500ms`) before starting, giving screens time to compose their targets.

## Adding a New Tutorial Step

1. **Add enum value** to `AppTutorialStep` with a unique `targetId` and the relevant `Screen`
2. **Add `TutorialDefinition`** to the appropriate `TutorialFlow` (or create a new flow)
3. **Wrap UI element** with `TutorialTarget(id = AppTutorialStep.YourStep.targetId) { ... }`
4. **If new flow**: add it to the `flows` list passed to `TutorialManager` in `AppContainer`
5. Done — the framework picks up the new step automatically on next launch

## Gesture Hints

`TutorialGestureHint` provides optional visual cues:
- `Swipe(direction, label)` — shows an animated swipe indicator (Up, Down, Left, Right, Vertical)

## TooltipPlacement

Controls where the tooltip card appears relative to the spotlight target: `Above`, `Below`, `Left`, `Right`.
