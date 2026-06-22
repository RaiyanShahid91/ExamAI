package com.ai.studyassistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.ui.components.GradientBackground
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.Cyan
import com.ai.studyassistant.ui.theme.DeepPurple
import com.ai.studyassistant.viewmodel.QuizViewModel

@Composable
fun ResultScreen(
    viewModel: QuizViewModel,
    onTryAgain: () -> Unit,
    onNewTopic: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val total = uiState.questions.size.coerceAtLeast(1)
    val score = uiState.score
    val context = LocalContext.current

    ResultContent(
        score = score,
        total = total,
        onShare = {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "I scored $score/$total on ExamAI - Smart Study Assistant! Free exam prep for JEE, NEET & UPSC."
                )
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share your score"))
        },
        onTryAgain = onTryAgain,
        onNewTopic = onNewTopic
    )
}

@Composable
private fun ResultContent(
    score: Int,
    total: Int,
    onShare: () -> Unit,
    onTryAgain: () -> Unit,
    onNewTopic: () -> Unit
) {
    val (message, emoji) = performanceMessage(score, total)

    GradientBackground(colors = listOf(DeepPurple, Cyan)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(96.dp)) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = DeepPurple,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Text(
                text = "$score / $total",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            TextButton(
                onClick = onShare,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Share Score",
                    color = Color.White,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNewTopic,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("New Topic")
                }
                Button(
                    onClick = onTryAgain,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Try Again")
                }
            }
        }
    }
}

private fun performanceMessage(score: Int, total: Int): Pair<String, String> {
    val ratio = score.toFloat() / total
    return when {
        ratio >= 1f -> "Outstanding! You're exam ready!" to "🏆"
        ratio >= 0.8f -> "Excellent work! Keep it up!" to "🌟"
        ratio >= 0.6f -> "Good effort, keep practicing!" to "👍"
        ratio >= 0.4f -> "You need more practice." to "💪"
        else -> "Don't worry, keep learning and try again!" to "📚"
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultContentHighScorePreview() {
    AIStudyAssistantTheme {
        ResultContent(score = 9, total = 10, onShare = {}, onTryAgain = {}, onNewTopic = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultContentLowScorePreview() {
    AIStudyAssistantTheme {
        ResultContent(score = 3, total = 10, onShare = {}, onTryAgain = {}, onNewTopic = {})
    }
}
