package com.ai.studyassistant.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ai.studyassistant.data.model.ExamData
import kotlinx.coroutines.flow.map

private val Context.studyPlanDataStore by preferencesDataStore(name = "study_plan")

/** [examDateEpochDay] is the exam date as an epoch-day number (no time zone), or null if unset. */
data class StudyPlanSettings(
    val exam: String = "JEE",
    val questionGoal: Int = 10,
    val examDateEpochDay: Long? = null
)

class StudyPlanPreferences(private val context: Context) {
    private val examKey = stringPreferencesKey("exam")
    private val goalKey = intPreferencesKey("question_goal")
    private val examDateKey = longPreferencesKey("exam_date_epoch_day")
    val settings = context.studyPlanDataStore.data.map {
        StudyPlanSettings(
            it[examKey]?.takeIf { name -> ExamData.exams.any { exam -> exam.name == name } } ?: "JEE",
            (it[goalKey] ?: 10).coerceIn(5, 50),
            it[examDateKey]
        )
    }
    suspend fun save(settings: StudyPlanSettings) {
        context.studyPlanDataStore.edit {
            it[examKey] = settings.exam
            it[goalKey] = settings.questionGoal.coerceIn(5, 50)
            settings.examDateEpochDay?.let { day -> it[examDateKey] = day } ?: it.remove(examDateKey)
        }
    }
}
