package com.ai.studyassistant.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.work.WorkManager
import com.ai.studyassistant.data.local.PersonalReminderEntity
import com.ai.studyassistant.data.local.QuizDatabase
import kotlinx.coroutines.flow.first

object ReminderScheduler {
    const val MORNING = "daily_morning"
    const val EVENING = "daily_evening"
    const val ACTION_DISABLE_DAILY = "com.ai.studyassistant.DISABLE_DAILY_REMINDERS"
    const val EXTRA_KEY = "reminder_key"
    const val TARGET = "notification_target"
    @Volatile var appInForeground = false

    fun preciseTimingAllowed(context: Context): Boolean = Build.VERSION.SDK_INT < 31 ||
        context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    private fun intent(context: Context, key: String): PendingIntent = PendingIntent.getBroadcast(
        context, 0, Intent(context, ReminderReceiver::class.java).apply {
            data = Uri.parse("examai://reminder/$key")
            putExtra(EXTRA_KEY, key)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun schedule(context: Context, key: String, time: Long, precise: Boolean = false) {
        val alarms = context.getSystemService(AlarmManager::class.java)
        val pending = intent(context, key)
        val trigger = time.coerceAtLeast(System.currentTimeMillis() + 1000)
        if (precise && preciseTimingAllowed(context)) {
            try { alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending); return }
            catch (_: SecurityException) { /* Access may be revoked between check and call. */ }
        }
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
    }

    fun scheduleDaily(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("daily_reminder_work")
        val now = System.currentTimeMillis()
        // Daily engagement nudges use inexact alarms; personal reminders can opt into precise timing.
        schedule(context, MORNING, nextDailyReminder(10, now))
        schedule(context, EVENING, nextDailyReminder(20, now))
    }
    fun cancelDaily(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("daily_reminder_work")
        cancel(context, MORNING)
        cancel(context, EVENING)
        androidx.core.app.NotificationManagerCompat.from(context).apply {
            cancel(MORNING, 0)
            cancel(EVENING, 0)
        }
    }
    fun schedulePersonal(context: Context, reminder: PersonalReminderEntity) =
        schedule(context, reminder.id, reminder.scheduledAt, precise = true)

    fun cancel(context: Context, key: String) {
        context.getSystemService(AlarmManager::class.java).cancel(intent(context, key))
    }
    suspend fun restore(context: Context) {
        if (ReminderPreferences(context).isEnabled.first()) scheduleDaily(context) else cancelDaily(context)
        QuizDatabase.getInstance(context).personalReminderDao().pending().forEach { schedulePersonal(context, it) }
    }
}
