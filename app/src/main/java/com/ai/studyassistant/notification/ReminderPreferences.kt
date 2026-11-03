package com.ai.studyassistant.notification

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.reminderDataStore by preferencesDataStore(name = "reminder_prefs")

class ReminderPreferences(private val context: Context) {

    private val keyEnabled = booleanPreferencesKey("daily_reminder_opt_in_v2")

    val isEnabled: Flow<Boolean> = context.reminderDataStore.data.map { it[keyEnabled] ?: false }

    suspend fun setEnabled(enabled: Boolean) {
        context.reminderDataStore.edit { it[keyEnabled] = enabled }
    }
}
