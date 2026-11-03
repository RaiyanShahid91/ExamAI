package com.ai.studyassistant.ui

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ai.studyassistant.data.api.OpenTriviaService
import com.ai.studyassistant.data.local.QuizDatabase
import com.ai.studyassistant.ui.screens.StudyNotesScreen
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.viewmodel.NotesViewModel
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Run separately with emulator networking enabled; failures identify external-service availability. */
@RunWith(AndroidJUnit4::class)
class OnlineFeatureTest {
    @get:Rule val ui = createComposeRule()
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    @Test fun liveTriviaReturnsUsableQuestions() = runBlocking {
        val response = OpenTriviaService.create().getQuestions(amount = 1, category = 17)
        assertTrue("Live trivia returned no questions (code ${response.response_code})", response.results.isNotEmpty())
        assertTrue(response.results.all { it.question.isNotBlank() && it.correct_answer.isNotBlank() })
    }

    @Test fun quizFallsBackQuicklyWhenLiveTriviaIsSlowOrUnavailable() = runBlocking {
        var questions = emptyList<com.ai.studyassistant.data.model.Question>()
        val elapsed = kotlin.system.measureTimeMillis {
            questions = com.ai.studyassistant.data.repository.QuestionRepository(app).getQuiz("JEE", "Physics", "Mechanics").getOrThrow()
        }
        assertTrue(questions.isNotEmpty())
        assertTrue("Question loading took ${elapsed}ms instead of falling back", elapsed < 8000)
    }

    @Test fun liveNotesSearchCanSaveItsResultAndUsesOneCredit() {
        val allowance = app.getSharedPreferences("note_search_allowance", 0)
        val original = allowance.all.toMap()
        val dao = QuizDatabase.getInstance(app).noteDao()
        val priorNote = runBlocking { dao.getByTopic("Photosynthesis") }
        try {
            allowance.edit().putString("day", java.time.LocalDate.now().toString()).putInt("remaining", 2).putInt("free_remaining", 2).apply()
            lateinit var model: NotesViewModel
            ui.setContent {
                model = androidx.lifecycle.viewmodel.compose.viewModel()
                AIStudyAssistantTheme { StudyNotesScreen(model, onBack = {}) }
            }
            ui.onNode(hasSetTextAction()).performTextInput("Photosynthesis")
            ui.onNodeWithContentDescription("Search").performClick()
            ui.waitUntil(30_000) { !model.uiState.value.isLoading && (model.uiState.value.note != null || model.uiState.value.error != null) }
            assertNotNull("Live notes unavailable: ${model.uiState.value.error}", model.uiState.value.note)
            assertEquals(1, allowance.getInt("remaining", 0))
            if (model.uiState.value.note?.isSaved != true) ui.onNodeWithContentDescription("Save note").performScrollTo().performClick()
            ui.waitUntil(5000) { runBlocking { dao.getByTopic("Photosynthesis") != null } }
            assertTrue(runBlocking { dao.getByTopic("Photosynthesis")!!.summary.isNotBlank() })
        } finally {
            runBlocking {
                if (priorNote != null) dao.save(priorNote)
                else dao.getByTopic("Photosynthesis")?.let { dao.delete(it) }
            }
            val editor = allowance.edit().clear()
            original.forEach { (key, value) -> when (value) { is String -> editor.putString(key, value); is Int -> editor.putInt(key, value) } }
            editor.apply()
        }
    }
}
