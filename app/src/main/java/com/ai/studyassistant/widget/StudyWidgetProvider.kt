package com.ai.studyassistant.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.ai.studyassistant.MainActivity
import com.ai.studyassistant.R
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.repository.AnalyticsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.ai.studyassistant.i18n.LanguageManager

/**
 * Home-screen widget showing the study streak and reviews due. It reads only on-device data
 * and has no network access; tapping it opens the app.
 */
class StudyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val views = buildViews(context.applicationContext)
                appWidgetIds.forEach { manager.updateAppWidget(it, views) }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        /** Call after practice so the widget reflects the new streak straight away. */
        fun refresh(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val ids = manager.getAppWidgetIds(ComponentName(app, StudyWidgetProvider::class.java))
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.IO).launch {
                val views = buildViews(app)
                ids.forEach { manager.updateAppWidget(it, views) }
            }
        }

        private suspend fun buildViews(context: Context): RemoteViews {
            val streak = AnalyticsRepository(context).insightsSnapshot().currentStreak
            val due = QuizDatabase.getInstance(context).reviewDao().getDue(System.currentTimeMillis(), 1000).size

            val c = LanguageManager.localized(context)
            val views = RemoteViews(context.packageName, R.layout.widget_study)
            views.setTextViewText(R.id.widget_title, c.getString(R.string.widget_title))
            views.setTextViewText(R.id.widget_cta, c.getString(R.string.widget_cta))
            views.setInt(R.id.widget_root, "setLayoutDirection", c.resources.configuration.layoutDirection)
            views.setTextViewText(R.id.widget_streak, c.resources.getQuantityString(R.plurals.widget_streak_days, streak, streak))
            views.setTextViewText(R.id.widget_reviews, c.resources.getQuantityString(R.plurals.widget_reviews_due, due, due))

            val launch = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            views.setOnClickPendingIntent(
                R.id.widget_root,
                PendingIntent.getActivity(context, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
            return views
        }
    }
}
