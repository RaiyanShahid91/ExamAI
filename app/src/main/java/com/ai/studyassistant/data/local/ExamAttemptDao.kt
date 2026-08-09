package com.ai.studyassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamAttemptDao {

    @Query("SELECT * FROM exam_attempts ORDER BY startTime DESC")
    fun observeAll(): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExamAttemptEntity?

    @Query("SELECT * FROM exam_attempts WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ExamAttemptEntity?>

    @Insert
    suspend fun insert(attempt: ExamAttemptEntity): Long

    @Update
    suspend fun update(attempt: ExamAttemptEntity)
}
