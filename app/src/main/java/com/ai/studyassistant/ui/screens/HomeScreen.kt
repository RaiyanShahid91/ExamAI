package com.ai.studyassistant.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.model.Exam
import com.ai.studyassistant.data.model.ExamData
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.ui.components.GradientBackground
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.CorrectGreen
import com.ai.studyassistant.ui.theme.Cyan
import com.ai.studyassistant.ui.theme.GradientBlue
import com.ai.studyassistant.ui.theme.GradientPurple
import com.ai.studyassistant.viewmodel.QuizViewModel

@Composable
fun HomeScreen(
    quizViewModel: QuizViewModel,
    onExamSelected: (String) -> Unit,
    onNotesClick: () -> Unit
) {
    val dailyQuestion by quizViewModel.dailyQuestion.collectAsState()
    HomeScreenContent(
        dailyQuestion = dailyQuestion,
        onExamSelected = onExamSelected,
        onNotesClick = onNotesClick
    )
}

@Composable
private fun HomeScreenContent(
    dailyQuestion: Question?,
    onExamSelected: (String) -> Unit,
    onNotesClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
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
            }
        }
    ) { padding ->
        GradientBackground(
            colors = listOf(GradientBlue, GradientPurple),
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
                            modifier = Modifier.padding(start = 8.dp)
                        )
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
                    if (dailyQuestion != null) {
                        item { DailyQuestionCard(question = dailyQuestion!!) }
                    }
                    items(ExamData.exams) { exam ->
                        ExamCard(exam = exam, onClick = { onExamSelected(exam.name) })
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
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        onClick = { revealed = !revealed },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Question of the Day",
                style = MaterialTheme.typography.labelLarge,
                color = GradientPurple,
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
private fun ExamCard(exam: Exam, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Cyan.copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = exam.icon,
                    contentDescription = exam.name,
                    tint = GradientPurple,
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
        HomeScreenContent(dailyQuestion = sampleDailyQuestion, onExamSelected = {}, onNotesClick = {})
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
