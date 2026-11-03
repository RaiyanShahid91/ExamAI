package com.ai.studyassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.data.local.AppearancePreferences
import com.ai.studyassistant.data.local.AppearanceSettings
import com.ai.studyassistant.data.local.FirstRunPreferences
import com.ai.studyassistant.ui.components.AppearancePicker
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R
import androidx.annotation.StringRes
import androidx.compose.material.icons.filled.Language

private data class WelcomePage(@StringRes val eyebrow: Int, @StringRes val title: Int, @StringRes val body: Int, val icon: ImageVector, val cards: List<Int>)
private val welcomePages = listOf(
    WelcomePage(R.string.onboarding_eyebrow_1, R.string.onboarding_title_1, R.string.onboarding_body_1, Icons.Default.School,
        listOf(R.string.onboarding_card_1a, R.string.onboarding_card_1b, R.string.onboarding_card_1c)),
    WelcomePage(R.string.onboarding_eyebrow_2, R.string.onboarding_title_2, R.string.onboarding_body_2, Icons.Default.AutoAwesome,
        listOf(R.string.onboarding_card_2a, R.string.onboarding_card_2b, R.string.onboarding_card_2c)),
    WelcomePage(R.string.onboarding_eyebrow_revision, R.string.onboarding_title_revision, R.string.onboarding_body_revision, Icons.AutoMirrored.Filled.MenuBook,
        listOf(R.string.onboarding_card_revision_a, R.string.onboarding_card_revision_b, R.string.onboarding_card_revision_c)),
    WelcomePage(R.string.onboarding_eyebrow_progress, R.string.onboarding_title_progress, R.string.onboarding_body_progress, Icons.Default.Insights,
        listOf(R.string.onboarding_card_progress_a, R.string.onboarding_card_progress_b, R.string.onboarding_card_progress_c))
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit, onPrivacyClick: () -> Unit = {}) {
    val lastPage = welcomePages.size
    val pageCount = lastPage + 1
    val pager = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val firstRun = remember { FirstRunPreferences(context) }
    val preferences = remember { AppearancePreferences(context) }
    val appearance by preferences.settings.collectAsState(initial = AppearanceSettings())
    var finishing by remember { mutableStateOf(false) }
    var showLanguage by remember { mutableStateOf(false) }
    fun finish() {
        if (finishing) return
        finishing = true
        scope.launch {
            try {
                firstRun.setSeenOnboarding()
                AnalyticsManager.logEvent("onboarding_completed", mapOf("last_page" to pager.currentPage))
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main.immediate) { onFinish() }
            } finally { finishing = false }
        }
    }
    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.home_examai), Modifier.weight(1f).padding(start = 10.dp), style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                androidx.compose.material3.IconButton(onClick = { showLanguage = true }) { Icon(Icons.Default.Language, contentDescription = stringResource(R.string.language_title)) }
                if (showLanguage) com.ai.studyassistant.ui.components.LanguageDialog(onDismiss = { showLanguage = false })
                if (pager.currentPage < lastPage) TextButton(onClick = { scope.launch { pager.animateScrollToPage(lastPage) } }, modifier = Modifier.widthIn(max = 148.dp)) { Text(stringResource(R.string.onboarding_choose_look), maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            }
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(Modifier.navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        repeat(pageCount) { index ->
                            Box(Modifier.height(5.dp).width(if (index == pager.currentPage) 32.dp else 12.dp).background(
                                if (index == pager.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, CircleShape))
                        }
                        Spacer(Modifier.weight(1f))
                        Text(stringResource(R.string.onboarding_page_count, pager.currentPage + 1, pageCount), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (pager.currentPage > 0) OutlinedButton(onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }, modifier = Modifier.heightIn(min = 52.dp)) { Text(stringResource(R.string.onboarding_back)) }
                        Button(
                            onClick = { if (pager.currentPage == lastPage) finish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                            enabled = !finishing, modifier = Modifier.weight(1f).heightIn(min = 54.dp)
                        ) {
                            Text(if (pager.currentPage == lastPage) stringResource(R.string.onboarding_lets_start) else stringResource(R.string.onboarding_continue))
                            Spacer(Modifier.width(10.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize().padding(padding).testTag("onboarding_pager")) { index ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                if (index < welcomePages.size) {
                    val page = welcomePages[index]
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                        Icon(page.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(20.dp).size(40.dp))
                    }
                    Text(stringResource(page.eyebrow), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(page.title), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.testTag("onboarding_page_$index"))
                    Text(stringResource(page.body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    page.cards.forEach { label ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(stringResource(label), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Text(stringResource(R.string.onboarding_your_space_to_focus), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.onboarding_language_options), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.onboarding_content_language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = { showLanguage = true }) {
                        Icon(Icons.Default.Language, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.language_title))
                    }
                    AppearancePicker(appearance) { choice -> scope.launch { preferences.save(choice) } }
                    Text(stringResource(R.string.onboarding_you_can_change_your_look), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider()
                    Text(stringResource(R.string.onboarding_a_little_transparency), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.onboarding_examai_is_independent_of_exam), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = onPrivacyClick, modifier = Modifier.testTag("onboarding_privacy")) {
                        Text(stringResource(R.string.settings_privacy_policy))
                    }
                }
            }
        }
    }
}
