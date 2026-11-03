package com.ai.studyassistant.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Compatibility for previously persisted WorkManager jobs; clock-based alarms replace them. */
class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        ReminderScheduler.restore(applicationContext)
        return Result.success()
    }
    companion object {
        fun schedule(context: Context) = ReminderScheduler.scheduleDaily(context)
        fun cancel(context: Context) = ReminderScheduler.cancelDaily(context)
    }
}
