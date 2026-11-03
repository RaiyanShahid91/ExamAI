package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.model.Flashcard
import com.ai.studyassistant.data.model.FlashcardSession
import com.ai.studyassistant.data.model.FlashcardSource
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.BannerAdView
import com.ai.studyassistant.ui.components.bounceClick
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.viewmodel.FlashcardsViewModel
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R
import androidx.compose.ui.res.pluralStringResource

@Composable
fun FlashcardsScreen(viewModel: FlashcardsViewModel, onBack: () -> Unit) {
    val session by viewModel.session.collectAsState()
    FlashcardsContent(
        session = session,
        onBack = onBack,
        onFlip = viewModel::flip,
        onGotIt = viewModel::gotIt,
        onAgain = viewModel::again,
        onRestart = viewModel::startSession
    )
}

@Composable
private fun FlashcardsContent(
    session: FlashcardSession?,
    onBack: () -> Unit,
    onFlip: () -> Unit,
    onGotIt: () -> Unit,
    onAgain: () -> Unit,
    onRestart: () -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                BackTopBar(title = stringResource(R.string.flashcards_flashcards), onBack = onBack)
                BannerAdView()
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                session == null -> Unit
                session.isEmpty -> EmptyDeck()
                session.isComplete -> CompleteDeck(session, onRestart, onBack)
                else -> StudyCard(session, onFlip, onGotIt, onAgain)
            }
        }
    }
}

@Composable
private fun StudyCard(session: FlashcardSession, onFlip: () -> Unit, onGotIt: () -> Unit, onAgain: () -> Unit) {
    val card = session.current ?: return

    Text(stringResource(R.string.flashcards_of_learned_to_go, session.known, session.total, session.queue.size),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    LinearProgressIndicator(
        progress = { if (session.total == 0) 0f else session.known.toFloat() / session.total },
        modifier = Modifier.fillMaxWidth()
    )

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = if (session.flipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .clickable(role = Role.Button, onClickLabel = if (session.flipped) stringResource(R.string.flashcards_show_question) else stringResource(R.string.flashcards_show_answer), onClick = onFlip)
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(sourceLabel(card.source)) + " · " + card.topic,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (!session.flipped) {
                Text(stringResource(R.string.flashcards_question), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(card.question, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.flashcards_tap_to_see_the_answer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(stringResource(R.string.flashcards_answer), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(card.answer, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (card.explanation.isNotBlank()) Text(card.explanation, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    if (session.flipped) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onAgain, modifier = Modifier.weight(1f).bounceClick()) { Text(stringResource(R.string.flashcards_again)) }
            Button(onClick = onGotIt, modifier = Modifier.weight(1f).bounceClick()) { Text(stringResource(R.string.flashcards_got_it)) }
        }
    }
}

@Composable
private fun EmptyDeck() {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.flashcards_no_flashcards_yet), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.flashcards_questions_you_get_wrong_and),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CompleteDeck(session: FlashcardSession, onRestart: () -> Unit, onBack: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.flashcards_deck_complete), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.flashcards_you_went_through_all_cards, session.total), style = MaterialTheme.typography.bodyLarge)
            if (session.retries > 0) {
                Text(
                    pluralStringResource(R.plurals.flashcards_need_another_look, session.retries, session.retries),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).bounceClick()) { Text(stringResource(R.string.flashcards_done)) }
        Button(onClick = onRestart, modifier = Modifier.weight(1f).bounceClick()) { Text(stringResource(R.string.flashcards_study_again)) }
    }
}

@androidx.annotation.StringRes
private fun sourceLabel(source: FlashcardSource) = when (source) {
    FlashcardSource.MISTAKE -> R.string.flashcards_source_missed
    FlashcardSource.SAVED -> R.string.flashcards_source_saved
    FlashcardSource.BOTH -> R.string.flashcards_source_both
}

private val previewCard = Flashcard(
    "The electric field inside a charged conductor in electrostatic equilibrium is:",
    listOf("Maximum", "Zero", "Equal to surface field", "Infinite"), 1,
    "Charges reside on the surface, making the internal field zero.",
    "JEE", "Physics", "Electrostatics", FlashcardSource.MISTAKE
)

@Preview(showBackground = true)
@Composable
private fun FlashcardsFrontPreview() {
    AIStudyAssistantTheme {
        FlashcardsContent(FlashcardSession.start(listOf(previewCard, previewCard.copy(question = "Other"))) { it }, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun FlashcardsBackPreview() {
    AIStudyAssistantTheme {
        FlashcardsContent(FlashcardSession.start(listOf(previewCard)) { it }.flip(), {}, {}, {}, {}, {})
    }
}
