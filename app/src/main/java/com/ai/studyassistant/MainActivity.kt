package com.ai.studyassistant

import android.graphics.Color as AndroidColor
import android.os.Bundle
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android enforces edge-to-edge from API 35+, so the scrim colors below are mostly
        // decorative (the platform keeps system bars transparent regardless) — the part that
        // actually and reliably applies is the icon appearance. Most screens have a light top
        // bar, so dark icons are the right default; do not switch this back to .dark()/light
        // icons without checking every screen's top-bar color first.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        ConsentManager.requestConsentAndInitialize(this)
        setContent {
            AIStudyAssistantTheme {
                ExamAIApp()
            }
        }
    }
}

@Composable
private fun ExamAIApp() {
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
