package com.ai.studyassistant.notification

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ai.studyassistant.MainActivity
import com.ai.studyassistant.R
import com.ai.studyassistant.i18n.LanguageManager

object NotificationHelper {
    const val CHANNEL_ID = "daily_reminder"
    const val PERSONAL_CHANNEL = "personal_reminders"

    fun canNotify(context: Context): Boolean =
        (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) &&
            NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val c = LanguageManager.localized(context)
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, c.getString(R.string.notif_channel_daily_name), NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = c.getString(R.string.notif_channel_daily_desc)
        })
        manager.createNotificationChannel(NotificationChannel(PERSONAL_CHANNEL, c.getString(R.string.notif_channel_personal_name), NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = c.getString(R.string.notif_channel_personal_desc)
        })
    }

    fun show(context: Context, key: String, title: String, body: String, target: String, personal: Boolean = false): Boolean {
        ensureChannel(context)
        if (!canNotify(context)) return false
        val c = LanguageManager.localized(context)
        val channel = if (personal) PERSONAL_CHANNEL else CHANNEL_ID
        if (context.getSystemService(NotificationManager::class.java).getNotificationChannel(channel)?.importance == NotificationManager.IMPORTANCE_NONE) return false
        val pending = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java).apply {
            data = Uri.parse("examai://notification/$key")
            putExtra(ReminderScheduler.TARGET, target)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val publicVersion = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification_study)
            .setContentTitle(c.getString(R.string.notif_public_title))
            .setContentText(c.getString(R.string.notif_public_body))
            .build()
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification_study)
            .setContentTitle(title).setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending).setAutoCancel(true).setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(0, if (personal) c.getString(R.string.notif_action_view) else c.getString(R.string.notif_action_continue), pending)
        if (!personal) {
            val disable = PendingIntent.getBroadcast(context, 0,
                Intent(context, ReminderReceiver::class.java).setAction(ReminderScheduler.ACTION_DISABLE_DAILY),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(0, c.getString(R.string.notif_action_disable), disable)
            builder.setTimeoutAfter(2 * 60 * 60 * 1000L)
        }
        val notification = builder.build()
        return try {
            NotificationManagerCompat.from(context).notify(key, 0, notification)
            true
        } catch (_: SecurityException) { false }
    }
}
