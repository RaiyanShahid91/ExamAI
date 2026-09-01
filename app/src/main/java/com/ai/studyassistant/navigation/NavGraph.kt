package com.ai.studyassistant.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ai.studyassistant.analytics.AnalyticsManager
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
import com.ai.studyassistant.ui.screens.ReviewScreen
import com.ai.studyassistant.ui.screens.SettingsScreen
import com.ai.studyassistant.ui.screens.StudyNotesScreen
import com.ai.studyassistant.ui.screens.SubjectScreen
import com.ai.studyassistant.ui.screens.TermsAndConditionsScreen
import com.ai.studyassistant.ui.screens.TopicScreen
import com.ai.studyassistant.viewmodel.BookmarksViewModel
import com.ai.studyassistant.viewmodel.HistoryViewModel
import com.ai.studyassistant.viewmodel.NotesViewModel
import com.ai.studyassistant.viewmodel.QuizViewModel
import com.ai.studyassistant.viewmodel.ReviewViewModel
import com.ai.studyassistant.viewmodel.UpdateViewModel

private const val ANIM_DURATION = 300

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: QuizViewModel,
    updateViewModel: UpdateViewModel,
    startDestination: String = Routes.HOME
) {
    // One listener covers every screen in the app — logs a screen_view on every
    // navigation instead of hand-wiring analytics into each composable.
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route ?: return@OnDestinationChangedListener
            val screenName = route.substringBefore("/").substringBefore("?")
            AnalyticsManager.logScreenView(screenName = screenName, screenClass = route)
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

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
                onExamSelected = { exam ->
                    AnalyticsManager.logEvent("exam_selected", mapOf("exam" to exam))
                    navController.navigate(Routes.subject(exam))
                },
                onNotesClick = {
                    AnalyticsManager.logEvent("bottom_nav_clicked", mapOf("tab" to "notes"))
                    navController.navigate(Routes.NOTES)
                },
                onSettingsClick = {
                    AnalyticsManager.logEvent("bottom_nav_clicked", mapOf("tab" to "settings"))
                    navController.navigate(Routes.SETTINGS)
                },
                onHistoryClick = {
                    AnalyticsManager.logEvent("bottom_nav_clicked", mapOf("tab" to "history"))
                    navController.navigate(Routes.HISTORY)
                },
                onBookmarksClick = {
                    AnalyticsManager.logEvent("bottom_nav_clicked", mapOf("tab" to "bookmarks"))
                    navController.navigate(Routes.BOOKMARKS)
                },
                onReviewClick = {
                    AnalyticsManager.logEvent("review_card_clicked")
                    navController.navigate(Routes.REVIEW)
                },
                onPracticeTopic = { exam, subject, topic ->
                    AnalyticsManager.logEvent(
                        "weak_topic_practice_started",
                        mapOf("exam" to exam, "subject" to subject, "topic" to topic)
                    )
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
                updateViewModel = updateViewModel,
                onBack = {
                    AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "settings"))
                    navController.popBackStack()
                },
                onAboutClick = {
                    AnalyticsManager.logEvent("settings_row_clicked", mapOf("row" to "about"))
                    navController.navigate(Routes.ABOUT)
                },
                onTermsClick = {
                    AnalyticsManager.logEvent("settings_row_clicked", mapOf("row" to "terms"))
                    navController.navigate(Routes.TERMS)
                },
                onPrivacyClick = {
                    AnalyticsManager.logEvent("settings_row_clicked", mapOf("row" to "privacy"))
                    navController.navigate(Routes.PRIVACY_POLICY)
                },
                onAiInfoClick = {
                    AnalyticsManager.logEvent("settings_row_clicked", mapOf("row" to "ai_info"))
                    navController.navigate(Routes.AI_INFO)
                }
            )
        }

        composable(Routes.TERMS) {
            TermsAndConditionsScreen(onBack = {
                AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "terms"))
                navController.popBackStack()
            })
        }

        composable(Routes.AI_INFO) {
            AiInfoScreen(onBack = {
                AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "ai_info"))
                navController.popBackStack()
            })
        }

        composable(Routes.BOOKMARKS) {
            val bookmarksViewModel: BookmarksViewModel = viewModel()
            BookmarksScreen(
                viewModel = bookmarksViewModel,
                onBack = {
                    AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "bookmarks"))
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.REVIEW) {
            val reviewViewModel: ReviewViewModel = viewModel()
            ReviewScreen(
                viewModel = reviewViewModel,
                onBack = {
                    AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "review"))
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HISTORY) {
            val historyViewModel: HistoryViewModel = viewModel()
            HistoryScreen(
                viewModel = historyViewModel,
                onBack = {
                    AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "history"))
                    navController.popBackStack()
                },
                onResume = { attemptId ->
                    AnalyticsManager.logEvent("attempt_resumed", mapOf("attempt_id" to attemptId))
                    navController.navigate(Routes.quizResume(attemptId))
                },
                onOpenDetail = { attemptId ->
                    AnalyticsManager.logEvent("attempt_detail_opened", mapOf("attempt_id" to attemptId))
                    navController.navigate(Routes.attemptDetail(attemptId))
                }
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
                onBack = {
                    AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "attempt_detail"))
                    navController.popBackStack()
                }
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
                onBack = {
                    AnalyticsManager.logEvent("quiz_abandoned", mapOf("resumed" to true))
                    navController.popBackStack()
                },
                onFinished = {
                    val state = viewModel.uiState.value
                    AnalyticsManager.logEvent(
                        "quiz_finished",
                        mapOf(
                            "exam" to state.exam, "subject" to state.subject, "topic" to state.topic,
                            "score" to state.score, "total" to state.questions.size, "resumed" to true
                        )
                    )
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
                onBack = {
                    AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "notes"))
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = {
                AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "about"))
                navController.popBackStack()
            })
        }

        composable(Routes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(onBack = {
                AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "privacy_policy"))
                navController.popBackStack()
            })
        }

        composable(
            Routes.SUBJECT,
            arguments = listOf(navArgument("exam") { type = NavType.StringType })
        ) { backStackEntry ->
            val exam = backStackEntry.arguments?.getString("exam").orEmpty()
            SubjectScreen(
                exam = exam,
                onBack = {
                    AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "subject"))
                    navController.popBackStack()
                },
                onSubjectSelected = { subject ->
                    AnalyticsManager.logEvent("subject_selected", mapOf("exam" to exam, "subject" to subject))
                    navController.navigate(Routes.topic(exam, subject))
                },
                onMockExam = {
                    AnalyticsManager.logEvent("mock_exam_started", mapOf("exam" to exam))
                    navController.navigate(Routes.mockQuiz(exam))
                }
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
                onBack = {
                    AnalyticsManager.logEvent("quiz_abandoned", mapOf("exam" to exam, "mock_exam" to true))
                    navController.popBackStack()
                },
                onFinished = {
                    val state = viewModel.uiState.value
                    AnalyticsManager.logEvent(
                        "quiz_finished",
                        mapOf(
                            "exam" to state.exam, "subject" to state.subject, "topic" to state.topic,
                            "score" to state.score, "total" to state.questions.size, "mock_exam" to true
                        )
                    )
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
                onBack = {
                    AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "topic"))
                    navController.popBackStack()
                },
                onTopicSelected = { topic, difficulty, timed ->
                    AnalyticsManager.logEvent(
                        "topic_selected",
                        mapOf(
                            "exam" to exam,
                            "subject" to subject,
                            "topic" to topic,
                            "difficulty" to (difficulty ?: "any"),
                            "timed" to timed
                        )
                    )
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
                onBack = {
                    AnalyticsManager.logEvent(
                        "quiz_abandoned",
                        mapOf("exam" to exam, "subject" to subject, "topic" to topic)
                    )
                    navController.popBackStack()
                },
                onFinished = {
                    val state = viewModel.uiState.value
                    AnalyticsManager.logEvent(
                        "quiz_finished",
                        mapOf(
                            "exam" to exam, "subject" to subject, "topic" to topic,
                            "score" to state.score, "total" to state.questions.size
                        )
                    )
                    navController.navigate(Routes.result(exam, subject, topic))
                }
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
                    AnalyticsManager.logEvent("result_try_again", mapOf("exam" to exam, "subject" to subject, "topic" to topic))
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
                    AnalyticsManager.logEvent("result_new_topic", mapOf("exam" to exam, "subject" to subject))
                    navController.popBackStack(Routes.TOPIC, inclusive = false)
                },
                onBack = {
                    AnalyticsManager.logEvent("back_pressed", mapOf("screen" to "result"))
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
