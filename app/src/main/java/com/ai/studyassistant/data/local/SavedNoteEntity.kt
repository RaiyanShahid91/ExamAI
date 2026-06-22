package com.ai.studyassistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_notes")
data class SavedNoteEntity(
    @PrimaryKey val topic: String,
    val title: String,
    val summary: String,
    val thumbnailUrl: String?,
    val savedAt: Long
)
