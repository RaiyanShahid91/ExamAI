package com.ai.studyassistant.data.repository

import android.content.Context
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.local.ReviewItemEntity
import com.ai.studyassistant.data.model.Question
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private const val MAX_BOX = 5
private const val SESSION_LIMIT = 20

/**
 * Leitner-box scheduling: a wrong answer drops an item to box 1 (due next day); each
 * correct review in a session advances it a box, doubling roughly how long the interval
 * gets before it resurfaces. A wrong review during a session sends it straight back to box 1.
 */
private val BOX_INTERVAL_DAYS = mapOf(1 to 1L, 2 to 3L, 3 to 7L, 4 to 14L, 5 to 30L)

class ReviewRepository(context: Context) {

    private val dao = QuizDatabase.getInstance(context).reviewDao()

    fun observeDueCount(): Flow<Int> = dao.observeDueCount(System.currentTimeMillis())

    suspend fun getDueQuestions(limit: Int = SESSION_LIMIT): List<ReviewItemEntity> =
        dao.getDue(System.currentTimeMillis(), limit)

    suspend fun upsertOnWrongAnswer(exam: String, subject: String, topic: String, question: Question) {
        val now = System.currentTimeMillis()
        val existing = dao.findByQuestion(question.question)
        if (existing != null) {
            dao.update(existing.copy(box = 1, dueAt = dueAtForBox(1, now)))
        } else {
            dao.insert(
                ReviewItemEntity(
                    exam = exam,
                    subject = subject,
                    topic = topic,
                    question = question.question,
                    options = question.options,
                    correct = question.correct,
                    explanation = question.explanation,
                    box = 1,
                    dueAt = dueAtForBox(1, now),
                    createdAt = now
                )
            )
        }
    }

    suspend fun recordResult(item: ReviewItemEntity, wasCorrect: Boolean) {
        val now = System.currentTimeMillis()
        val newBox = if (wasCorrect) (item.box + 1).coerceAtMost(MAX_BOX) else 1
        dao.update(item.copy(box = newBox, dueAt = dueAtForBox(newBox, now), lastReviewedAt = now))
    }

    private fun dueAtForBox(box: Int, now: Long): Long {
        val days = BOX_INTERVAL_DAYS[box] ?: 1L
        return now + TimeUnit.DAYS.toMillis(days)
    }
}
