package com.ai.studyassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.local.StudyPlanPreferences
import com.ai.studyassistant.data.local.StudyPlanSettings
import com.ai.studyassistant.data.model.buildDailyStudyPlan
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = StudyPlanPreferences(application)
    private val database = QuizDatabase.getInstance(application)
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000)
        }
    }
    val plan = combine(preferences.settings, database.examAttemptDao().observeAll(), database.reviewDao().observeAll(), clock) {
        settings, attempts, reviews, now -> buildDailyStudyPlan(settings, attempts, reviews, now)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(settings: StudyPlanSettings) {
        viewModelScope.launch { preferences.save(settings) }
    }
}
