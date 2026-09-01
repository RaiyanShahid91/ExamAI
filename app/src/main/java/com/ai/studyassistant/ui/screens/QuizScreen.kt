package com.ai.studyassistant.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.ads.InterstitialAdManager
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.data.model.Question
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.BannerAdView
import com.ai.studyassistant.ui.components.OfflineBanner
import com.ai.studyassistant.ui.components.bounceClick
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.CorrectGreen
import com.ai.studyassistant.ui.theme.WrongRed
import com.ai.studyassistant.ui.util.findActivity
import com.ai.studyassistant.viewmodel.QuizUiState
import com.ai.studyassistant.viewmodel.QuizViewModel

@Composable
fun QuizScreen(
    exam: String = "",
    subject: String = "",
    topic: String = "",
    attemptId: Long? = null,
    difficulty: String? = null,
    timed: Boolean = false,
    mockExam: String? = null,
    viewModel: QuizViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val bookmarkedTexts by viewModel.bookmarkedQuestionTexts.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val context = LocalContext.current

    // The score reveal is gated behind an interstitial so it plays before every result,
    // not after landing on it — falls straight through to onFinished if no ad is ready
    // (no activity, load still in flight, fill failure) so a slow/missing ad never blocks.
    val finishAndShowResult: () -> Unit = {
        viewModel.finishAttempt()
        val activity = context.findActivity()
        if (activity != null) {
            InterstitialAdManager.showIfAvailable(activity, onDismissed = onFinished)
        } else {
            onFinished()
        }
    }

    LaunchedEffect(exam, subject, topic, attemptId, mockExam) {
        when {
            attemptId != null -> viewModel.resumeAttempt(attemptId)
            mockExam != null -> viewModel.loadMockExam(mockExam, timed)
            else -> viewModel.loadQuestions(exam, subject, topic, difficulty, timed)
        }
    }

    LaunchedEffect(uiState.timeRemainingSeconds) {
        if (uiState.isTimed && uiState.timeRemainingSeconds == 0) {
            finishAndShowResult()
        }
    }

    Scaffold(
        topBar = {
            Column {
                BackTopBar(title = uiState.topic.ifBlank { topic }, onBack = onBack)
                if (isOffline) OfflineBanner()
                BannerAdView()
            }
        }
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
                    onRetry = {
                        AnalyticsManager.logEvent("quiz_retry_clicked", mapOf("exam" to exam, "subject" to subject, "topic" to topic))
                        when {
                            attemptId != null -> viewModel.resumeAttempt(attemptId)
                            mockExam != null -> viewModel.loadMockExam(mockExam, timed)
                            else -> viewModel.loadQuestions(exam, subject, topic, difficulty, timed)
                        }
                    }
                )
                uiState.currentQuestion != null -> QuizContent(
                    uiState = uiState,
                    isBookmarked = bookmarkedTexts.contains(uiState.currentQuestion?.question),
                    onSelectAnswer = viewModel::selectAnswer,
                    onToggleBookmark = viewModel::toggleBookmark,
                    onNext = {
                        if (uiState.isLastQuestion) {
                            finishAndShowResult()
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
    isBookmarked: Boolean = false,
    onSelectAnswer: (Int) -> Unit,
    onToggleBookmark: () -> Unit = {},
    onNext: () -> Unit
) {
    val question = uiState.currentQuestion ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Question ${uiState.currentIndex + 1} of ${uiState.questions.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            if (uiState.isTimed && uiState.timeRemainingSeconds != null) {
                TimerChip(secondsRemaining = uiState.timeRemainingSeconds)
            }
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark question",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        LinearProgressIndicator(
            progress = { (uiState.currentIndex + 1) / uiState.questions.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        AnimatedContent(
            targetState = uiState.currentIndex,
            transitionSpec = {
                (slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280))) togetherWith
                    (slideOutHorizontally(tween(220)) { -it / 3 } + fadeOut(tween(220)))
            },
            label = "questionTransition"
        ) { _ ->
            Column {
                Surface(
                    shape = MaterialTheme.shapes.medium,
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
                        shape = MaterialTheme.shapes.medium,
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
                            shape = MaterialTheme.shapes.medium,
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
                            .bounceClick()
                    ) {
                        Text(if (uiState.isLastQuestion) "See Result" else "Next Question")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerChip(secondsRemaining: Int) {
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val isLow = secondsRemaining <= 30

    val pulseScale = if (isLow) {
        val transition = rememberInfiniteTransition(label = "timerPulse")
        val pulse by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
            label = "timerPulseScale"
        )
        pulse
    } else {
        1f
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = if (isLow) WrongRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .padding(end = 4.dp)
            .scale(pulseScale)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Time remaining",
                tint = if (isLow) WrongRed else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = "%d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.labelLarge,
                color = if (isLow) WrongRed else MaterialTheme.colorScheme.primary
            )
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

    val targetContainerColor = when {
        !uiState.isAnswered -> MaterialTheme.colorScheme.surface
        isCorrectOption -> CorrectGreen.copy(alpha = 0.15f)
        isSelected -> WrongRed.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }
    val targetBorderColor = when {
        !uiState.isAnswered -> MaterialTheme.colorScheme.outlineVariant
        isCorrectOption -> CorrectGreen
        isSelected -> WrongRed
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val containerColor by animateColorAsState(targetContainerColor, tween(300), label = "optionContainerColor")
    val borderColor by animateColorAsState(targetBorderColor, tween(300), label = "optionBorderColor")

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        onClick = onClick,
        enabled = !uiState.isAnswered,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
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
