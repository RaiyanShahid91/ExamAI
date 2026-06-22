package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.CorrectGreen
import com.ai.studyassistant.ui.theme.WrongRed
import com.ai.studyassistant.viewmodel.QuizUiState
import com.ai.studyassistant.viewmodel.QuizViewModel

@Composable
fun QuizScreen(
    exam: String,
    subject: String,
    topic: String,
    viewModel: QuizViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(exam, subject, topic) {
        viewModel.loadQuestions(exam, subject, topic)
    }

    Scaffold(
        topBar = { BackTopBar(title = topic, onBack = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> LoadingScreen()
                uiState.error != null -> QuizError(
                    message = uiState.error.orEmpty(),
                    onRetry = { viewModel.loadQuestions(exam, subject, topic) }
                )
                uiState.currentQuestion != null -> QuizContent(
                    uiState = uiState,
                    onSelectAnswer = viewModel::selectAnswer,
                    onNext = {
                        if (uiState.isLastQuestion) {
                            onFinished()
                        } else {
                            viewModel.nextQuestion()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuizError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
            Text("Retry")
        }
    }
}

@Composable
private fun QuizContent(
    uiState: QuizUiState,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit
) {
    val question = uiState.currentQuestion ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Question ${uiState.currentIndex + 1} of ${uiState.questions.size}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        LinearProgressIndicator(
            progress = { (uiState.currentIndex + 1) / uiState.questions.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 20.dp)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            question.options.forEachIndexed { index, option ->
                OptionCard(
                    text = option,
                    index = index,
                    question = question,
                    uiState = uiState,
                    onClick = { onSelectAnswer(index) }
                )
            }
        }

        if (uiState.isAnswered) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text(
                    text = question.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (!uiState.topicSummary.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "From Wikipedia",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = uiState.topicSummary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text(if (uiState.isLastQuestion) "See Result" else "Next Question")
            }
        }
    }
}

@Composable
private fun OptionCard(
    text: String,
    index: Int,
    question: Question,
    uiState: QuizUiState,
    onClick: () -> Unit
) {
    val isSelected = uiState.selectedOptionIndex == index
    val isCorrectOption = index == question.correct

    val containerColor = when {
        !uiState.isAnswered -> MaterialTheme.colorScheme.surface
        isCorrectOption -> CorrectGreen.copy(alpha = 0.15f)
        isSelected -> WrongRed.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        !uiState.isAnswered -> MaterialTheme.colorScheme.outlineVariant
        isCorrectOption -> CorrectGreen
        isSelected -> WrongRed
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        onClick = onClick,
        enabled = !uiState.isAnswered,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                if (uiState.isAnswered && (isCorrectOption || isSelected)) {
                    Icon(
                        imageVector = if (isCorrectOption) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (isCorrectOption) CorrectGreen else WrongRed
                    )
                }
            }
        }
    }
}

private val sampleQuestion = Question(
    question = "What is the SI unit of force?",
    options = listOf("Newton", "Joule", "Watt", "Pascal"),
    correct = 0,
    explanation = "Newton is the SI unit of force. F = ma."
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun QuizContentUnansweredPreview() {
    AIStudyAssistantTheme {
        QuizContent(
            uiState = QuizUiState(questions = List(10) { sampleQuestion }, currentIndex = 2),
            onSelectAnswer = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun QuizContentAnsweredPreview() {
    AIStudyAssistantTheme {
        QuizContent(
            uiState = QuizUiState(
                questions = List(10) { sampleQuestion },
                currentIndex = 2,
                selectedOptionIndex = 1,
                isAnswered = true,
                topicSummary = "Force is any interaction that, when unopposed, will change the motion of an object."
            ),
            onSelectAnswer = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun QuizErrorPreview() {
    AIStudyAssistantTheme {
        QuizError(message = "Couldn't load questions. Check your connection and try again.", onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionCardPreview() {
    AIStudyAssistantTheme {
        OptionCard(
            text = "Newton",
            index = 0,
            question = sampleQuestion,
            uiState = QuizUiState(questions = listOf(sampleQuestion), selectedOptionIndex = 0, isAnswered = true),
            onClick = {}
        )
    }
}
