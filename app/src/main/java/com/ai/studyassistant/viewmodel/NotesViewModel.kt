package com.ai.studyassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.data.model.StudyNote
import com.ai.studyassistant.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesUiState(
    val isLoading: Boolean = false,
    val note: StudyNote? = null,
    val error: String? = null,
    val savedNotes: List<StudyNote> = emptyList()
)

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotesRepository(application)

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSavedNotes().collect { notes ->
                _uiState.update { it.copy(savedNotes = notes) }
            }
        }
    }

    fun fetchNote(topic: String) {
        _uiState.update { it.copy(isLoading = true, error = null, note = null) }
        viewModelScope.launch {
            repository.fetchSummary(topic)
                .onSuccess { note -> _uiState.update { it.copy(isLoading = false, note = note) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Couldn't fetch notes for $topic.")
                    }
                }
        }
    }

    fun toggleSave() {
        val note = _uiState.value.note ?: return
        viewModelScope.launch {
            if (note.isSaved) repository.deleteNote(note) else repository.saveNote(note)
            _uiState.update { it.copy(note = note.copy(isSaved = !note.isSaved)) }
        }
    }

    fun clearNote() {
        _uiState.update { it.copy(note = null, error = null) }
    }
}
