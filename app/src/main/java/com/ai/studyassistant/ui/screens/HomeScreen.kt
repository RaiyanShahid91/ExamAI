package com.ai.studyassistant.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.data.local.FirstRunPreferences
import com.ai.studyassistant.data.model.Exam
import com.ai.studyassistant.data.model.ExamData
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.data.model.TopicStat
import com.ai.studyassistant.data.repository.FlashcardRepository
import com.ai.studyassistant.data.repository.ReviewRepository
import com.ai.studyassistant.notification.DailyReminderWorker
import com.ai.studyassistant.notification.ReminderPreferences
import com.ai.studyassistant.ui.components.BannerAdView
import com.ai.studyassistant.ui.components.GradientBackground
import com.ai.studyassistant.ui.components.OfflineBanner
import com.ai.studyassistant.ui.components.StaggeredEntrance
import com.ai.studyassistant.ui.components.bounceClick
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.CorrectGreen
import com.ai.studyassistant.ui.theme.BrandNavy
import com.ai.studyassistant.ui.theme.GradientTokens
import com.ai.studyassistant.viewmodel.QuizViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.studyassistant.viewmodel.StudyPlanViewModel
import com.ai.studyassistant.data.model.DailyStudyPlan
import com.ai.studyassistant.data.local.StudyPlanSettings
import com.ai.studyassistant.ui.components.DailyStudyPlanCard
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R
import androidx.compose.ui.res.pluralStringResource

@Composable
fun HomeScreen(
    quizViewModel: QuizViewModel,
    onExamSelected: (String) -> Unit,
    onNotesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onReviewClick: () -> Unit = {},
    onRemindersClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onFlashcardsClick: () -> Unit = {},
    onPracticeTopic: (String, String, String) -> Unit
) {
    val studyPlanViewModel: StudyPlanViewModel = viewModel()
    val studyPlan by studyPlanViewModel.plan.collectAsState()
    val dailyQuestion by quizViewModel.dailyQuestion.collectAsState()
    val streak by quizViewModel.streak.collectAsState()
    val weakestTopic by quizViewModel.weakestTopic.collectAsState()
    val isOffline by quizViewModel.isOffline.collectAsState()

    val context = LocalContext.current
    val reviewRepository = remember { ReviewRepository(context) }
    val dueReviewCount by reviewRepository.observeDueCount().collectAsState(initial = 0)
    val flashcardRepository = remember { FlashcardRepository(context) }
    val flashcardCount by remember { flashcardRepository.observeCards() }.collectAsState(initial = emptyList())

    HomeScreenContent(
        studyPlan = studyPlan,
        onSavePlan = studyPlanViewModel::save,
        dailyQuestion = dailyQuestion,
        streak = streak,
        weakestTopic = weakestTopic,
        dueReviewCount = dueReviewCount,
        flashcardCount = flashcardCount.size,
        isOffline = isOffline,
        onExamSelected = onExamSelected,
        onNotesClick = onNotesClick,
        onAboutClick = onSettingsClick,
        onHistoryClick = onHistoryClick,
        onBookmarksClick = onBookmarksClick,
        onReviewClick = onReviewClick,
        onRemindersClick = onRemindersClick,
        onInsightsClick = onInsightsClick,
        onFlashcardsClick = onFlashcardsClick,
        onPracticeTopic = onPracticeTopic
    )
}

