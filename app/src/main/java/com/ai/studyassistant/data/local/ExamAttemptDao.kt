package com.ai.studyassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamAttemptDao {

    @Query("SELECT * FROM exam_attempts ORDER BY startTime DESC")
    fun observeAll(): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts ORDER BY startTime DESC")
    suspend fun getAll(): List<ExamAttemptEntity>

    @Query("SELECT * FROM exam_attempts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExamAttemptEntity?

    @Query("SELECT * FROM exam_attempts WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ExamAttemptEntity?>

    @Insert
    suspend fun insert(attempt: ExamAttemptEntity): Long

    @Query("UPDATE exam_attempts SET answers = :answers WHERE id = :id")
    suspend fun updateAnswers(id: Long, answers: List<Int>)

    @Query("UPDATE exam_attempts SET currentIndex = :index WHERE id = :id")
    suspend fun advance(id: Long, index: Int)

    @Query("UPDATE exam_attempts SET isCompleted = 1, endTime = :endTime WHERE id = :id")
    suspend fun finish(id: Long, endTime: Long)

    @Transaction
    suspend fun recordAnswer(id: Long, questionIndex: Int, answer: Int) {
        val attempt = getById(id) ?: return
        if (questionIndex !in attempt.questions.indices || answer !in attempt.questions[questionIndex].options.indices) return
        val answers = attempt.answers.toMutableList()
        if (questionIndex !in answers.indices) return
        answers[questionIndex] = answer
        updateAnswers(id, answers)
    }

    @Update
    suspend fun update(attempt: ExamAttemptEntity)
}
