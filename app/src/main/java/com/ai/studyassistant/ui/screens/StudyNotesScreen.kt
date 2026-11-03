package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.model.StudyNote
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.BannerAdView
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.util.findActivity
import com.ai.studyassistant.viewmodel.NotesUiState
import com.ai.studyassistant.viewmodel.NotesViewModel
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R
import androidx.compose.ui.res.pluralStringResource

private val quickTopics = listOf(
    "Photosynthesis", "Newton's Laws of Motion", "Indian Constitution",
    "French Revolution", "Periodic Table", "Cell Biology"
)

@Composable
fun StudyNotesScreen(
    viewModel: NotesViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    StudyNotesContent(
        uiState = uiState,
        onSearch = { topic -> viewModel.search(topic) },
        onRewardSearches = viewModel::grantSearches,
        onOpenSaved = viewModel::openSaved,
        onToggleSave = { viewModel.toggleSave() },
        onBack = onBack
    )
}

@Composable
private fun StudyNotesContent(
    uiState: NotesUiState,
    onSearch: (String) -> Unit,
    onToggleSave: () -> Unit,
    onBack: () -> Unit,
    onRewardSearches: () -> Unit = {},
    onOpenSaved: (StudyNote) -> Unit = {}
) {
    var query by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column {
                BackTopBar(title = stringResource(R.string.study_notes_study_notes), onBack = onBack)
                BannerAdView()
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            com.ai.studyassistant.ui.components.ScreenIntro(stringResource(R.string.notes_follow_curiosity), stringResource(R.string.notes_clear_summaries))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.study_notes_search_any_topic)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { if (query.isNotBlank()) onSearch(query.trim()) },
                    enabled = !uiState.isLoading
                ) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.study_notes_search))
                }
            }

            if (com.ai.studyassistant.ads.ADS_ENABLED) {
                SearchAllowanceHint(remaining = uiState.freeSearchesRemaining)
                if (uiState.freeSearchesRemaining <= 1) {
                    com.ai.studyassistant.ui.components.RewardedOfferCard(
                        stringResource(R.string.notes_add_two_searches), stringResource(R.string.notes_add_two_searches_body),
                        "notes_searches", onRewardSearches
                    )
                }
            }

            Text(
                text = stringResource(R.string.study_notes_quick_topics),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickTopics) { topic ->
                    AssistChip(
                        enabled = !uiState.isLoading,
                        onClick = {
                            query = topic
                            onSearch(topic)
                        },
                        label = { Text(topic) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
                uiState.note != null -> {
                    val note = uiState.note!!
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = onToggleSave) {
                                    Icon(
                                        imageVector = if (note.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = stringResource(R.string.study_notes_save_note)
                                    )
                                }
                            }
                            Text(
                                text = note.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }

            com.ai.studyassistant.ui.components.BrynflowLearningCard("notes")

            if (uiState.savedNotes.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.study_notes_saved_for_offline),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 28.dp, bottom = 8.dp)
                )
                uiState.savedNotes.forEach { saved ->
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        onClick = { onOpenSaved(saved) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = saved.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchAllowanceHint(remaining: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Icon(
            imageVector = if (remaining > 0) Icons.Default.Search else Icons.Default.OndemandVideo,
            contentDescription = null,
            tint = if (remaining > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (remaining > 0) {
                pluralStringResource(R.plurals.notes_searches_left, remaining, remaining)
            } else {
                stringResource(R.string.notes_no_searches_left)
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (remaining > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

private val sampleNote = StudyNote(
    topic = "Photosynthesis",
    title = "Photosynthesis",
    summary = "Photosynthesis is the process by which green plants and some other organisms use sunlight to synthesize nutrients from carbon dioxide and water, generating oxygen as a byproduct.",
    isSaved = true
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StudyNotesContentPreview() {
    AIStudyAssistantTheme {
        StudyNotesContent(
            uiState = NotesUiState(
                note = sampleNote,
                savedNotes = listOf(sampleNote, sampleNote.copy(topic = "Newton's Laws of Motion", title = stringResource(R.string.study_notes_newton_s_laws_of_motion)))
            ),
            onSearch = {},
            onToggleSave = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StudyNotesContentLoadingPreview() {
    AIStudyAssistantTheme {
        StudyNotesContent(
            uiState = NotesUiState(isLoading = true),
            onSearch = {},
            onToggleSave = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun StudyNotesContentErrorPreview() {
    AIStudyAssistantTheme {
        StudyNotesContent(
            uiState = NotesUiState(error = "Couldn't fetch notes for this topic."),
            onSearch = {},
            onToggleSave = {},
            onBack = {}
        )
    }
}
