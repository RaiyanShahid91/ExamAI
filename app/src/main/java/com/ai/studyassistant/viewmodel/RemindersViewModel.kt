package com.ai.studyassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.data.local.PersonalReminderEntity
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.notification.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import com.ai.studyassistant.i18n.LanguageManager
import com.ai.studyassistant.R

class RemindersViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = QuizDatabase.getInstance(application).personalReminderDao()
    private val preferences = ReminderPreferences(application)
    val reminders = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dailyEnabled = preferences.isEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()

    fun setDailyEnabled(enabled: Boolean) = viewModelScope.launch {
        preferences.setEnabled(enabled)
        if (enabled) ReminderScheduler.scheduleDaily(getApplication()) else ReminderScheduler.cancelDaily(getApplication())
    }

    fun save(id: String?, title: String, note: String, scheduledAt: Long, onSaved: () -> Unit) {
        if (_saving.value) return
        if (!validPersonalReminder(title, scheduledAt, System.currentTimeMillis())) {
            _error.value = msg(R.string.reminders_error_invalid)
            return
        }
        _saving.value = true
        viewModelScope.launch {
            try {
                val item = PersonalReminderEntity(id ?: UUID.randomUUID().toString(), title.trim(), note.trim().take(500), scheduledAt)
                dao.save(item)
                ReminderScheduler.schedulePersonal(getApplication(), item)
                _error.value = null
                onSaved()
            } catch (_: Exception) { _error.value = msg(R.string.reminders_error_save) }
            finally { _saving.value = false }
        }
    }

    fun delete(id: String) = viewModelScope.launch {
        try {
            dao.delete(id)
            ReminderScheduler.cancel(getApplication(), id)
            androidx.core.app.NotificationManagerCompat.from(getApplication()).cancel(id, 0)
        } catch (_: Exception) { _error.value = msg(R.string.reminders_error_delete) }
    }
    fun clearError() { _error.value = null }

    private fun msg(@androidx.annotation.StringRes id: Int, vararg args: Any): String =
        LanguageManager.localized(getApplication<Application>()).getString(id, *args)
}
