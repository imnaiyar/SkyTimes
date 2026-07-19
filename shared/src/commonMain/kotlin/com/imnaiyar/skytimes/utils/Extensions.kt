package com.imnaiyar.skytimes.utils

import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Converts an integer to an ordinal string.
 * Like 1st, 2nd, 3rd, 4th, etc.
 */
fun Int.toOrdinal(): String = when {
    this % 100 in 11..13 -> "${this}th"
    this % 10 == 1 -> "${this}st"
    this % 10 == 2 -> "${this}nd"
    this % 10 == 3 -> "${this}rd"
    else -> "${this}th"
}

/**
 * Just a small helper
 */
fun <T> List<T>.indexOfKey(key: Any?): Int = indexOfFirst { it == key }


/** extension that allows all the inputs keeping in mind also the web targets for right clicks */
fun Modifier.contextClickable(
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onRightClick: () -> Unit = {},
): Modifier =
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()

                if (event.type == PointerEventType.Press &&
                    event.buttons.isSecondaryPressed
                ) {
                    onRightClick()
                }
            }
        }
    }
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongPress
        )