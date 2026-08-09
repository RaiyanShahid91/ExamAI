package com.ai.studyassistant.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.ai.studyassistant.data.local.FirstRunPreferences
import com.ai.studyassistant.data.model.Exam
import com.ai.studyassistant.data.model.ExamData
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.data.model.TopicStat
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

@Composable
fun HomeScreen(
    quizViewModel: QuizViewModel,
    onExamSelected: (String) -> Unit,
    onNotesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onPracticeTopic: (String, String, String) -> Unit
) {
    val dailyQuestion by quizViewModel.dailyQuestion.collectAsState()
    val streak by quizViewModel.streak.collectAsState()
    val weakestTopic by quizViewModel.weakestTopic.collectAsState()
    val isOffline by quizViewModel.isOffline.collectAsState()

    val context = LocalContext.current
    val firstRunPreferences = remember { FirstRunPreferences(context) }
    val reminderPreferences = remember { ReminderPreferences(context) }
    val hasPromptedNotification by firstRunPreferences.hasPromptedNotificationPermission.collectAsState(initial = true)
    val scope = rememberCoroutineScope()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch { reminderPreferences.setEnabled(true) }
            DailyReminderWorker.schedule(context)
        }
        scope.launch { firstRunPreferences.setPromptedNotificationPermission() }
    }

    LaunchedEffect(hasPromptedNotification) {
        if (!hasPromptedNotification) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val alreadyGranted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (alreadyGranted) {
                    firstRunPreferences.setPromptedNotificationPermission()
                } else {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                firstRunPreferences.setPromptedNotificationPermission()
            }
        }
    }

    HomeScreenContent(
        dailyQuestion = dailyQuestion,
        streak = streak,
        weakestTopic = weakestTopic,
        isOffline = isOffline,
        onExamSelected = onExamSelected,
        onNotesClick = onNotesClick,
        onAboutClick = onSettingsClick,
        onHistoryClick = onHistoryClick,
        onBookmarksClick = onBookmarksClick,
        onPracticeTopic = onPracticeTopic
    )
}

@Composable
private fun HomeScreenContent(
    dailyQuestion: Question?,
    streak: Int = 0,
    weakestTopic: TopicStat? = null,
    isOffline: Boolean = false,
    onExamSelected: (String) -> Unit,
    onNotesClick: () -> Unit,
    onAboutClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onBookmarksClick: () -> Unit = {},
    onPracticeTopic: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { if (isOffline) OfflineBanner() },
        bottomBar = {
            Column {
                BannerAdView()
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onNotesClick()
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Study Notes") },
                        label = { Text("Notes") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            onHistoryClick()
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = {
                            selectedTab = 3
                            onBookmarksClick()
                        },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks") },
                        label = { Text("Saved") }
                    )
                }
            }
        }
    ) { padding ->
        GradientBackground(
            colors = GradientTokens.hero(),
            animated = true,
            modifier = Modifier.padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "ExamAI",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1f)
                        )
                        IconButton(onClick = onAboutClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        text = "Smart Study Assistant for JEE, NEET & UPSC",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    NoSubscriptionBadge(modifier = Modifier.padding(top = 12.dp))
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var cardIndex = 0
                    if (dailyQuestion != null) {
                        item {
                            StaggeredEntrance(index = cardIndex) { DailyQuestionCard(question = dailyQuestion!!) }
                        }
                        cardIndex++
                    }
                    if (streak > 0) {
                        item {
                            StaggeredEntrance(index = cardIndex) { StreakCard(streak = streak) }
                        }
                        cardIndex++
                    }
                    if (weakestTopic != null) {
                        item {
                            StaggeredEntrance(index = cardIndex) {
                                WeakTopicCard(
                                    stat = weakestTopic,
                                    onPractice = { onPracticeTopic(weakestTopic.exam, weakestTopic.subject, weakestTopic.topic) }
                                )
                            }
                        }
                        cardIndex++
                    }
                    val examStartIndex = cardIndex
                    itemsIndexed(ExamData.exams) { i, exam ->
                        StaggeredEntrance(index = examStartIndex + i) {
                            ExamCard(exam = exam, onClick = { onExamSelected(exam.name) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoSubscriptionBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.18f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "100% Free • No subscription needed",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun DailyQuestionCard(question: Question) {
    var revealed by remember(question.question) { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        onClick = { revealed = !revealed },
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Question of the Day",
                style = MaterialTheme.typography.labelLarge,
                color = BrandNavy,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (revealed) {
                Text(
                    text = "Answer: ${question.options.getOrNull(question.correct).orEmpty()}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = CorrectGreen,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                Text(
                    text = "Tap to reveal answer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun StreakCard(streak: Int) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = "$streak day${if (streak == 1) "" else "s"} streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Keep it going — take a quiz today!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = "Practice weak spot: ${stat.topic}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(stat.accuracy * 100).toInt()}% accuracy • ${stat.subject}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
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
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = exam.icon,
                    contentDescription = exam.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = exam.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = exam.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
private fun NoSubscriptionBadgePreview() {
    AIStudyAssistantTheme {
        NoSubscriptionBadge()
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
