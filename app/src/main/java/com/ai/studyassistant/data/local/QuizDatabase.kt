package com.ai.studyassistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CachedQuestionEntity::class, SavedNoteEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class QuizDatabase : RoomDatabase() {

    abstract fun questionDao(): QuestionDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: QuizDatabase? = null

        fun getInstance(context: Context): QuizDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "exam_ai.db"
                ).build().also { INSTANCE = it }
            }
    }
}
