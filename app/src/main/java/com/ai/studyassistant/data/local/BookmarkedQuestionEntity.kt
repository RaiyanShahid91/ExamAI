package com.ai.studyassistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_questions")
data class BookmarkedQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exam: String,
    val subject: String,
    val topic: String,
    val question: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String,
    val bookmarkedAt: Long
)
