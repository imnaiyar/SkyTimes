package com.imnaiyar.skytimes.utils

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