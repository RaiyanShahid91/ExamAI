package com.ai.studyassistant.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.ads.RewardedAdManager
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.data.model.StudyNote
import com.ai.studyassistant.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSavedNotes().collect { notes ->
                _uiState.update { it.copy(savedNotes = notes) }
            }
        }
        RewardedAdManager.preload(application)
    }

    /**
     * Gate in front of [fetchNote]: the first two searches per unlock are free, then a
     * rewarded ad must be watched (and the reward actually earned) before the next two.
     * Falls through to a plain search if there's no Activity to show the ad in, rather
     * than stalling the user on a gate that can't be shown.
     */
    fun search(topic: String, activity: Activity?) {
        if (topic.isBlank()) return
        if (_uiState.value.freeSearchesRemaining > 0 || activity == null) {
            _uiState.update { it.copy(freeSearchesRemaining = (it.freeSearchesRemaining - 1).coerceAtLeast(0)) }
            fetchNote(topic)
            return
        }

        AnalyticsManager.logEvent("notes_search_ad_gate_shown", mapOf("topic" to topic))
        var rewardEarned = false
        RewardedAdManager.showIfAvailable(
            activity,
            onRewarded = { amount ->
                rewardEarned = true
                // amount is whatever this ad unit's reward is configured as in the AdMob
                // console (currently 2) — this search uses one of the granted searches.
                _uiState.update { it.copy(freeSearchesRemaining = (amount - 1).coerceAtLeast(0)) }
                fetchNote(topic)
            },
            onDismissed = {
                if (!rewardEarned) {
                    _uiState.update {
                        it.copy(error = "Watch a short ad to unlock 2 more searches — no ad is ready yet, please try again in a moment.")
                    }
                }
                RewardedAdManager.preload(getApplication())
            }
        )
    }

    fun fetchNote(topic: String) {
        AnalyticsManager.logEvent("note_searched", mapOf("topic" to topic))
        _uiState.update { it.copy(isLoading = true, error = null, note = null) }
        viewModelScope.launch {
            repository.fetchSummary(topic)
                .onSuccess { note -> _uiState.update { it.copy(isLoading = false, note = note) } }
                .onFailure { e ->
                    AnalyticsManager.recordNonFatal(e, context = "fetchNote($topic)")
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Couldn't fetch notes for $topic.")
                    }
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
