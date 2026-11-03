package com.ai.studyassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.data.model.FlashcardSession
import com.ai.studyassistant.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FlashcardsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FlashcardRepository(application)

    // Null while the deck loads. The deck is snapshotted at the start of a session so cards
    // don't shuffle under the student if they bookmark something elsewhere.
    private val _session = MutableStateFlow<FlashcardSession?>(null)
    val session: StateFlow<FlashcardSession?> = _session.asStateFlow()

    init {
        startSession()
    }

    fun startSession() {
        viewModelScope.launch {
            _session.value = FlashcardSession.start(repository.observeCards().first())
        }
    }

    fun flip() = _session.update { it?.flip() }

    fun gotIt() {
        _session.update { it?.gotIt() }
        if (_session.value?.isComplete == true) {
            AnalyticsManager.logEvent("flashcards_completed", mapOf("cards" to (_session.value?.total ?: 0)))
        }
    }

    fun again() = _session.update { it?.again() }
}
