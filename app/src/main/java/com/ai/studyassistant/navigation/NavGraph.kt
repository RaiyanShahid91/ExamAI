package com.ai.studyassistant.navigation

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
import com.ai.studyassistant.ui.screens.HomeScreen
import com.ai.studyassistant.ui.screens.QuizScreen
import com.ai.studyassistant.ui.screens.ResultScreen
import com.ai.studyassistant.ui.screens.StudyNotesScreen
import com.ai.studyassistant.ui.screens.SubjectScreen
import com.ai.studyassistant.ui.screens.TopicScreen
import com.ai.studyassistant.viewmodel.NotesViewModel
import com.ai.studyassistant.viewmodel.QuizViewModel

private const val ANIM_DURATION = 300

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: QuizViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            fadeIn(tween(ANIM_DURATION)) + slideInHorizontally(tween(ANIM_DURATION)) { it / 4 }
        },
        exitTransition = {
            fadeOut(tween(ANIM_DURATION))
        },
        popEnterTransition = {
            fadeIn(tween(ANIM_DURATION))
        },
        popExitTransition = {
            fadeOut(tween(ANIM_DURATION)) + slideOutHorizontally(tween(ANIM_DURATION)) { it / 4 }
        }
    ) {

        composable(Routes.HOME) {
            HomeScreen(
                quizViewModel = viewModel,
                onExamSelected = { exam -> navController.navigate(Routes.subject(exam)) },
                onNotesClick = { navController.navigate(Routes.NOTES) }
            )
        }

        composable(Routes.NOTES) {
            val notesViewModel: NotesViewModel = viewModel()
            StudyNotesScreen(
                viewModel = notesViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.SUBJECT,
            arguments = listOf(navArgument("exam") { type = NavType.StringType })
        ) { backStackEntry ->
            val exam = backStackEntry.arguments?.getString("exam").orEmpty()
            SubjectScreen(
                exam = exam,
                onBack = { navController.popBackStack() },
                onSubjectSelected = { subject -> navController.navigate(Routes.topic(exam, subject)) }
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
                onTopicSelected = { topic ->
                    navController.navigate(Routes.quiz(exam, subject, topic)) {
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
                navArgument("topic") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val exam = backStackEntry.arguments?.getString("exam").orEmpty()
            val subject = backStackEntry.arguments?.getString("subject").orEmpty()
            val topic = backStackEntry.arguments?.getString("topic").orEmpty()
            QuizScreen(
                exam = exam,
                subject = subject,
                topic = topic,
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
                navArgument("topic") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val exam = backStackEntry.arguments?.getString("exam").orEmpty()
            val subject = backStackEntry.arguments?.getString("subject").orEmpty()
            val topic = backStackEntry.arguments?.getString("topic").orEmpty()
            ResultScreen(
                viewModel = viewModel,
                onTryAgain = {
                    navController.navigate(Routes.quiz(exam, subject, topic)) {
                        popUpTo(Routes.QUIZ) { inclusive = true }
                    }
                },
                onNewTopic = {
                    navController.popBackStack(Routes.TOPIC, inclusive = false)
                }
            )
        }
    }
}
