package com.ai.studyassistant.data.model

data class SubjectStat(
    val subject: String,
    val correct: Int,
    val total: Int
) {
    val accuracy: Float get() = if (total == 0) 0f else correct.toFloat() / total
}

data class TopicStat(
    val exam: String,
    val subject: String,
    val topic: String,
    val correct: Int,
    val total: Int
) {
    val accuracy: Float get() = if (total == 0) 0f else correct.toFloat() / total
}
