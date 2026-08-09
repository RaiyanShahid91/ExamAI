package com.ai.studyassistant.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ai.studyassistant.ui.screens.AboutScreen
import com.ai.studyassistant.ui.screens.AiInfoScreen
import com.ai.studyassistant.ui.screens.AttemptDetailScreen
import com.ai.studyassistant.ui.screens.BookmarksScreen
import com.ai.studyassistant.ui.screens.HistoryScreen
import com.ai.studyassistant.ui.screens.HomeScreen
import com.ai.studyassistant.ui.screens.OnboardingScreen
import com.ai.studyassistant.ui.screens.PrivacyPolicyScreen
import com.ai.studyassistant.ui.screens.QuizScreen
import com.ai.studyassistant.ui.screens.ResultScreen
import com.ai.studyassistant.ui.screens.SettingsScreen
import com.ai.studyassistant.ui.screens.StudyNotesScreen
import com.ai.studyassistant.ui.screens.SubjectScreen
import com.ai.studyassistant.ui.screens.TermsAndConditionsScreen
import com.ai.studyassistant.ui.screens.TopicScreen
import com.ai.studyassistant.viewmodel.BookmarksViewModel
import com.ai.studyassistant.viewmodel.HistoryViewModel
import com.ai.studyassistant.viewmodel.NotesViewModel
import com.ai.studyassistant.viewmodel.QuizViewModel

