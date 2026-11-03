package com.ai.studyassistant.notification

import org.junit.Assert.*
import org.junit.Test
import java.time.*

class ReminderPolicyTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private fun time(value: String) = LocalDateTime.parse(value).atZone(zone).toInstant().toEpochMilli()
    @Test fun morningAndEveningUseLocalClockTime() {
        val now = time("2026-11-01T09:00:00")
        assertEquals(time("2026-11-01T10:00:00"), nextDailyReminder(10, now, zone))
        assertEquals(time("2026-11-01T20:00:00"), nextDailyReminder(20, now, zone))
    }
    @Test fun firedSlotMovesToTomorrowWithoutRepeating() {
        assertEquals(time("2026-11-02T10:00:00"), nextDailyReminder(10, time("2026-11-01T10:00:00"), zone))
        assertEquals(time("2026-11-02T20:00:00"), nextDailyReminder(20, time("2026-11-01T23:59:00"), zone))
    }
    @Test fun preservesTenAmAcrossDaylightSavingTransition() {
        val newYork = ZoneId.of("America/New_York")
        val now = ZonedDateTime.of(2026, 10, 31, 20, 0, 0, 0, newYork)
        val next = Instant.ofEpochMilli(nextDailyReminder(10, now.toInstant().toEpochMilli(), newYork)).atZone(newYork)
        assertEquals(10, next.hour)
        assertEquals(LocalDate.of(2026, 11, 1), next.toLocalDate())
        assertEquals(ZoneOffset.ofHours(-5), next.offset)
    }
    @Test fun rejectsBlankTitlesAndPastDates() {
        assertFalse(validPersonalReminder("  ", 200, 100))
        assertFalse(validPersonalReminder("Revise biology", 100, 100))
        assertFalse(validPersonalReminder("A".repeat(101), 200, 100))
        assertTrue(validPersonalReminder("Revise biology", 200, 100))
    }
}
