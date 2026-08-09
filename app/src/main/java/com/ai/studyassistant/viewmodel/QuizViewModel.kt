package com.ai.studyassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.ads.InterstitialAdManager
import com.ai.studyassistant.data.api.WikipediaService
import com.ai.studyassistant.data.local.ConnectivityObserver
import com.ai.studyassistant.data.local.NetworkUtils
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.data.model.TopicStat
import com.ai.studyassistant.data.repository.AnalyticsRepository
import com.ai.studyassistant.data.repository.BookmarkRepository
import com.ai.studyassistant.data.repository.ExamHistoryRepository
import com.ai.studyassistant.data.repository.QuestionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val SECONDS_PER_QUESTION = 90

data class QuizUiState(
    val isLoading: Boolean = false,
    val exam: String = "",
    val subject: String = "",
    val topic: String = "",
    val attemptId: Long? = null,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswered: Boolean = false,
    val score: Int = 0,
    val error: String? = null,
    val topicSummary: String? = null,
    val isTimed: Boolean = false,
    val timeRemainingSeconds: Int? = null
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val isLastQuestion: Boolean get() = currentIndex == questions.lastIndex
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository(application)
    private val historyRepository = ExamHistoryRepository(application)
    private val analyticsRepository = AnalyticsRepository(application)
    private val bookmarkRepository = BookmarkRepository(application)
    private val wikipediaService = WikipediaService.create()

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _dailyQuestion = MutableStateFlow<Question?>(null)
    val dailyQuestion: StateFlow<Question?> = _dailyQuestion.asStateFlow()

    val streak: StateFlow<Int> = analyticsRepository.currentStreak()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val weakestTopic: StateFlow<TopicStat?> = analyticsRepository.weakestTopic()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val bookmarkedQuestionTexts: StateFlow<Set<String>> = bookmarkRepository.observeBookmarks()
        .map { list -> list.map { it.question }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val isOffline: StateFlow<Boolean> = ConnectivityObserver.isOnline(application)
        .map { online -> !online }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), !NetworkUtils.isOnline(application))

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            _dailyQuestion.value = repository.fetchDailyQuestion()
        }
    }

    fun loadQuestions(
        exam: String,
        subject: String,
        topic: String,
        difficulty: String? = null,
        timed: Boolean = false
    ) {
        timerJob?.cancel()
        _uiState.value = QuizUiState(isLoading = true, exam = exam, subject = subject, topic = topic)
        viewModelScope.launch {
            repository.getQuiz(exam, subject, topic, difficulty)
                .onSuccess { questions ->
                    val durationSeconds = if (timed) questions.size * SECONDS_PER_QUESTION else null
                    val attemptId = historyRepository.startAttempt(
                        exam, subject, topic, questions,
                        isTimed = timed,
                        durationSeconds = durationSeconds
                    )
                    _uiState.value = QuizUiState(
                        exam = exam,
                        subject = subject,
                        topic = topic,
                        attemptId = attemptId,
                        questions = questions,
                        isTimed = timed
                    )
                    if (timed && durationSeconds != null) {
                        startCountdown(durationSeconds)
                    }
                }
                .onFailure { e ->
                    _uiState.value = QuizUiState(
                        exam = exam,
                        subject = subject,
                        topic = topic,
                        error = e.message ?: "Couldn't load questions. Check your connection and try again."
                    )
                }
            loadTopicSummary(topic)
        }
    }

    fun loadMockExam(exam: String, timed: Boolean = true) {
        timerJob?.cancel()
        val subject = "All Subjects"
        val topic = "Mock Test"
        _uiState.value = QuizUiState(isLoading = true, exam = exam, subject = subject, topic = topic)
        viewModelScope.launch {
            repository.getMockExam(exam)
                .onSuccess { questions ->
                    val durationSeconds = if (timed) questions.size * SECONDS_PER_QUESTION else null
                    val attemptId = historyRepository.startAttempt(
                        exam, subject, topic, questions,
                        isTimed = timed,
                        durationSeconds = durationSeconds,
                        isMockExam = true
                    )
                    _uiState.value = QuizUiState(
                        exam = exam,
                        subject = subject,
                        topic = topic,
                        attemptId = attemptId,
                        questions = questions,
                        isTimed = timed
                    )
                    if (timed && durationSeconds != null) {
                        startCountdown(durationSeconds)
                    }
                }
                .onFailure { e ->
                    _uiState.value = QuizUiState(
                        exam = exam,
                        subject = subject,
                        topic = topic,
                        error = e.message ?: "Couldn't load the mock exam. Check your connection and try again."
                    )
                }
        }
    }

    fun resumeAttempt(attemptId: Long) {
        timerJob?.cancel()
        _uiState.value = QuizUiState(isLoading = true)
        viewModelScope.launch {
            val attempt = historyRepository.getAttempt(attemptId)
            if (attempt == null) {
                _uiState.value = QuizUiState(error = "This attempt could not be found.")
                return@launch
            }
            val currentAnswer = attempt.answers.getOrNull(attempt.currentIndex) ?: -1
            val score = attempt.answers.indices.count { i ->
                attempt.answers[i] != -1 && attempt.answers[i] == attempt.questions.getOrNull(i)?.correct
            }
            _uiState.value = QuizUiState(
                exam = attempt.exam,
                subject = attempt.subject,
                topic = attempt.topic,
                attemptId = attempt.id,
                questions = attempt.questions,
                currentIndex = attempt.currentIndex,
                selectedOptionIndex = currentAnswer.takeIf { it != -1 },
                isAnswered = currentAnswer != -1,
                score = score,
                isTimed = attempt.isTimed
            )
            loadTopicSummary(attempt.topic)

            if (attempt.isTimed) {
                val elapsedSeconds = ((System.currentTimeMillis() - attempt.startTime) / 1000).toInt()
                val remaining = (attempt.durationSeconds ?: 0) - elapsedSeconds
                if (remaining <= 0) {
                    _uiState.update { it.copy(timeRemainingSeconds = 0) }
                } else {
                    startCountdown(remaining)
                }
            }
        }
    }

    private fun startCountdown(totalSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = totalSeconds
            _uiState.update { it.copy(timeRemainingSeconds = remaining) }
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(timeRemainingSeconds = remaining) }
            }
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
        val attemptId = state.attemptId ?: return
        viewModelScope.launch {
            historyRepository.recordAnswer(attemptId, state.currentIndex, index)
        }
    }

    fun nextQuestion() {
        val nextIndex = _uiState.value.currentIndex + 1
        _uiState.update {
            it.copy(
                currentIndex = nextIndex,
                selectedOptionIndex = null,
                isAnswered = false
            )
        }
        val attemptId = _uiState.value.attemptId ?: return
        viewModelScope.launch {
            historyRepository.advanceTo(attemptId, nextIndex)
        }
    }

    fun toggleBookmark() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(state.exam, state.subject, state.topic, question)
        }
    }

    fun finishAttempt() {
        timerJob?.cancel()
        InterstitialAdManager.preload(getApplication())
        val attemptId = _uiState.value.attemptId ?: return
        viewModelScope.launch {
            historyRepository.finishAttempt(attemptId)
        }
    }
}
