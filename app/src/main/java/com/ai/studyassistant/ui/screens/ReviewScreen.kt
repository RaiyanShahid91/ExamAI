package com.ai.studyassistant.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.TaskAlt
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
import com.ai.studyassistant.data.local.ReviewItemEntity
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.BannerAdView
import com.ai.studyassistant.ui.components.bounceClick
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.CorrectGreen
import com.ai.studyassistant.ui.theme.WrongRed
import com.ai.studyassistant.viewmodel.ReviewUiState
import com.ai.studyassistant.viewmodel.ReviewViewModel

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startSession()
    }

    Scaffold(
        topBar = {
            Column {
                BackTopBar(title = "Review", onBack = onBack)
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
                uiState.isLoading -> LoadingScreen(message = "Loading your review queue...")
                uiState.items.isEmpty() -> ReviewEmptyState()
                uiState.isSessionComplete -> ReviewCompleteState(uiState = uiState, onDone = onBack)
                else -> ReviewContent(
                    uiState = uiState,
                    onSelectAnswer = viewModel::selectAnswer,
                    onNext = viewModel::nextItem
                )
            }
        }
    }
}

@Composable
private fun ReviewEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EventAvailable,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Nothing due for review",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Questions you get wrong in a quiz will show up here for spaced review.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ReviewCompleteState(uiState: ReviewUiState, onDone: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.TaskAlt,
            contentDescription = null,
            tint = CorrectGreen
        )
        Text(
            text = "Review complete!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "${uiState.correctCount} of ${uiState.items.size} correct",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Button(onClick = onDone, modifier = Modifier.padding(top = 24.dp).bounceClick()) {
            Text("Done")
        }
    }
}

@Composable
private fun ReviewContent(
    uiState: ReviewUiState,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit
) {
    val item = uiState.currentItem ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Question ${uiState.currentIndex + 1} of ${uiState.items.size}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        LinearProgressIndicator(
            progress = { (uiState.currentIndex + 1) / uiState.items.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 20.dp)
        )

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.question,
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
            item.options.forEachIndexed { index, option ->
                ReviewOptionCard(
                    text = option,
                    index = index,
                    item = item,
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
                    text = item.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .bounceClick()
            ) {
                Text(if (uiState.isLastItem) "Finish" else "Next Question")
            }
        }
    }
}

@Composable
private fun ReviewOptionCard(
    text: String,
    index: Int,
    item: ReviewItemEntity,
    uiState: ReviewUiState,
    onClick: () -> Unit
) {
    val isSelected = uiState.selectedOptionIndex == index
    val isCorrectOption = index == item.correct

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
    val containerColor by animateColorAsState(targetContainerColor, tween(300), label = "reviewOptionContainerColor")
    val borderColor by animateColorAsState(targetBorderColor, tween(300), label = "reviewOptionBorderColor")

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.5.dp, borderColor),
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

private val sampleReviewItem = ReviewItemEntity(
    id = 1,
    exam = "JEE",
    subject = "Physics",
    topic = "Laws of Motion",
    question = "What is the SI unit of force?",
    options = listOf("Newton", "Joule", "Watt", "Pascal"),
    correct = 0,
    explanation = "Newton is the SI unit of force. F = ma.",
    box = 1,
    dueAt = System.currentTimeMillis(),
    createdAt = System.currentTimeMillis()
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ReviewContentPreview() {
    AIStudyAssistantTheme {
        ReviewContent(
            uiState = ReviewUiState(isLoading = false, items = List(5) { sampleReviewItem }, currentIndex = 1),
            onSelectAnswer = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ReviewEmptyStatePreview() {
    AIStudyAssistantTheme {
        ReviewEmptyState()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ReviewCompleteStatePreview() {
    AIStudyAssistantTheme {
        ReviewCompleteState(
            uiState = ReviewUiState(isLoading = false, items = List(5) { sampleReviewItem }, currentIndex = 5, correctCount = 4),
            onDone = {}
        )
    }
}
