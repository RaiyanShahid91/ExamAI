package com.ai.studyassistant.data.model

data class StudyNote(
    val topic: String,
    val title: String,
    val summary: String,
    val thumbnailUrl: String? = null,
    val isSaved: Boolean = false
)
