package com.ai.studyassistant.data.repository

import android.content.Context
import com.ai.studyassistant.data.api.WikipediaService
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.local.SavedNoteEntity
import com.ai.studyassistant.data.model.StudyNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotesRepository(
    context: Context,
    private val wikipediaService: WikipediaService = WikipediaService.create()
) {
    private val noteDao = QuizDatabase.getInstance(context).noteDao()

    suspend fun fetchSummary(topic: String): Result<StudyNote> {
        return try {
            val dto = wikipediaService.getSummary(topic.replace(" ", "_"))
            val savedAlready = noteDao.getByTopic(topic) != null
            Result.success(
                StudyNote(
                    topic = topic,
                    title = dto.title.ifBlank { topic },
                    summary = dto.extract.ifBlank { dto.description.orEmpty() },
                    thumbnailUrl = dto.thumbnail?.source,
                    isSaved = savedAlready
                )
            )
        } catch (e: Exception) {
            val cached = noteDao.getByTopic(topic)
            if (cached != null) Result.success(cached.toStudyNote()) else Result.failure(e)
        }
    }

    suspend fun saveNote(note: StudyNote) {
        noteDao.save(
            SavedNoteEntity(
                topic = note.topic,
                title = note.title,
                summary = note.summary,
                thumbnailUrl = note.thumbnailUrl,
                savedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNote(note: StudyNote) {
        noteDao.delete(
            SavedNoteEntity(
                topic = note.topic,
                title = note.title,
                summary = note.summary,
                thumbnailUrl = note.thumbnailUrl,
                savedAt = 0L
            )
        )
    }

    fun observeSavedNotes(): Flow<List<StudyNote>> =
        noteDao.observeAll().map { entities -> entities.map { it.toStudyNote() } }

    private fun SavedNoteEntity.toStudyNote() = StudyNote(
        topic = topic,
        title = title,
        summary = summary,
        thumbnailUrl = thumbnailUrl,
        isSaved = true
    )
}
