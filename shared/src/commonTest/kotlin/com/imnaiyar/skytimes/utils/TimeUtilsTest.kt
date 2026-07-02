package com.imnaiyar.skytimes.utils

import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeUtilsTest {
    private val timeUtils = TimeUtils()

    @Test
    fun formatsLocalTimeUsing24HourClock() {
        val formatted = timeUtils.formatTime(
            TimeValue.localTime(LocalTime(13, 5, 9)),
            use24HourClock = true
        )

        assertEquals("13:05:09", formatted)
    }

    @Test
    fun formatsLocalTimeUsing12HourClock() {
        val formatted = timeUtils.formatTime(
            TimeValue.localTime(LocalTime(13, 5, 9)),
            use24HourClock = false
        )

        assertEquals("1:05:09 PM", formatted)
    }
}
