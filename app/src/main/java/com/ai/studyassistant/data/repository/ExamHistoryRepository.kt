package com.ai.studyassistant.data.repository

import android.content.Context
import com.ai.studyassistant.data.local.ExamAttemptEntity
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.model.ExamAttemptSummary
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.data.model.toSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExamHistoryRepository(context: Context) {

    private val dao = QuizDatabase.getInstance(context).examAttemptDao()

    fun observeAttempts(): Flow<List<ExamAttemptSummary>> =
        dao.observeAll().map { list -> list.map { it.toSummary() } }

    fun observeAttempt(id: Long): Flow<ExamAttemptSummary?> =
        dao.observeById(id).map { it?.toSummary() }

    suspend fun getAttempt(id: Long): ExamAttemptEntity? = dao.getById(id)

    suspend fun startAttempt(
        exam: String,
        subject: String,
        topic: String,
        questions: List<Question>,
        isTimed: Boolean = false,
        durationSeconds: Int? = null,
        isMockExam: Boolean = false
    ): Long =
        dao.insert(
            ExamAttemptEntity(
                exam = exam,
                subject = subject,
                topic = topic,
                startTime = System.currentTimeMillis(),
                currentIndex = 0,
                questions = questions,
                answers = List(questions.size) { -1 },
                isTimed = isTimed,
                durationSeconds = durationSeconds,
                isMockExam = isMockExam
            )
        )

    suspend fun recordAnswer(attemptId: Long, questionIndex: Int, selectedIndex: Int) {
        val attempt = dao.getById(attemptId) ?: return
        val updatedAnswers = attempt.answers.toMutableList().apply {
            if (questionIndex in indices) this[questionIndex] = selectedIndex
        }
        dao.update(attempt.copy(answers = updatedAnswers))
    }

    suspend fun advanceTo(attemptId: Long, currentIndex: Int) {
        val attempt = dao.getById(attemptId) ?: return
        dao.update(attempt.copy(currentIndex = currentIndex))
    }

    suspend fun finishAttempt(attemptId: Long) {
        val attempt = dao.getById(attemptId) ?: return
        dao.update(attempt.copy(isCompleted = true, endTime = System.currentTimeMillis()))
    }
}
