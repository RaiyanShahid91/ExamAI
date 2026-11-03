package com.ai.studyassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.data.model.InsightsData
import com.ai.studyassistant.data.repository.AnalyticsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AnalyticsRepository(application)

    val insights: StateFlow<InsightsData> = repository.insights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsData())
}
