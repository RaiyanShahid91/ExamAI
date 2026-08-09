package com.ai.studyassistant.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.firstRunDataStore by preferencesDataStore(name = "first_run_prefs")

class FirstRunPreferences(private val context: Context) {

    private val keySeenOnboarding = booleanPreferencesKey("has_seen_onboarding")
    private val keyPromptedNotification = booleanPreferencesKey("has_prompted_notification_permission")
    private val keyCompletedAttemptCount = intPreferencesKey("completed_attempt_count")

    val hasSeenOnboarding: Flow<Boolean> =
        context.firstRunDataStore.data.map { it[keySeenOnboarding] ?: false }

    val hasPromptedNotificationPermission: Flow<Boolean> =
        context.firstRunDataStore.data.map { it[keyPromptedNotification] ?: false }

    suspend fun setSeenOnboarding() {
        context.firstRunDataStore.edit { it[keySeenOnboarding] = true }
    }

    suspend fun setPromptedNotificationPermission() {
        context.firstRunDataStore.edit { it[keyPromptedNotification] = true }
    }

    /** Returns the new count after incrementing, used to frequency-cap the interstitial ad. */
    suspend fun incrementCompletedAttemptCount(): Int {
        var newCount = 0
        context.firstRunDataStore.edit {
            newCount = (it[keyCompletedAttemptCount] ?: 0) + 1
            it[keyCompletedAttemptCount] = newCount
        }
        return newCount
    }
}
