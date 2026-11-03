package com.ai.studyassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.ads.ADS_ENABLED
import com.ai.studyassistant.ads.SearchBalance
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.LocalDate
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.data.model.StudyNote
import com.ai.studyassistant.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.ai.studyassistant.i18n.LanguageManager
import com.ai.studyassistant.R

private const val FREE_SEARCHES_PER_UNLOCK = 2

data class NotesUiState(
    val isLoading: Boolean = false,
    val note: StudyNote? = null,
    val error: String? = null,
    val savedNotes: List<StudyNote> = emptyList(),
    val freeSearchesRemaining: Int = FREE_SEARCHES_PER_UNLOCK
)

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotesRepository(application)

    private val allowance = application.getSharedPreferences("note_search_allowance", Application.MODE_PRIVATE)
    private fun remaining(): Int {
        val day = LocalDate.now().toString()
        if (allowance.getString("day", null) != day) {
            // Daily free searches replenish without discarding unused earned searches.
            val updated = SearchBalance(allowance.getInt("remaining", 0), allowance.getInt("free_remaining", 0)).nextDay()
            allowance.edit().putString("day", day).putInt("remaining", updated.total)
                .putInt("free_remaining", updated.dailyFree).apply()
        }
        return allowance.getInt("remaining", FREE_SEARCHES_PER_UNLOCK)
    }
    private fun consumeSearch() {
        val balance = remaining()
        val updated = SearchBalance(balance, allowance.getInt("free_remaining", 0)).consume()
        allowance.edit().putInt("remaining", updated.total).putInt("free_remaining", updated.dailyFree).apply()
    }
    private val _uiState = MutableStateFlow(NotesUiState(freeSearchesRemaining = remaining()))
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSavedNotes().collect { notes ->
                _uiState.update { it.copy(savedNotes = notes) }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                _uiState.update { it.copy(freeSearchesRemaining = remaining()) }
                delay(30_000)
            }
        }
    }

    fun search(topic: String) {
        val cleaned = topic.trim()
        if (cleaned.isBlank() || _uiState.value.isLoading) return
        val saved = _uiState.value.savedNotes.firstOrNull { it.topic.equals(cleaned, ignoreCase = true) }
        if (saved != null) { openSaved(saved); return }
        val balance = remaining()
        _uiState.update { it.copy(freeSearchesRemaining = balance) }
        if (ADS_ENABLED && balance <= 0) {
            _uiState.update { it.copy(error = msg(R.string.notes_error_no_searches)) }
            return
        }
        fetchNote(cleaned, consumeCredit = ADS_ENABLED)
    }

    private fun msg(@androidx.annotation.StringRes id: Int, vararg args: Any): String =
        LanguageManager.localized(getApplication<Application>()).getString(id, *args)

    fun grantSearches() {
        val updated = SearchBalance(remaining(), allowance.getInt("free_remaining", 0)).reward()
        allowance.edit().putInt("remaining", updated.total).apply()
        _uiState.update { it.copy(freeSearchesRemaining = remaining(), error = null) }
    }

    fun openSaved(note: StudyNote) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(note = note, error = null) }
    }

    private fun fetchNote(topic: String, consumeCredit: Boolean) {
        AnalyticsManager.logEvent("note_searched", mapOf("topic" to topic))
        _uiState.update { it.copy(isLoading = true, error = null, note = null) }
        viewModelScope.launch {
            repository.fetchSummary(topic)
                .onSuccess { note ->
                    if (consumeCredit) consumeSearch()
                    _uiState.update { it.copy(isLoading = false, note = note, freeSearchesRemaining = remaining()) }
                }
                .onFailure { e ->
                    AnalyticsManager.recordNonFatal(e, context = "fetchNote($topic)")
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: msg(R.string.notes_error_fetch_topic, topic)) }
                }
        }
    }

    fun toggleSave() {
        val note = _uiState.value.note ?: return
        val willBeSaved = !note.isSaved
        AnalyticsManager.logEvent(if (willBeSaved) "note_saved" else "note_unsaved", mapOf("topic" to note.topic))
        viewModelScope.launch {
            if (note.isSaved) repository.deleteNote(note) else repository.saveNote(note)
            _uiState.update { it.copy(note = note.copy(isSaved = !note.isSaved)) }
        }
    }

    fun clearNote() {
        _uiState.update { it.copy(note = null, error = null) }
    }
}
