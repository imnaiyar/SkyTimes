package com.imnaiyar.skytimes.onboarding

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A step is supplied by the app, so the tutorial package never needs to know about
 * individual screens. An enum is a convenient implementation of this interface.
 */
interface TutorialStep {
    /** The id used by [TutorialTarget] to identify the composable to spotlight. */
    val targetId: Any

    /** A stable key used by [TutorialProgressPersistence]. Override if targetId can change. */
    val persistenceKey: String
        get() = targetId.toString()
}

/** The side of a target on which a tooltip should preferably be shown. */
enum class TooltipPlacement {
    Above,
    Below,
    Left,
    Right
}

/** An optional, reusable visual cue for tutorials that teach a gesture. */
sealed interface TutorialGestureHint {
    data class Swipe(
        val direction: SwipeDirection,
        val label: String? = null
    ) : TutorialGestureHint
}

enum class SwipeDirection {
    Up,
    Down,
    Left,
    Right,
    Vertical
}

/**
 * Presentation data for a single [TutorialStep]. Apps own these definitions;
 * this library only lays them out and renders them.
 */
data class TutorialDefinition<S : TutorialStep>(
    val step: S,
    val title: String? = null,
    val description: String,
    val preferredPlacement: TooltipPlacement = TooltipPlacement.Below,
    val spotlightPadding: Dp = 8.dp,
    val spotlightCornerRadius: Dp = 16.dp,
    val gestureHint: TutorialGestureHint? = null
)

/**
 * An ordered, independently startable tutorial. Flows let an app add a new
 * screen's onboarding without changing any existing tutorial configuration.
 */
data class TutorialFlow<S : TutorialStep>(
    val id: String,
    val steps: List<TutorialDefinition<S>>
) {
    init {
        require(id.isNotBlank()) { "A tutorial flow must have a non-blank id." }
        require(steps.isNotEmpty()) { "A tutorial flow must contain at least one step." }
    }
}
