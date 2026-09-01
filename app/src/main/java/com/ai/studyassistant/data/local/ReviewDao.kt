package com.ai.studyassistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {

    @Query("SELECT * FROM review_items WHERE dueAt <= :now ORDER BY dueAt ASC LIMIT :limit")
    suspend fun getDue(now: Long, limit: Int): List<ReviewItemEntity>

    @Query("SELECT COUNT(*) FROM review_items WHERE dueAt <= :now")
    fun observeDueCount(now: Long): Flow<Int>

    @Query("SELECT * FROM review_items WHERE question = :question LIMIT 1")
    suspend fun findByQuestion(question: String): ReviewItemEntity?

    @Insert
    suspend fun insert(item: ReviewItemEntity)

    @Update
    suspend fun update(item: ReviewItemEntity)
}
