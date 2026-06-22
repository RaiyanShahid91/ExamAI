package com.ai.studyassistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_questions")
data class CachedQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bankKey: String,
    val question: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String
)
