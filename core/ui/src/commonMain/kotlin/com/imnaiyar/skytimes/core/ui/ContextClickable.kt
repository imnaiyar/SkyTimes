package com.imnaiyar.skytimes.core.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput

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
