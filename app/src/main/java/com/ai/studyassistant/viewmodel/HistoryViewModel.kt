package com.ai.studyassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.data.model.ExamAttemptSummary
import com.ai.studyassistant.data.model.SubjectStat
import com.ai.studyassistant.data.repository.AnalyticsRepository
import com.ai.studyassistant.data.repository.ExamHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExamHistoryRepository(application)
    private val analyticsRepository = AnalyticsRepository(application)

    private val _attempts = MutableStateFlow<List<ExamAttemptSummary>>(emptyList())
    val attempts: StateFlow<List<ExamAttemptSummary>> = _attempts.asStateFlow()

    val subjectStats: StateFlow<List<SubjectStat>> = analyticsRepository.subjectStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _detail = MutableStateFlow<ExamAttemptSummary?>(null)
    val detail: StateFlow<ExamAttemptSummary?> = _detail.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAttempts().collect { _attempts.value = it }
        }
    }

    fun loadDetail(attemptId: Long) {
        _detail.value = null
        viewModelScope.launch {
            repository.observeAttempt(attemptId).collect { _detail.value = it }
        }
    }
}
