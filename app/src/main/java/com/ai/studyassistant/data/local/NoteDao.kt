package com.ai.studyassistant.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM saved_notes ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<SavedNoteEntity>>

    @Query("SELECT * FROM saved_notes WHERE topic = :topic LIMIT 1")
    suspend fun getByTopic(topic: String): SavedNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(note: SavedNoteEntity)

    @Delete
    suspend fun delete(note: SavedNoteEntity)
}
