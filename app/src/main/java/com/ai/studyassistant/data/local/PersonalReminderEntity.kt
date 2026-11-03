package com.ai.studyassistant.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "personal_reminders")
data class PersonalReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val note: String,
    val scheduledAt: Long,
    val delivered: Boolean = false
)

@Dao
interface PersonalReminderDao {
    @Query("SELECT * FROM personal_reminders ORDER BY delivered ASC, scheduledAt ASC")
    fun observeAll(): Flow<List<PersonalReminderEntity>>
    @Query("SELECT * FROM personal_reminders WHERE delivered = 0")
    suspend fun pending(): List<PersonalReminderEntity>
    @Query("SELECT * FROM personal_reminders WHERE id = :id")
    suspend fun find(id: String): PersonalReminderEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(reminder: PersonalReminderEntity)
    @Query("DELETE FROM personal_reminders WHERE id = :id")
    suspend fun delete(id: String)
    @Query("UPDATE personal_reminders SET delivered = 1 WHERE id = :id AND delivered = 0")
    suspend fun claim(id: String): Int
}
