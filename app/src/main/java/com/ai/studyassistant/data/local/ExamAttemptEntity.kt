package com.ai.studyassistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ai.studyassistant.data.model.Question

@Entity(tableName = "exam_attempts")
data class ExamAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exam: String,
    val subject: String,
    val topic: String,
    val startTime: Long,
    val endTime: Long? = null,
    val currentIndex: Int = 0,
    val questions: List<Question>,
    val answers: List<Int>,
    val isCompleted: Boolean = false,
    val isTimed: Boolean = false,
    val durationSeconds: Int? = null,
    val isMockExam: Boolean = false
)
