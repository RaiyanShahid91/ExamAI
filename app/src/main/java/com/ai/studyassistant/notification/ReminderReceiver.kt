package com.ai.studyassistant.notification

import android.content.*
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.model.ExamData
import com.ai.studyassistant.navigation.Routes
import com.ai.studyassistant.analytics.AnalyticsManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import com.ai.studyassistant.i18n.LanguageManager
import com.ai.studyassistant.R

class ReminderReceiver : BroadcastReceiver() {
    companion object { private val deliveryLock = Mutex() }
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                withTimeout(8_000) {
                    deliveryLock.withLock {
                        if (intent.action == ReminderScheduler.ACTION_DISABLE_DAILY) {
                            ReminderPreferences(context).setEnabled(false)
                            ReminderScheduler.cancelDaily(context)
                            return@withLock
                        }
                        val key = intent.getStringExtra(ReminderScheduler.EXTRA_KEY)
                        if (key == null) ReminderScheduler.restore(context)
                        else if (key == ReminderScheduler.MORNING || key == ReminderScheduler.EVENING) daily(context, key)
                        else personal(context, key)
                    }
                }
            } catch (error: Exception) {
                AnalyticsManager.recordNonFatal(error, "reminder_delivery")
            } finally { pending.finish() }
        }
    }

    private suspend fun daily(context: Context, key: String) {
        if (!ReminderPreferences(context).isEnabled.first()) return
        ReminderScheduler.scheduleDaily(context)
        val hour = java.time.LocalTime.now().hour
        if ((key == ReminderScheduler.MORNING && hour !in 10..11) ||
            (key == ReminderScheduler.EVENING && hour !in 20..21)) return
        if (ReminderScheduler.appInForeground || !NotificationHelper.canNotify(context)) return
        val sent = context.getSharedPreferences("reminder_delivery", Context.MODE_PRIVATE)
        val today = LocalDate.now().toString()
        if (sent.getString(key, null) == today) return
        val database = QuizDatabase.getInstance(context)
        val attempts = database.examAttemptDao().observeAll().first()
        val unfinished = attempts.firstOrNull { !it.isCompleted && it.questions.isNotEmpty() }
        val due = database.reviewDao().observeDueCount(System.currentTimeMillis()).first()
        val latest = attempts.firstOrNull { it.topic in ExamData.topicsFor(it.exam, it.subject) }
        val c = LanguageManager.localized(context)
        val title: String
        val body: String
        val target: String
        when {
            unfinished != null -> {
                title = c.getString(R.string.notif_unfinished_title)
                body = c.getString(R.string.notif_unfinished_body, unfinished.topic, unfinished.exam)
                target = Routes.HISTORY
            }
            due > 0 -> {
                title = c.getString(R.string.notif_due_title)
                body = c.resources.getQuantityString(R.plurals.notif_due_body, due, due)
                target = Routes.REVIEW
            }
            latest != null -> {
                title = if (key == ReminderScheduler.MORNING) c.getString(R.string.notif_latest_title_morning) else c.getString(R.string.notif_latest_title_evening)
                body = c.getString(R.string.notif_latest_body, latest.topic)
                target = Routes.topic(latest.exam, latest.subject)
            }
            else -> {
                title = if (key == ReminderScheduler.MORNING) c.getString(R.string.notif_default_title_morning) else c.getString(R.string.notif_default_title_evening)
                body = c.getString(R.string.notif_default_body)
                target = Routes.HOME
            }
        }
        if (NotificationHelper.show(context, key, title, body, target)) sent.edit().putString(key, today).apply()
    }

    private suspend fun personal(context: Context, key: String) {
        val dao = QuizDatabase.getInstance(context).personalReminderDao()
        val item = dao.find(key) ?: return
        if (item.delivered) return
        if (item.scheduledAt > System.currentTimeMillis()) {
            ReminderScheduler.schedulePersonal(context, item)
            return
        }
        if (!NotificationHelper.canNotify(context)) return
        if (NotificationHelper.show(context, key, item.title, item.note.ifBlank { LanguageManager.localized(context).getString(R.string.notif_personal_default_body) }, Routes.REMINDERS, personal = true)) {
            dao.claim(key)
        }
    }
}