private const val ANIM_DURATION = 300

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: QuizViewModel,
    startDestination: String = Routes.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(tween(ANIM_DURATION)) + slideInHorizontally(
                spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
            ) { it / 4 }
        },
        exitTransition = {
            fadeOut(tween(ANIM_DURATION))
        },
        popEnterTransition = {
            fadeIn(tween(ANIM_DURATION))
        },
        popExitTransition = {
            fadeOut(tween(ANIM_DURATION)) + slideOutHorizontally(
                spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
            ) { it / 4 }
        }
    ) {

        composable(Routes.HOME) {
            HomeScreen(
                quizViewModel = viewModel,
                onExamSelected = { exam -> navController.navigate(Routes.subject(exam)) },
                onNotesClick = { navController.navigate(Routes.NOTES) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onHistoryClick = { navController.navigate(Routes.HISTORY) },
                onBookmarksClick = { navController.navigate(Routes.BOOKMARKS) },
                onPracticeTopic = { exam, subject, topic ->
                    navController.navigate(Routes.quiz(exam, subject, topic))
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onAboutClick = { navController.navigate(Routes.ABOUT) },
                onTermsClick = { navController.navigate(Routes.TERMS) },
                onPrivacyClick = { navController.navigate(Routes.PRIVACY_POLICY) },
                onAiInfoClick = { navController.navigate(Routes.AI_INFO) }
            )
        }

        composable(Routes.TERMS) {
            TermsAndConditionsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.AI_INFO) {
            AiInfoScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.BOOKMARKS) {
            val bookmarksViewModel: BookmarksViewModel = viewModel()
            BookmarksScreen(
                viewModel = bookmarksViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            val historyViewModel: HistoryViewModel = viewModel()
            HistoryScreen(
                viewModel = historyViewModel,
                onBack = { navController.popBackStack() },
                onResume = { attemptId -> navController.navigate(Routes.quizResume(attemptId)) },
                onOpenDetail = { attemptId -> navController.navigate(Routes.attemptDetail(attemptId)) }
            )
        }

        composable(
            Routes.ATTEMPT_DETAIL,
            arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
        ) { backStackEntry ->
            val historyViewModel: HistoryViewModel = viewModel()
            val attemptId = backStackEntry.arguments?.getLong("attemptId") ?: 0L
            AttemptDetailScreen(
                attemptId = attemptId,
                viewModel = historyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.QUIZ_RESUME,
            arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
        ) { backStackEntry ->
            val attemptId = backStackEntry.arguments?.getLong("attemptId") ?: 0L
            QuizScreen(
                attemptId = attemptId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onFinished = {
                    val state = viewModel.uiState.value
                    navController.navigate(
                        Routes.result(state.exam, state.subject, state.topic, fromHistory = true)
                    ) {
                        popUpTo(Routes.QUIZ_RESUME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.NOTES) {
            val notesViewModel: NotesViewModel = viewModel()
            StudyNotesScreen(
                viewModel = notesViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }

        composable(
            Routes.SUBJECT,
            arguments = listOf(navArgument("exam") { type = NavType.StringType })
        ) { backStackEntry ->
            val exam = backStackEntry.arguments?.getString("exam").orEmpty()
            SubjectScreen(
                exam = exam,
                onBack = { navController.popBackStack() },
                onSubjectSelected = { subject -> navController.navigate(Routes.topic(exam, subject)) },
                onMockExam = { navController.navigate(Routes.mockQuiz(exam)) }
            )
        }

        composable(
            Routes.MOCK_QUIZ,
            arguments = listOf(
                navArgument("exam") { type = NavType.StringType },
                navArgument("timed") { type = NavType.BoolType; defaultValue = true }
            )
        ) { backStackEntry ->
            val exam = backStackEntry.arguments?.getString("exam").orEmpty()
            val timed = backStackEntry.arguments?.getBoolean("timed") ?: true
            QuizScreen(
                mockExam = exam,
                timed = timed,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onFinished = {
                    val state = viewModel.uiState.value
                    navController.navigate(Routes.result(state.exam, state.subject, state.topic)) {
                        popUpTo(Routes.MOCK_QUIZ) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Routes.TOPIC,
            arguments = listOf(
                navArgument("exam") { type = NavType.StringType },
                navArgument("subject") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val exam = backStackEntry.arguments?.getString("exam").orEmpty()
            val subject = backStackEntry.arguments?.getString("subject").orEmpty()
            TopicScreen(
                exam = exam,
                subject = subject,
                onBack = { navController.popBackStack() },
                onTopicSelected = { topic, difficulty, timed ->
                    navController.navigate(Routes.quiz(exam, subject, topic, difficulty, timed)) {
                        popUpTo(Routes.QUIZ) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Routes.QUIZ,
            arguments = listOf(
                navArgument("exam") { type = NavType.StringType },
                navArgument("subject") { type = NavType.StringType },
                navArgument("topic") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType; defaultValue = "" },
                navArgument("timed") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val exam = backStackEntry.arguments?.getString("exam").orEmpty()
            val subject = backStackEntry.arguments?.getString("subject").orEmpty()
            val topic = backStackEntry.arguments?.getString("topic").orEmpty()
            val difficulty = backStackEntry.arguments?.getString("difficulty").takeUnless { it.isNullOrBlank() }
            val timed = backStackEntry.arguments?.getBoolean("timed") ?: false
            QuizScreen(
                exam = exam,
                subject = subject,
                topic = topic,
                difficulty = difficulty,
                timed = timed,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onFinished = { navController.navigate(Routes.result(exam, subject, topic)) }
            )
        }

        composable(
            Routes.RESULT,
            arguments = listOf(
                navArgument("exam") { type = NavType.StringType },
                navArgument("subject") { type = NavType.StringType },
                navArgument("topic") { type = NavType.StringType },
                navArgument("fromHistory") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val exam = backStackEntry.arguments?.getString("exam").orEmpty()
            val subject = backStackEntry.arguments?.getString("subject").orEmpty()
            val topic = backStackEntry.arguments?.getString("topic").orEmpty()
            val fromHistory = backStackEntry.arguments?.getBoolean("fromHistory") ?: false
            val isMockExam = subject == "All Subjects"
            ResultScreen(
                viewModel = viewModel,
                fromHistory = fromHistory || isMockExam,
                onTryAgain = {
                    if (isMockExam) {
                        navController.navigate(Routes.mockQuiz(exam)) {
                            popUpTo(Routes.RESULT) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.quiz(exam, subject, topic)) {
                            popUpTo(if (fromHistory) Routes.RESULT else Routes.QUIZ) { inclusive = true }
                        }
                    }
                },
                onNewTopic = {
                    navController.popBackStack(Routes.TOPIC, inclusive = false)
                },
                onBack = {
                    if (isMockExam) {
                        navController.popBackStack(Routes.SUBJECT, inclusive = false)
                    } else {
                        navController.popBackStack(Routes.HISTORY, inclusive = false)
                    }
                }
            )
        }
    }
}
