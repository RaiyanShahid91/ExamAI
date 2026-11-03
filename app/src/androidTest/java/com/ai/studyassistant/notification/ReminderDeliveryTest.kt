package com.ai.studyassistant.notification

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ai.studyassistant.data.local.PersonalReminderEntity
import com.ai.studyassistant.data.local.QuizDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ReminderDeliveryTest {
    @Test fun migrationPreservesExistingNotesAndAddsReminders() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val name = "migration-test-${UUID.randomUUID()}.db"
        var migrated: QuizDatabase? = null
        try {
            val schema = org.json.JSONObject(instrumentation.context.assets.open("quiz_schema_v4.json").bufferedReader().use { it.readText() })
                .getJSONObject("database").getJSONArray("entities")
            context.openOrCreateDatabase(name, 0, null).use { old ->
                for (index in 0 until schema.length()) {
                    val table = schema.getJSONObject(index)
                    old.execSQL(table.getString("createSql").replace("\${TABLE_NAME}", table.getString("tableName")))
                }
                old.execSQL("INSERT INTO saved_notes (topic, title, summary, thumbnailUrl, savedAt) VALUES (?, ?, ?, NULL, ?)", arrayOf("Physics", "Existing note", "Keep my progress", 100L))
                old.version = 4
            }
            val database = androidx.room.Room.databaseBuilder(context, QuizDatabase::class.java, name)
                .addMigrations(QuizDatabase.MIGRATION_4_5).build()
            migrated = database
            assertEquals("Keep my progress", database.noteDao().getByTopic("Physics")?.summary)
            database.personalReminderDao().save(PersonalReminderEntity("migration-reminder", "Revise", "", 1000))
            assertEquals(1, database.personalReminderDao().pending().size)
        } finally {
            migrated?.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun dailyNotificationOptOutCancelsNudgesWithoutChangingPersonalReminders() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        val preferences = ReminderPreferences(context)
        val original = preferences.isEnabled.first()
        val dao = QuizDatabase.getInstance(context).personalReminderDao()
        val key = UUID.randomUUID().toString()
        val manager = context.getSystemService(NotificationManager::class.java)
        try {
            preferences.setEnabled(true)
            dao.save(PersonalReminderEntity(key, "Personal plan", "Keep this reminder", System.currentTimeMillis() + 86_400_000))
            assertTrue(NotificationHelper.show(context, ReminderScheduler.MORNING, "Study reminder", "A daily nudge", com.ai.studyassistant.navigation.Routes.HOME))
            val posted = manager.activeNotifications.first { it.tag == ReminderScheduler.MORNING }
            assertNotNull(posted.notification.publicVersion)
            assertEquals("Open ExamAI to view your study reminder.", posted.notification.publicVersion.extras.getString("android.text"))
            posted.notification.actions.last().actionIntent.send()
            repeat(50) { if (preferences.isEnabled.first()) delay(100) }
            assertFalse(preferences.isEnabled.first())
            assertFalse(manager.activeNotifications.any { it.tag == ReminderScheduler.MORNING })
            assertNotNull(dao.find(key))
        } finally {
            dao.delete(key)
            preferences.setEnabled(original)
            ReminderScheduler.restore(context)
            manager.cancel(ReminderScheduler.MORNING, 0)
        }
    }

    @Test fun personalReminderPostsAndIsMarkedDeliveredOnlyOnce() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT >= 33) instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        val key = UUID.randomUUID().toString()
        val dao = QuizDatabase.getInstance(context).personalReminderDao()
        val manager = context.getSystemService(NotificationManager::class.java)
        try {
            dao.save(PersonalReminderEntity(key, "Revise chemistry", "Review chemical bonding", System.currentTimeMillis() - 1000))
            val intent = Intent(context, ReminderReceiver::class.java).putExtra(ReminderScheduler.EXTRA_KEY, key)
            context.sendBroadcast(intent)
            repeat(50) {
                if (dao.find(key)?.delivered != true) delay(100)
            }
            assertTrue(dao.find(key)?.delivered == true)
            val notification = manager.activeNotifications.firstOrNull { it.tag == key }
            assertNotNull(notification)
            assertEquals("Revise chemistry", notification!!.notification.extras.getString("android.title"))
            context.sendBroadcast(intent)
            delay(300)
            assertEquals(1, manager.activeNotifications.count { it.tag == key })
        } finally {
            dao.delete(key)
            ReminderScheduler.cancel(context, key)
            manager.cancel(key, 0)
        }
    }
}
