package com.ai.studyassistant

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.ai.studyassistant.notification.ReminderScheduler
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import com.ai.studyassistant.data.local.AppearancePreferences
import com.ai.studyassistant.ui.theme.ThemeMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.ai.studyassistant.ads.ConsentManager
import com.ai.studyassistant.data.local.FirstRunPreferences
import com.ai.studyassistant.navigation.NavGraph
import com.ai.studyassistant.navigation.Routes
import com.ai.studyassistant.ui.components.UpdateAvailableDialog
import com.ai.studyassistant.ui.components.UpdateReadyDialog
import com.ai.studyassistant.ui.theme.AIStudyAssistantTheme
import com.ai.studyassistant.ui.util.findActivity
import com.ai.studyassistant.viewmodel.QuizViewModel
import com.ai.studyassistant.viewmodel.UpdateViewModel
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.ai.studyassistant.i18n.LanguageManager.wrap(newBase))
    }

    private var reminderDestination by mutableStateOf<String?>(null)
    private var notificationLaunchToken by mutableIntStateOf(0)
    private var checkPermissionOnReturn = false
    private var returningFromNotificationSettings = false

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!com.ai.studyassistant.ads.AdExperience.fullscreenShowing) checkPermissionOnReturn = true
    }
    override fun onResume() {
        super.onResume()
        if (returningFromNotificationSettings) {
            returningFromNotificationSettings = false
            checkPermissionOnReturn = false
        } else if (checkPermissionOnReturn) {
            checkPermissionOnReturn = false
            notificationLaunchToken++
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("notification_launch_token", notificationLaunchToken)
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_MAIN) checkPermissionOnReturn = true
        reminderDestination = intent.getStringExtra(ReminderScheduler.TARGET)
    }
    override fun onStart() {
        super.onStart()
        ReminderScheduler.appInForeground = true
        lifecycleScope.launch { ReminderScheduler.restore(applicationContext) }
    }
    override fun onStop() {
        if (!isChangingConfigurations && !returningFromNotificationSettings && !com.ai.studyassistant.ads.AdExperience.fullscreenShowing) {
            checkPermissionOnReturn = true
        }
        ReminderScheduler.appInForeground = false
        super.onStop()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.ai.studyassistant.widget.StudyWidgetProvider.refresh(this)
        com.ai.studyassistant.notification.NotificationHelper.ensureChannel(this)
        notificationLaunchToken = savedInstanceState?.getInt("notification_launch_token") ?: 0
        reminderDestination = intent.getStringExtra(ReminderScheduler.TARGET)
        // Keep bars transparent; the selected appearance controls icon contrast below.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        ConsentManager.requestConsentAndInitialize(this)
        setContent {
            val preferences = remember { AppearancePreferences(applicationContext) }
            val appearance by preferences.settings.collectAsState(initial = null)
            val settings = appearance
            if (settings != null) {
                val dark = when (settings.mode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                }
                AIStudyAssistantTheme(darkTheme = dark, palette = settings.palette) {
                    com.ai.studyassistant.ui.components.StartupNotificationPermission(notificationLaunchToken) {
                        returningFromNotificationSettings = true
                    }
                    ExamAIApp(reminderDestination) {
                        reminderDestination = null
                        intent.removeExtra(ReminderScheduler.TARGET)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamAIApp(notificationTarget: String?, onNotificationConsumed: () -> Unit) {
    val context = LocalContext.current
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val hasSeenOnboarding = FirstRunPreferences(context).hasSeenOnboarding.first()
        startDestination = if (hasSeenOnboarding) Routes.HOME else Routes.ONBOARDING
    }

    val destination = startDestination
    if (destination == null) {
        // Reading the one-time onboarding flag is effectively instant, but never render
        // Home (or anything) until we actually know — that's what caused the old
        // "flashes Home, then jumps to Onboarding" bug.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    LaunchedEffect(notificationTarget, entry?.destination?.route) {
        val target = notificationTarget
        if (target != null && entry != null && entry?.destination?.route != Routes.ONBOARDING) {
            if (target in listOf(Routes.HOME, Routes.HISTORY, Routes.REVIEW, Routes.REMINDERS) || com.ai.studyassistant.data.model.ExamData.exams.any { exam -> com.ai.studyassistant.data.model.ExamData.subjectsFor(exam.name).any { subject -> target == Routes.topic(exam.name, subject.name) } }) {
                navController.navigate(target) { launchSingleTop = true }
            }
            onNotificationConsumed()
        }
    }
    val viewModel: QuizViewModel = viewModel()
    val updateViewModel: UpdateViewModel = viewModel()
    val updateUiState by updateViewModel.uiState.collectAsState()

    val updateLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { /* Play's own flow drives progress from here via the install-state listener. */ }

    LaunchedEffect(Unit) {
        updateViewModel.checkOnLaunch()
    }

    NavGraph(
        navController = navController,
        viewModel = viewModel,
        updateViewModel = updateViewModel,
        startDestination = destination
    )

    if (updateUiState.showDialog && updateUiState.availableUpdateInfo != null) {
        UpdateAvailableDialog(
            onUpdate = {
                context.findActivity()?.let { activity ->
                    updateViewModel.startUpdate(activity, updateLauncher)
                }
            },
            onCancel = updateViewModel::dismissDialog
        )
    }

    if (updateUiState.installReady) {
        UpdateReadyDialog(
            onRestart = updateViewModel::completeInstall,
            onLater = updateViewModel::dismissInstallReady
        )
    }
}
