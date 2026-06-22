package com.ai.studyassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.data.api.WikipediaService
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val isLoading: Boolean = false,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswered: Boolean = false,
    val score: Int = 0,
    val error: String? = null,
    val topicSummary: String? = null
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val isLastQuestion: Boolean get() = currentIndex == questions.lastIndex
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository(application)
    private val wikipediaService = WikipediaService.create()

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _dailyQuestion = MutableStateFlow<Question?>(null)
    val dailyQuestion: StateFlow<Question?> = _dailyQuestion.asStateFlow()

    init {
        viewModelScope.launch {
            _dailyQuestion.value = repository.fetchDailyQuestion()
        }
    }

    fun loadQuestions(exam: String, subject: String, topic: String) {
        _uiState.value = QuizUiState(isLoading = true)
        viewModelScope.launch {
            repository.getQuiz(exam, subject, topic)
                .onSuccess { questions ->
                    _uiState.value = QuizUiState(questions = questions)
                }
                .onFailure { e ->
                    _uiState.value = QuizUiState(error = e.message ?: "Couldn't load questions. Check your connection and try again.")
                }
            loadTopicSummary(topic)
        }
    }

    private suspend fun loadTopicSummary(topic: String) {
        try {
            val summary = wikipediaService.getSummary(topic.replace(" ", "_")).extract
            if (summary.isNotBlank()) {
                _uiState.update { it.copy(topicSummary = summary) }
            }
        } catch (_: Exception) {
            // Optional supplementary content — silently skip if unavailable.
        }
    }

    fun selectAnswer(index: Int) {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        if (state.isAnswered) return
        val isCorrect = index == question.correct
        _uiState.update {
            it.copy(
                selectedOptionIndex = index,
                isAnswered = true,
                score = if (isCorrect) it.score + 1 else it.score
            )
        }
    }

    fun nextQuestion() {
        _uiState.update {
            it.copy(
                currentIndex = it.currentIndex + 1,
                selectedOptionIndex = null,
                isAnswered = false
            )
        }
    }
}
