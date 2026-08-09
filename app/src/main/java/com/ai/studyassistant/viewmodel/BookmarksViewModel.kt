package com.ai.studyassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.studyassistant.data.local.BookmarkedQuestionEntity
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.data.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookmarksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookmarkRepository(application)

    private val _bookmarks = MutableStateFlow<List<BookmarkedQuestionEntity>>(emptyList())
    val bookmarks: StateFlow<List<BookmarkedQuestionEntity>> = _bookmarks.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeBookmarks().collect { list -> _bookmarks.update { list } }
        }
    }

    fun removeBookmark(bookmark: BookmarkedQuestionEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(
                bookmark.exam,
                bookmark.subject,
                bookmark.topic,
                Question(bookmark.question, bookmark.options, bookmark.correct, bookmark.explanation)
            )
        }
    }
}
