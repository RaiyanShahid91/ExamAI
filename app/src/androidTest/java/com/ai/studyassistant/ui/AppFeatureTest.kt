package com.ai.studyassistant.ui

import android.app.Application
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.isSystemInDarkTheme
import com.ai.studyassistant.ui.theme.ThemeMode
import kotlinx.coroutines.flow.first
import com.ai.studyassistant.data.model.ExamData
import com.ai.studyassistant.data.repository.QuestionRepository
import com.ai.studyassistant.notification.ReminderScheduler
import com.ai.studyassistant.ui.util.AppLinks
import java.util.UUID
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ai.studyassistant.data.local.*
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.navigation.*
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.viewmodel.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppFeatureTest {
    @get:Rule val ui = createComposeRule()
    private lateinit var nav: NavHostController
    private lateinit var quiz: QuizViewModel
    private lateinit var database: QuizDatabase
    private val createdAttempts = mutableListOf<Long>()
    private val cleanup = mutableListOf<() -> Unit>()
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private val sample = Question("Feature test: what is 2 + 2?", listOf("4", "5"), 0, "Two plus two equals four.")

    @Before fun launchAppGraph() {
        database = QuizDatabase.getInstance(app)
        ui.setContent {
            nav = rememberNavController()
            quiz = viewModel()
            val update: UpdateViewModel = viewModel()
            val preferences = remember { AppearancePreferences(app) }
            val appearance by preferences.settings.collectAsState(initial = AppearanceSettings())
            val dark = when (appearance.mode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            AIStudyAssistantTheme(darkTheme = dark, palette = appearance.palette) { NavGraph(nav, quiz, update) }
        }
        awaitRoute(Routes.HOME)
    }
    @After fun cleanUpTestAttempts() {
        cleanup.reversed().forEach { it() }
        createdAttempts.forEach { id -> database.openHelper.writableDatabase.execSQL("DELETE FROM exam_attempts WHERE id = ?", arrayOf(id)) }
    }
    private fun awaitRoute(route: String) {
        ui.waitUntil(10_000) { ::nav.isInitialized && nav.currentDestination?.route == route }
        ui.waitForIdle()
    }
    private fun go(route: String) { ui.runOnIdle { nav.navigate(route) } }
    private fun seedAttempt(completed: Boolean = true, timed: Boolean = false, mock: Boolean = false): Long = runBlocking {
        database.examAttemptDao().insert(ExamAttemptEntity(exam = "JEE", subject = if (mock) "All Subjects" else "Physics", topic = if (mock) "Mock Test" else "Mechanics",
            startTime = System.currentTimeMillis() - 10_000, endTime = if (completed) System.currentTimeMillis() else null,
            questions = listOf(sample), answers = listOf(if (completed) 0 else -1), isCompleted = completed,
            isTimed = timed, durationSeconds = if (timed) 1 else null, isMockExam = mock)).also { createdAttempts += it }
    }

    @Test fun newTopicWorksWhenResultHasNoTopicPickerInHistory() {
        val id = seedAttempt()
        ui.runOnIdle { quiz.resumeAttempt(id) }
        go(Routes.result("JEE", "Physics", "Mechanics"))
        awaitRoute(Routes.RESULT)
        ui.onNodeWithText("New Topic").performScrollTo().performClick()
        awaitRoute(Routes.TOPIC)
        ui.onNodeWithText("One topic at a time").assertIsDisplayed()
    }
    private fun tap(text: String) {
        try {
            ui.waitUntil(1000) { ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        } catch (_: ComposeTimeoutException) {
            ui.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(text))
        }
        val node = ui.onNodeWithText(text)
        try { node.assertIsDisplayed() } catch (_: AssertionError) { node.performScrollTo() }
        node.performClick()
    }
    private fun awaitQuiz() {
        awaitRoute(Routes.QUIZ)
        ui.waitUntil(20_000) { quiz.uiState.value.currentQuestion != null && !quiz.uiState.value.isLoading }
        quiz.uiState.value.attemptId?.let { createdAttempts += it }
        ui.waitForIdle()
    }
    private fun finishQuiz() {
        val total = quiz.uiState.value.questions.size
        repeat(total) { index ->
            ui.waitUntil(5000) { quiz.uiState.value.currentIndex == index }
            val answer = quiz.uiState.value.currentQuestion!!.correct
            ui.onNodeWithTag("quiz_answer_$answer").performScrollTo().performClick()
            ui.onNodeWithTag("quiz_next").performScrollTo().performClick()
        }
        awaitRoute(Routes.RESULT)
    }

    @Test fun shortcutQuizCompletesAndTryAgainCreatesFreshAttempt() {
        go(Routes.quiz("JEE", "Physics", "Mechanics"))
        awaitQuiz()
        val id = quiz.uiState.value.attemptId!!
        finishQuiz()
        val total = quiz.uiState.value.questions.size
        ui.waitUntil(5000) { runBlocking { database.examAttemptDao().getById(id)?.let { it.isCompleted && it.answers.all { answer -> answer >= 0 } } == true } }
        assertEquals(total, quiz.uiState.value.score)
        tap("Try Again")
        awaitQuiz()
        assertNotEquals(id, quiz.uiState.value.attemptId)
        assertEquals(0, quiz.uiState.value.currentIndex)
        assertFalse(quiz.uiState.value.isAnswered)
    }

    @Test fun normalExamFlowReturnsToTheExistingTopicPicker() {
        go(Routes.subject("JEE"))
        awaitRoute(Routes.SUBJECT)
        tap("Physics")
        awaitRoute(Routes.TOPIC)
        tap("Mechanics")
        awaitQuiz()
        finishQuiz()
        tap("New Topic")
        awaitRoute(Routes.TOPIC)
        assertEquals("JEE", nav.currentBackStackEntry!!.arguments!!.getString("exam"))
        ui.onNodeWithContentDescription("Back").performClick()
        awaitRoute(Routes.SUBJECT)
    }

    @Test fun allSevenExamsHaveWorkingSubjectAndTopicNavigation() {
        ExamData.exams.forEach { exam ->
            ui.runOnIdle { nav.navigate(Routes.HOME) { popUpTo(Routes.HOME); launchSingleTop = true } }
            go(Routes.subject(exam.name))
            awaitRoute(Routes.SUBJECT)
            val subject = ExamData.subjectsFor(exam.name).first().name
            tap(subject)
            awaitRoute(Routes.TOPIC)
            assertEquals(exam.name, nav.currentBackStackEntry!!.arguments!!.getString("exam"))
            assertEquals(subject, nav.currentBackStackEntry!!.arguments!!.getString("subject"))
            ui.onNodeWithText("One topic at a time").assertIsDisplayed()
        }
    }

    @Test fun allOfflineBanksAndMockExamsContainValidQuestions() = runBlocking {
        val repository = QuestionRepository(app)
        ExamData.exams.forEach { exam ->
            ExamData.subjectsFor(exam.name).forEach { subject ->
                val questions = repository.getQuiz(exam.name, subject.name, ExamData.topicsFor(exam.name, subject.name).first()).getOrThrow()
                assertTrue("${exam.name}/${subject.name}", questions.isNotEmpty())
                assertTrue(questions.all { it.question.isNotBlank() && it.correct in it.options.indices && it.explanation.isNotBlank() })
            }
            assertTrue(repository.getMockExam(exam.name).getOrThrow().isNotEmpty())
        }
    }

    @Test fun expiredMockResumeOpensResultAndReturnsToHistory() {
        val id = seedAttempt(completed = false, timed = true, mock = true)
        go(Routes.quizResume(id))
        awaitRoute(Routes.RESULT)
        tap("Back")
        awaitRoute(Routes.HISTORY)
        ui.waitUntil(5000) { runBlocking { database.examAttemptDao().getById(id)?.isCompleted == true } }
    }

    @Test fun unfinishedQuizCanResumeFinishAndOpenItsReport() {
        val id = seedAttempt(completed = false)
        go(Routes.quizResume(id))
        awaitRoute(Routes.QUIZ_RESUME)
        ui.waitUntil(5000) { quiz.uiState.value.currentQuestion != null }
        ui.onNodeWithTag("quiz_answer_0").performScrollTo().performClick()
        ui.onNodeWithTag("quiz_next").performScrollTo().performClick()
        awaitRoute(Routes.RESULT)
        tap("Back")
        awaitRoute(Routes.HISTORY)
        go(Routes.attemptDetail(id))
        awaitRoute(Routes.ATTEMPT_DETAIL)
        ui.waitUntil(5000) { ui.onAllNodesWithContentDescription("Share Report").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("1 / 1").assertIsDisplayed()
        ui.onNodeWithText(sample.question).assertIsDisplayed()
    }

    @Test fun savedNotesOpenOfflineWithoutUsingSearchAllowance() {
        val unique = "Saved note ${UUID.randomUUID()}"
        runBlocking { database.noteDao().save(SavedNoteEntity(unique, unique, "A saved offline explanation", null, System.currentTimeMillis())) }
        cleanup += { database.openHelper.writableDatabase.execSQL("DELETE FROM saved_notes WHERE topic = ?", arrayOf(unique)) }
        go(Routes.NOTES)
        awaitRoute(Routes.NOTES)
        ui.waitUntil(5000) { ui.onAllNodesWithText(unique).fetchSemanticsNodes().isNotEmpty() }
        val before = app.getSharedPreferences("note_search_allowance", 0).getInt("remaining", 2)
        tap(unique)
        ui.onNodeWithText("A saved offline explanation").assertExists()
        assertEquals(before, app.getSharedPreferences("note_search_allowance", 0).getInt("remaining", 2))
        ui.onNodeWithContentDescription("Save note").performClick()
        ui.waitUntil(5000) { runBlocking { database.noteDao().getByTopic(unique) == null } }
    }

    @Test fun bookmarksCanBeViewedAndRemoved() {
        val question = "Bookmark ${UUID.randomUUID()}"
        runBlocking { database.bookmarkDao().insert(BookmarkedQuestionEntity(exam = "JEE", subject = "Physics", topic = "Mechanics", question = question, options = listOf("Correct", "Wrong"), correct = 0, explanation = "Saved explanation", bookmarkedAt = Long.MAX_VALUE)) }
        cleanup += { database.openHelper.writableDatabase.execSQL("DELETE FROM bookmarked_questions WHERE question = ?", arrayOf(question)) }
        go(Routes.BOOKMARKS)
        awaitRoute(Routes.BOOKMARKS)
        ui.waitUntil(5000) { ui.onAllNodesWithText(question).fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText(question).assertIsDisplayed()
        ui.onAllNodesWithContentDescription("Remove bookmark").onFirst().performClick()
        ui.waitUntil(5000) { runBlocking { database.bookmarkDao().findByQuestion(question) == null } }
    }

    @Test fun reviewAnswersUpdateTheSpacedRevisionSchedule() {
        val question = "Review ${UUID.randomUUID()}"
        runBlocking { database.reviewDao().insert(ReviewItemEntity(exam = "JEE", subject = "Physics", topic = "Mechanics", question = question, options = listOf("Correct test answer", "Wrong test answer"), correct = 0, explanation = "A revision explanation", dueAt = 0, createdAt = System.currentTimeMillis() - 86_400_000)) }
        cleanup += { database.openHelper.writableDatabase.execSQL("DELETE FROM review_items WHERE question = ?", arrayOf(question)) }
        go(Routes.REVIEW)
        awaitRoute(Routes.REVIEW)
        ui.waitUntil(5000) { ui.onAllNodesWithText(question).fetchSemanticsNodes().isNotEmpty() }
        tap("Correct test answer")
        ui.waitUntil(5000) { runBlocking { database.reviewDao().findByQuestion(question)?.box == 2 } }
        val updated = runBlocking { database.reviewDao().findByQuestion(question)!! }
        assertTrue(updated.dueAt > System.currentTimeMillis())
        assertNotNull(updated.lastReviewedAt)
    }

    @Test fun remindersCanBeCreatedEditedAndDeleted() {
        go(Routes.REMINDERS)
        awaitRoute(Routes.REMINDERS)
        val title = "UI reminder ${UUID.randomUUID()}"
        val edited = "Edited $title"
        ui.waitUntil(5000) { ui.onAllNodesWithTag("add_reminder").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithTag("add_reminder").performClick()
        ui.onNodeWithTag("reminder_title").performTextInput(title)
        ui.onNodeWithTag("reminder_note").performTextInput("Review this topic")
        tap("Save reminder")
        ui.waitUntil(5000) { runBlocking { database.personalReminderDao().pending().any { it.title == title } } }
        val item = runBlocking { database.personalReminderDao().pending().first { it.title == title } }
        cleanup += { runBlocking { database.personalReminderDao().delete(item.id); ReminderScheduler.cancel(app, item.id) } }
        ui.onNodeWithTag("reminder_edit_${item.id}").performScrollTo().performClick()
        ui.onNodeWithTag("reminder_title").performTextReplacement(edited)
        tap("Save reminder")
        ui.waitUntil(5000) { runBlocking { database.personalReminderDao().find(item.id)?.title == edited } }
        ui.onNodeWithTag("reminder_delete_${item.id}").performScrollTo().performClick()
        ui.waitUntil(5000) { runBlocking { database.personalReminderDao().find(item.id) == null } }
    }

    @Test fun themeSelectionIsSavedAndAllInformationScreensOpen() {
        val preferences = AppearancePreferences(app)
        val original = runBlocking { preferences.settings.first() }
        cleanup += { runBlocking { preferences.save(original) } }
        go(Routes.SETTINGS)
        awaitRoute(Routes.SETTINGS)
        tap("Violet")
        tap("Dark")
        ui.waitUntil(5000) { runBlocking { preferences.settings.first().let { it.palette == com.ai.studyassistant.ui.theme.AppPalette.VIOLET && it.mode == ThemeMode.DARK } } }
        listOf(Routes.ABOUT, Routes.AI_INFO, Routes.PRIVACY_POLICY, Routes.TERMS).forEach { route ->
            go(route)
            awaitRoute(route)
            ui.onNodeWithContentDescription("Back").assertExists()
        }
        go(Routes.PRIVACY_POLICY)
        awaitRoute(Routes.PRIVACY_POLICY)
        if (ui.onAllNodesWithText("3. Notifications and your choices").fetchSemanticsNodes().isEmpty()) {
            ui.onNode(hasScrollAction()).performScrollToNode(hasText("3. Notifications and your choices"))
        }
        ui.onNodeWithText("3. Notifications and your choices").assertIsDisplayed()
    }

    @Test fun dailyGoalCanBeChangedAndWelcomeTourReturnsHome() {
        val preferences = StudyPlanPreferences(app)
        val original = runBlocking { preferences.settings.first() }
        cleanup += { runBlocking { preferences.save(original) } }
        ui.waitUntil(5000) { ui.onAllNodesWithText("Edit").fetchSemanticsNodes().isNotEmpty() }
        tap("Edit")
        tap("NEET")
        ui.onNode(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsActions.SetProgress)).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress) { it(20f) }
        tap("Save")
        ui.waitUntil(5000) { runBlocking { preferences.settings.first().let { it.exam == "NEET" && it.questionGoal == 20 } } }
        go(Routes.ONBOARDING)
        awaitRoute(Routes.ONBOARDING)
        ui.onNodeWithText("Choose look").performClick()
        ui.onNodeWithText("Let's start learning").performClick()
        awaitRoute(Routes.HOME)
    }

    @Test fun websiteSupportAndScoreSharingLaunchTheExpectedIntents() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val webFilter = IntentFilter(Intent.ACTION_VIEW).apply { addDataScheme("https") }
        val emailFilter = IntentFilter(Intent.ACTION_SENDTO).apply { addDataScheme("mailto") }
        val web = instrumentation.addMonitor(webFilter, Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null), true)
        val email = instrumentation.addMonitor(emailFilter, Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null), true)
        val share = instrumentation.addMonitor(IntentFilter(Intent.ACTION_CHOOSER), Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null), true)
        try {
            go(Routes.ABOUT)
            awaitRoute(Routes.ABOUT)
            tap("Explore exam prep")
            assertEquals(1, web.hits)
            tap("Contact support")
            assertEquals(1, email.hits)
            val id = seedAttempt()
            ui.runOnIdle { quiz.resumeAttempt(id) }
            go(Routes.result("JEE", "Physics", "Mechanics"))
            awaitRoute(Routes.RESULT)
            tap("Share Score")
            assertEquals(1, share.hits)
            assertTrue(java.io.File(app.cacheDir, "share/score_card.png").length() > 0)
            go(Routes.attemptDetail(id))
            awaitRoute(Routes.ATTEMPT_DETAIL)
            ui.waitUntil(5000) { ui.onAllNodesWithContentDescription("Share Report").fetchSemanticsNodes().isNotEmpty() }
            ui.onNodeWithContentDescription("Share Report").performClick()
            assertEquals(2, share.hits)
            go(Routes.SETTINGS)
            awaitRoute(Routes.SETTINGS)
            tap("Share App")
            assertEquals(3, share.hits)
            assertEquals("https://brynflow.com/exams", AppLinks.EXAM_PREP_URL)
            assertEquals("contact@brynflow.com", AppLinks.SUPPORT_EMAIL)
        } finally {
            instrumentation.removeMonitor(web); instrumentation.removeMonitor(email); instrumentation.removeMonitor(share)
        }
    }

    @Test fun concurrentAnswerProgressAndFinishWritesPreserveAllFields() = runBlocking {
        val questions = List(10) { sample.copy(question = "Persistence test $it") }
        val repository = com.ai.studyassistant.data.repository.ExamHistoryRepository(app)
        val id = repository.startAttempt("JEE", "Physics", "Mechanics", questions)
        createdAttempts += id
        kotlinx.coroutines.coroutineScope {
            val jobs = questions.indices.map { index -> launch { repository.recordAnswer(id, index, 0) } }
            val advance = launch { repository.advanceTo(id, 9) }
            val finish = launch { repository.finishAttempt(id) }
            jobs.forEach { it.join() }; advance.join(); finish.join()
        }
        val saved = database.examAttemptDao().getById(id)!!
        assertEquals(List(10) { 0 }, saved.answers)
        assertEquals(9, saved.currentIndex)
        assertTrue(saved.isCompleted)
        assertNotNull(saved.endTime)
    }

    @Test fun offlineSearchFailureKeepsCreditsAndShowsAUsefulError() {
        val allowance = app.getSharedPreferences("note_search_allowance", 0)
        val original = allowance.all.toMap()
        cleanup += {
            val editor = allowance.edit().clear()
            original.forEach { (key, value) -> when (value) { is String -> editor.putString(key, value); is Int -> editor.putInt(key, value) } }
            editor.apply()
        }
        allowance.edit().putString("day", java.time.LocalDate.now().toString()).putInt("remaining", 2).putInt("free_remaining", 2).apply()
        go(Routes.NOTES)
        awaitRoute(Routes.NOTES)
        ui.onNode(hasSetTextAction()).performTextInput("Offline test ${UUID.randomUUID()}")
        ui.onNodeWithContentDescription("Search").performClick()
        ui.waitUntil(5000) { ui.onAllNodesWithText("No internet connection.", substring = true).fetchSemanticsNodes().isNotEmpty() }
        assertEquals(2, allowance.getInt("remaining", 0))
        ui.onNodeWithText("2 searches left • 2 free daily").assertExists()
    }

}
