package com.ai.studyassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.data.local.ReviewItemEntity
import com.ai.studyassistant.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewUiState(
    val isLoading: Boolean = true,
    val items: List<ReviewItemEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswered: Boolean = false,
    val correctCount: Int = 0
) {
    val currentItem: ReviewItemEntity? get() = items.getOrNull(currentIndex)
    val isLastItem: Boolean get() = currentIndex == items.lastIndex
    val isSessionComplete: Boolean get() = !isLoading && items.isNotEmpty() && currentIndex > items.lastIndex
}

class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReviewRepository(application)

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun startSession() {
        _uiState.value = ReviewUiState(isLoading = true)
        viewModelScope.launch {
            val items = repository.getDueQuestions()
            _uiState.value = ReviewUiState(isLoading = false, items = items)
            AnalyticsManager.logEvent("review_session_started", mapOf("due_count" to items.size))
        }
    }

    fun selectAnswer(index: Int) {
        val state = _uiState.value
        val item = state.currentItem ?: return
        if (state.isAnswered) return
        val isCorrect = index == item.correct
        _uiState.update {
            it.copy(
                selectedOptionIndex = index,
                isAnswered = true,
                correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount
            )
        }
        AnalyticsManager.logEvent(
            "review_answer",
            mapOf("exam" to item.exam, "subject" to item.subject, "topic" to item.topic, "correct" to isCorrect, "box" to item.box)
        )
        viewModelScope.launch {
            repository.recordResult(item, isCorrect)
        }
    }

    fun nextItem() {
        _uiState.update {
            it.copy(currentIndex = it.currentIndex + 1, selectedOptionIndex = null, isAnswered = false)
        }
        if (_uiState.value.isSessionComplete) {
            AnalyticsManager.logEvent(
                "review_completed",
                mapOf("correct" to _uiState.value.correctCount, "total" to _uiState.value.items.size)
            )
        }
    }
}
