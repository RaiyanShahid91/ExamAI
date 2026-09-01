package com.ai.studyassistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CachedQuestionEntity::class,
        SavedNoteEntity::class,
        ExamAttemptEntity::class,
        BookmarkedQuestionEntity::class,
        ReviewItemEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class QuizDatabase : RoomDatabase() {

    abstract fun questionDao(): QuestionDao
    abstract fun noteDao(): NoteDao
    abstract fun examAttemptDao(): ExamAttemptDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile private var INSTANCE: QuizDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `exam_attempts` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `exam` TEXT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `topic` TEXT NOT NULL,
                        `startTime` INTEGER NOT NULL,
                        `endTime` INTEGER,
                        `currentIndex` INTEGER NOT NULL,
                        `questions` TEXT NOT NULL,
                        `answers` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `bookmarked_questions` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `exam` TEXT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `topic` TEXT NOT NULL,
                        `question` TEXT NOT NULL,
                        `options` TEXT NOT NULL,
                        `correct` INTEGER NOT NULL,
                        `explanation` TEXT NOT NULL,
                        `bookmarkedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE exam_attempts ADD COLUMN isTimed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE exam_attempts ADD COLUMN durationSeconds INTEGER")
                db.execSQL("ALTER TABLE exam_attempts ADD COLUMN isMockExam INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `review_items` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `exam` TEXT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `topic` TEXT NOT NULL,
                        `question` TEXT NOT NULL,
                        `options` TEXT NOT NULL,
                        `correct` INTEGER NOT NULL,
                        `explanation` TEXT NOT NULL,
                        `box` INTEGER NOT NULL,
                        `dueAt` INTEGER NOT NULL,
                        `lastReviewedAt` INTEGER,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): QuizDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "exam_ai.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { INSTANCE = it }
            }
    }
}
