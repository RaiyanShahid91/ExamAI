package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.data.local.BookmarkedQuestionEntity
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.BannerAdView
import com.ai.studyassistant.ui.components.StaggeredEntrance
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.CorrectGreen
import com.ai.studyassistant.ui.theme.WrongRed
import com.ai.studyassistant.viewmodel.BookmarksViewModel
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R

@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel,
    onBack: () -> Unit
) {
    val bookmarks by viewModel.bookmarks.collectAsState()

    BookmarksContent(
        bookmarks = bookmarks,
        onBack = onBack,
        onRemove = viewModel::removeBookmark
    )
}

@Composable
private fun BookmarksContent(
    bookmarks: List<BookmarkedQuestionEntity>,
    onBack: () -> Unit,
    onRemove: (BookmarkedQuestionEntity) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                BackTopBar(title = stringResource(R.string.bookmarks_bookmarked_questions), onBack = onBack)
                BannerAdView()
            }
        }
    ) { padding ->
        if (bookmarks.isEmpty()) {
            EmptyBookmarks(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { com.ai.studyassistant.ui.components.ScreenIntro("Your personal question bank", "Keep the questions worth another look.") }
                itemsIndexed(bookmarks, key = { _, bookmark -> bookmark.id }) { index, bookmark ->
                    StaggeredEntrance(index = index) {
                        BookmarkCard(bookmark = bookmark, onRemove = { onRemove(bookmark) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBookmarks(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.bookmarks_no_bookmarked_questions_yet),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = stringResource(R.string.bookmarks_tap_the_bookmark_icon_during),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun BookmarkCard(bookmark: BookmarkedQuestionEntity, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${bookmark.exam} • ${bookmark.subject} • ${bookmark.topic}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.bookmarks_remove_bookmark),
                        tint = WrongRed
                    )
                }
            }
            Text(
                text = bookmark.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            bookmark.options.forEachIndexed { index, option ->
                val isCorrect = index == bookmark.correct
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (isCorrect) CorrectGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, if (isCorrect) CorrectGreen else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(text = option, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (isCorrect) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CorrectGreen)
                        }
                    }
                }
            }
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = bookmark.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

private val sampleBookmark = BookmarkedQuestionEntity(
    id = 1,
    exam = "JEE",
    subject = "Physics",
    topic = "Laws of Motion",
    question = "What is the SI unit of force?",
    options = listOf("Newton", "Joule", "Watt", "Pascal"),
    correct = 0,
    explanation = "Newton is the SI unit of force. F = ma.",
    bookmarkedAt = System.currentTimeMillis()
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BookmarksContentPreview() {
    AIStudyAssistantTheme {
        BookmarksContent(bookmarks = listOf(sampleBookmark), onBack = {}, onRemove = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BookmarksContentEmptyPreview() {
    AIStudyAssistantTheme {
        BookmarksContent(bookmarks = emptyList(), onBack = {}, onRemove = {})
    }
}
