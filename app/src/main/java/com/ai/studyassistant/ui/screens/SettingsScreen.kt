package com.ai.studyassistant.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.notification.DailyReminderWorker
import com.ai.studyassistant.notification.ReminderPreferences
import com.ai.studyassistant.ui.components.BackTopBar
import com.ai.studyassistant.ui.components.bounceClick
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.theme.BrandNavy
import com.ai.studyassistant.viewmodel.UpdateViewModel
import com.ai.studyassistant.ui.util.findActivity
import kotlinx.coroutines.launch
import com.ai.studyassistant.data.local.AppearancePreferences
import com.ai.studyassistant.data.local.AppearanceSettings
import com.ai.studyassistant.ui.components.AppearancePicker
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ai.studyassistant.ui.components.LanguageDialog
import com.ai.studyassistant.i18n.LanguageManager

private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.ai.studyassistant"

@Composable
fun SettingsScreen(
    updateViewModel: UpdateViewModel,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAiInfoClick: () -> Unit,
    onRemindersClick: () -> Unit = {},
    onWelcomeTour: () -> Unit = {}
) {
    val context = LocalContext.current
    val appearancePreferences = remember { AppearancePreferences(context) }
    val appearance by appearancePreferences.settings.collectAsState(initial = AppearanceSettings())
    val reminderPreferences = remember { ReminderPreferences(context) }
    val reminderEnabled by reminderPreferences.isEnabled.collectAsState(initial = false)
    val updateUiState by updateViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    fun enableReminders() {
        scope.launch {
            reminderPreferences.setEnabled(true)
            DailyReminderWorker.schedule(context)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) enableReminders()
    }

    LaunchedEffect(updateUiState.statusMessage) {
        val message = updateUiState.statusMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        updateViewModel.clearStatusMessage()
    }

    SettingsContent(
        appearance = appearance,
        onAppearanceChange = { scope.launch { appearancePreferences.save(it) } },
        reminderEnabled = reminderEnabled,
        onReminderToggle = { enabled ->
            AnalyticsManager.logEvent("reminder_toggled", mapOf("enabled" to enabled))
            if (!enabled) {
                scope.launch {
                    reminderPreferences.setEnabled(false)
                    DailyReminderWorker.cancel(context)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                enableReminders()
            }
        },
        onCheckForUpdate = {
            AnalyticsManager.logEvent("settings_row_clicked", mapOf("row" to "check_for_update"))
            updateViewModel.checkForUpdate(manual = true)
        },
        onShareApp = {
            AnalyticsManager.logEvent("settings_row_clicked", mapOf("row" to "share_app"))
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    context.getString(R.string.settings_share_text, PLAY_STORE_URL)
                )
            }
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.settings_share_chooser)))
        },
        onBack = onBack,
        onAboutClick = onAboutClick,
        onTermsClick = onTermsClick,
        onPrivacyClick = onPrivacyClick,
        onAiInfoClick = onAiInfoClick,
        onRemindersClick = onRemindersClick,
        onWelcomeTour = onWelcomeTour
    )
}

@Composable
private fun SettingsContent(
    appearance: AppearanceSettings = AppearanceSettings(),
    onAppearanceChange: (AppearanceSettings) -> Unit = {},
    reminderEnabled: Boolean,
    onReminderToggle: (Boolean) -> Unit,
    onCheckForUpdate: () -> Unit,
    onShareApp: () -> Unit,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAiInfoClick: () -> Unit,
    onRemindersClick: () -> Unit = {},
    onWelcomeTour: () -> Unit = {}
) {
    Scaffold(
        topBar = { BackTopBar(title = stringResource(R.string.settings_settings), onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { SettingsHeader() }
            item { com.ai.studyassistant.ui.components.FocusRewardCard("settings_focus") }
            item { AdPrivacyOptions() }
            item { com.ai.studyassistant.ui.components.BrynflowLearningCard("settings") }
            item { com.ai.studyassistant.ui.components.SupportContactCard() }
            item {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(20.dp)) { AppearancePicker(appearance, onAppearanceChange) }
                }
            }
            item { LanguageSettingRow() }
            item { Text(stringResource(R.string.settings_your_routine), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { SettingsRow(Icons.Default.School, stringResource(R.string.settings_welcome_tour), stringResource(R.string.settings_welcome_tour_sub), onWelcomeTour) }
            item { SettingsRow(Icons.Default.School, stringResource(R.string.common_my_reminders), stringResource(R.string.settings_reminders_sub), onRemindersClick) }
            item { ReminderToggleCard(enabled = reminderEnabled, onToggle = onReminderToggle) }
            item {
                SettingsRow(
                    icon = Icons.Default.SystemUpdate,
                    title = stringResource(R.string.settings_check_for_updates),
                    subtitle = stringResource(R.string.settings_see_if_a_newer_version),
                    onClick = onCheckForUpdate
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Share,
                    title = stringResource(R.string.settings_share_app),
                    subtitle = stringResource(R.string.settings_tell_a_friend_about_examai),
                    onClick = onShareApp
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_about_examai),
                    subtitle = stringResource(R.string.settings_app_info_disclaimer_official_sources),
                    onClick = onAboutClick
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.AutoAwesome,
                    title = stringResource(R.string.settings_how_ai_picks_your_questions),
                    subtitle = stringResource(R.string.settings_where_the_questions_come_from),
                    onClick = onAiInfoClick
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.settings_privacy_policy),
                    subtitle = stringResource(R.string.settings_how_we_handle_your_data),
                    onClick = onPrivacyClick
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Gavel,
                    title = stringResource(R.string.settings_terms_conditions),
                    subtitle = stringResource(R.string.settings_rules_for_using_examai),
                    onClick = onTermsClick
                )
            }
        }
    }
}

@Composable
private fun SettingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(12.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = stringResource(R.string.home_examai),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.settings_smart_study_assistant),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReminderToggleCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.reminders_daily_study_reminders),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.settings_optional_up_to_2_a),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    AIStudyAssistantTheme {
        SettingsContent(
            reminderEnabled = true,
            onReminderToggle = {},
            onCheckForUpdate = {},
            onShareApp = {},
            onBack = {},
            onAboutClick = {},
            onTermsClick = {},
            onPrivacyClick = {},
            onAiInfoClick = {}
        )
    }
}

@Composable
private fun AdPrivacyOptions() {
    val required by com.ai.studyassistant.ads.ConsentManager.privacyOptionsRequired.collectAsState()
    val context = LocalContext.current
    if (required) {
        SettingsRow(Icons.Default.Lock, stringResource(R.string.settings_ad_privacy), stringResource(R.string.settings_ad_privacy_sub)) {
            context.findActivity()?.let { com.ai.studyassistant.ads.ConsentManager.showPrivacyOptions(it) }
        }
    }
}

@Composable
private fun LanguageSettingRow() {
    val context = LocalContext.current
    var open by remember { mutableStateOf(false) }
    val current = remember(open) { LanguageManager.currentTag(context) }
    SettingsRow(
        Icons.Default.Language,
        stringResource(R.string.settings_language),
        LanguageManager.nativeNameFor(current) ?: stringResource(R.string.language_system_default)
    ) { open = true }
    if (open) LanguageDialog(onDismiss = { open = false })
}
