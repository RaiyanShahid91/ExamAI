package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.data.local.FirstRunPreferences
import com.ai.studyassistant.ui.components.GradientBackground
import com.ai.studyassistant.ui.components.bounceClick
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.GradientTokens
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Default.School,
        title = "Welcome to ExamAI",
        body = "Practice quizzes for JEE, NEET, UPSC, SSC, Banking, CAT, and CDS/NDA — " +
                "with topic-wise questions, study notes, bookmarks, full-length mock tests, " +
                "and a history of every exam you take. 100% free, no login required."
    ),
    OnboardingPage(
        icon = Icons.Default.AutoAwesome,
        title = "Where questions come from",
        body = "Every question is AI/algorithm-curated — blended from free public sources " +
                "(Open Trivia Database, Wikipedia, NCERT) and a hand-picked local question bank.\n\n" +
                "ExamAI is an independent app. It is NOT affiliated with, endorsed by, or " +
                "written by any coaching center, teacher, or official exam authority " +
                "(UPSC, NTA, or otherwise)."
    ),
    OnboardingPage(
        icon = Icons.Default.Shield,
        title = "Your privacy, your device",
        body = "No accounts, no login, no ads-first tracking of you personally, no analytics " +
                "spying on your taps. Your quiz history, bookmarks, and notes are stored only " +
                "on your device.\n\n" +
                "Internet is only used to fetch fresh exam questions — the app works fully " +
                "offline otherwise. Nothing you do here is stored on any server of ours, " +
                "because we don't run one."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val firstRunPreferences = remember { FirstRunPreferences(context) }

    fun finish(via: String) {
        AnalyticsManager.logEvent(
            "onboarding_completed",
            mapOf("via" to via, "last_page" to pagerState.currentPage)
        )
        scope.launch { firstRunPreferences.setSeenOnboarding() }
        onFinish()
    }

    GradientBackground(colors = GradientTokens.hero(), animated = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (pagerState.currentPage < onboardingPages.lastIndex) {
                    TextButton(onClick = { finish(via = "skip") }) {
                        Text("Skip", color = Color.White)
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPageContent(onboardingPages[page])
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                onboardingPages.indices.forEach { index ->
                    val selected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .background(
                                color = Color.White.copy(alpha = if (selected) 1f else 0.4f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < onboardingPages.lastIndex) {
                        AnalyticsManager.logEvent(
                            "onboarding_next_clicked",
                            mapOf("page" to pagerState.currentPage)
                        )
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        finish(via = "get_started")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .bounceClick()
            ) {
                Text(if (pagerState.currentPage < onboardingPages.lastIndex) "Next" else "Get Started")
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Color.White.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.92f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    AIStudyAssistantTheme {
        OnboardingScreen(onFinish = {})
    }
}
