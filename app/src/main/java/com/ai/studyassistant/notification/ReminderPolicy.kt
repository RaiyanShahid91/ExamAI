package com.ai.studyassistant.notification

import java.time.Instant
import java.time.ZoneId

/** Compute each occurrence from local calendar time, including DST and timezone changes. */
fun nextDailyReminder(hour: Int, now: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
    val current = Instant.ofEpochMilli(now).atZone(zone)
    var target = current.toLocalDate().atTime(hour, 0).atZone(zone)
    if (!target.isAfter(current)) target = current.toLocalDate().plusDays(1).atTime(hour, 0).atZone(zone)
    return target.toInstant().toEpochMilli()
}

fun validPersonalReminder(title: String, scheduledAt: Long, now: Long): Boolean =
    title.trim().isNotEmpty() && title.trim().length <= 100 && scheduledAt > now
