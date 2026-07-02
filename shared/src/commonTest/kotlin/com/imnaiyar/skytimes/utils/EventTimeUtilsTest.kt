package com.imnaiyar.skytimes.utils

import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.constants.events
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class EventTimeUtilsTest {
    @Test
    fun inactiveEventUsesInjectedNowForRemainingTime() {
        val event = event(EventKey.DAILY_RESET)
        val now = Instant.parse("2026-07-02T08:00:00Z")

        val details = EventTimeUtils.getEventDetails(event, now)

        assertEquals(Instant.parse("2026-07-03T07:00:00Z"), details.nextOccurrence)
        val status = assertIs<Times.Inactive>(details.status)
        assertEquals((23 * 60).minutes, status.remaining)
    }

    @Test
    fun activeEventUsesInjectedNowForRemainingTime() {
        val event = event(EventKey.GEYSER)
        val now = Instant.parse("2026-07-02T07:05:00Z")

        val details = EventTimeUtils.getEventDetails(event, now)

        assertEquals(Instant.parse("2026-07-02T09:00:00Z"), details.nextOccurrence)
        val status = assertIs<Times.Active>(details.status)
        assertEquals(10.minutes, status.remaining)
        assertEquals(Instant.parse("2026-07-02T07:00:00Z"), status.startTime)
        assertEquals(Instant.parse("2026-07-02T07:15:00Z"), status.endTime)
    }

    @Test
    fun intervalEventRollsForwardToNextOccurrence() {
        val event = event(EventKey.GEYSER)
        val now = Instant.parse("2026-07-02T08:30:00Z")

        val nextOccurrence = EventTimeUtils.getNextOccurrence(event, now)

        assertEquals(Instant.parse("2026-07-02T09:00:00Z"), nextOccurrence)
    }

    @Test
    fun weekdayEventUsesNextMatchingWeekday() {
        val event = event(EventKey.EDEN)
        val now = Instant.parse("2026-07-02T08:00:00Z")

        val nextOccurrence = EventTimeUtils.getNextOccurrence(event, now)

        assertEquals(Instant.parse("2026-07-05T07:00:00Z"), nextOccurrence)
    }

    @Test
    fun monthlyEventUsesNextMatchingDayOfMonth() {
        val event = event(EventKey.FIREWORKS_FESTIVAL)
        val now = Instant.parse("2026-07-02T08:00:00Z")

        val nextOccurrence = EventTimeUtils.getNextOccurrence(event, now)

        assertEquals(Instant.parse("2026-08-01T07:00:00Z"), nextOccurrence)
    }

    private fun event(key: EventKey) = events.first { it.key == key }
}
