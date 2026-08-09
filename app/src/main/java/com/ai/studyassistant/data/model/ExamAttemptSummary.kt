package com.ai.studyassistant.data.model

import com.ai.studyassistant.data.local.ExamAttemptEntity

data class ExamAttemptSummary(
    val id: Long,
    val exam: String,
    val subject: String,
    val topic: String,
    val startTime: Long,
    val endTime: Long?,
    val questions: List<Question>,
    val answers: List<Int>,
    val isCompleted: Boolean
) {
    val total: Int get() = questions.size
    val answeredCount: Int get() = answers.count { it != -1 }
    val score: Int get() = answers.indices.count { i -> answers[i] != -1 && answers[i] == questions.getOrNull(i)?.correct }
    val durationMillis: Long? get() = endTime?.let { it - startTime }
}

fun ExamAttemptEntity.toSummary() = ExamAttemptSummary(
    id = id,
    exam = exam,
    subject = subject,
    topic = topic,
    startTime = startTime,
    endTime = endTime,
    questions = questions,
    answers = answers,
    isCompleted = isCompleted
)
