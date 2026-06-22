package com.ai.studyassistant.data.repository

import android.content.Context
import com.ai.studyassistant.data.api.WikipediaService
import com.ai.studyassistant.data.local.NetworkUtils
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.data.local.SavedNoteEntity
import com.ai.studyassistant.data.model.StudyNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class NotesRepository(
    private val context: Context,
    private val wikipediaService: WikipediaService = WikipediaService.create()
) {
    private val noteDao = QuizDatabase.getInstance(context).noteDao()

    suspend fun fetchSummary(topic: String): Result<StudyNote> {
        if (!NetworkUtils.isOnline(context)) {
            val cached = noteDao.getByTopic(topic)
            return if (cached != null) {
                Result.success(cached.toStudyNote())
            } else {
                Result.failure(IOException("No internet connection. Connect to Wi-Fi or mobile data and try again."))
            }
        }

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
            if (cached != null) {
                Result.success(cached.toStudyNote())
            } else {
                Result.failure(IOException(friendlyMessage(topic, e)))
            }
        }
    }

    private fun friendlyMessage(topic: String, e: Exception): String = when {
        e is HttpException && e.code() == 404 ->
            "Couldn't find a Wikipedia article for \"$topic\". Try a more specific or different search term."
        e is HttpException && (e.code() == 403 || e.code() == 429) ->
            "Wikipedia is temporarily blocking requests from this network. Please try again in a moment."
        e is IOException ->
            "No internet connection. Connect to Wi-Fi or mobile data and try again."
        else ->
            "Couldn't fetch notes for \"$topic\" right now. Please try again."
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