@Composable
private fun HomeScreenContent(
    dailyQuestion: Question?,
    studyPlan: DailyStudyPlan? = null,
    onSavePlan: (StudyPlanSettings) -> Unit = {},
    streak: Int = 0,
    weakestTopic: TopicStat? = null,
    dueReviewCount: Int = 0,
    flashcardCount: Int = 0,
    isOffline: Boolean = false,
    onExamSelected: (String) -> Unit,
    onNotesClick: () -> Unit,
    onAboutClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onBookmarksClick: () -> Unit = {},
    onReviewClick: () -> Unit = {},
    onRemindersClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onFlashcardsClick: () -> Unit = {},
    onPracticeTopic: (String, String, String) -> Unit = { _, _, _ -> }
) {
    Scaffold(
        topBar = {
            Column {
                androidx.compose.material3.TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp).size(22.dp))
                            }
                            Text(stringResource(R.string.home_examai), style = MaterialTheme.typography.titleLarge)
                        }
                    },
                    actions = {
                        IconButton(onClick = onRemindersClick) { Icon(Icons.Default.EventAvailable, stringResource(R.string.common_my_reminders)) }
                        IconButton(onClick = onAboutClick) { Icon(Icons.Default.Settings, stringResource(R.string.home_settings_appearance)) }
                    },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                if (isOffline) OfflineBanner()
            }
        },
        bottomBar = {
            Column {
                BannerAdView()
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text(stringResource(R.string.home_home), maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) })
                    NavigationBarItem(selected = false, onClick = onNotesClick, icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, null) }, label = { Text(stringResource(R.string.home_notes), maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) })
                    NavigationBarItem(selected = false, onClick = onHistoryClick, icon = { Icon(Icons.Default.History, null) }, label = { Text(stringResource(R.string.history_history), maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) })
                    NavigationBarItem(selected = false, onClick = onBookmarksClick, icon = { Icon(Icons.Default.Bookmark, null) }, label = { Text(stringResource(R.string.home_saved), maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) })
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.home_a_little_progress_every_day), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.home_your_next_chapter_nstarts_here), style = MaterialTheme.typography.headlineLarge)
                    Text(stringResource(R.string.home_build_confidence_one_question_at), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardStat(pluralStringResource(R.plurals.home_streak_days, streak, streak), stringResource(R.string.home_current_streak), Icons.Default.LocalFireDepartment, Modifier.weight(1f), onInsightsClick)
                    DashboardStat("$dueReviewCount", stringResource(R.string.home_reviews_due), Icons.Default.EventAvailable, Modifier.weight(1f), onReviewClick)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardStat("$flashcardCount", pluralStringResource(R.plurals.home_flashcards_label, flashcardCount), Icons.Default.Style, Modifier.weight(1f), onFlashcardsClick)
                    DashboardStat(stringResource(R.string.home_view), stringResource(R.string.home_your_insights), Icons.Default.Insights, Modifier.weight(1f), onInsightsClick)
                }
            }
            studyPlan?.let { plan ->
                item(key = "daily_study_plan") { DailyStudyPlanCard(plan, onSavePlan, onPracticeTopic, onReviewClick) }
            }
            item { com.ai.studyassistant.ui.components.BrynflowLearningCard("home") }
            item { com.ai.studyassistant.ui.components.FocusRewardCard("home_focus") }
            item { HomeSectionHeading(stringResource(R.string.home_explore_your_exams), stringResource(R.string.home_choose_a_path)) }
            itemsIndexed(ExamData.exams.chunked(2)) { _, pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { exam ->
                        Box(Modifier.weight(1f)) { ExamCard(exam) { onExamSelected(exam.name) } }
                    }
                    if (pair.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                }
            }
            if (dailyQuestion != null) {
                item { HomeSectionHeading(stringResource(R.string.home_quick_brain_boost), stringResource(R.string.home_something_new)) }
                item { DailyQuestionCard(dailyQuestion) }
            }
            if (weakestTopic != null) {
                item { WeakTopicCard(weakestTopic) { onPracticeTopic(weakestTopic.exam, weakestTopic.subject, weakestTopic.topic) } }
            }
        }
    }
}

@Composable
private fun HomeSectionHeading(title: String, subtitle: String) {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DashboardStat(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit = {}) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DailyQuestionCard(question: Question) {
    var revealed by remember(question.question) { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        onClick = {
            revealed = !revealed
            if (revealed) AnalyticsManager.logEvent("daily_question_revealed")
        },
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.home_question_of_the_day),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (revealed) {
                Text(
                    text = stringResource(R.string.home_answer, question.options.getOrNull(question.correct).orEmpty()),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = CorrectGreen,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.home_tap_to_reveal_answer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun WeakTopicCard(stat: TopicStat, onPractice: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        onClick = onPractice,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_practice_weak_spot, stat.topic),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.home_accuracy, (stat.accuracy * 100).toInt(), stat.subject),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExamCard(exam: Exam, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().bounceClick()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(exam.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp).size(24.dp))
            }
            Text(exam.name, style = MaterialTheme.typography.titleLarge)
            Text(stringResource(exam.taglineRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, minLines = 2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.home_explore), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private val sampleDailyQuestion = Question(
    question = "Which gas do plants absorb from the atmosphere for photosynthesis?",
    options = listOf("Oxygen", "Carbon dioxide", "Nitrogen", "Hydrogen"),
    correct = 1,
    explanation = "Plants absorb carbon dioxide and release oxygen during photosynthesis."
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    AIStudyAssistantTheme {
        HomeScreenContent(
            dailyQuestion = sampleDailyQuestion,
            streak = 3,
            weakestTopic = TopicStat(
                exam = "JEE",
                subject = "Physics",
                topic = "Thermodynamics",
                correct = 4,
                total = 10
            ),
            onExamSelected = {},
            onNotesClick = {},
            onAboutClick = {},
            onHistoryClick = {},
            onBookmarksClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyQuestionCardPreview() {
    AIStudyAssistantTheme {
        DailyQuestionCard(question = sampleDailyQuestion)
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamCardPreview() {
    AIStudyAssistantTheme {
        ExamCard(exam = ExamData.exams.first(), onClick = {})
    }
}
