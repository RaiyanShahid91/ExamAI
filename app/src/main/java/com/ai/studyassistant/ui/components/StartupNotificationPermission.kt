package com.ai.studyassistant.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ai.studyassistant.data.local.FirstRunPreferences
import com.ai.studyassistant.notification.*
import com.ai.studyassistant.ui.util.findActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R

@Composable
fun StartupNotificationPermission(launchToken: Int, onOpeningSettings: () -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    val preferences = remember(context) { FirstRunPreferences(context) }
    val scope = rememberCoroutineScope()
    var handledToken by rememberSaveable { mutableIntStateOf(-1) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scope.launch { ReminderScheduler.restore(context) }
        // A denial is respected for this session. Retry only on a later app opening.
    }
    LaunchedEffect(launchToken) {
        if (handledToken == launchToken) return@LaunchedEffect
        handledToken = launchToken
        val runtimeNeeded = Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        when (startupPermissionAction(NotificationHelper.canNotify(context), runtimeNeeded,
            preferences.hasPromptedNotificationPermission.first(),
            ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS))) {
            StartupPermissionAction.NONE -> showSettings = false
            StartupPermissionAction.REQUEST -> {
                preferences.setPromptedNotificationPermission()
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            StartupPermissionAction.SETTINGS -> showSettings = true
        }
    }
    if (showSettings) AlertDialog(
        onDismissRequest = { showSettings = false },
        title = { Text(stringResource(R.string.startup_notification_permission_allow_reminder_n)) },
        text = { Text(stringResource(R.string.startup_notification_permission_notification_acc)) },
        confirmButton = {
            TextButton(onClick = {
                showSettings = false
                onOpeningSettings()
                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
            }) { Text(stringResource(R.string.startup_notification_permission_open_settings)) }
        },
        dismissButton = { TextButton(onClick = { showSettings = false }) { Text(stringResource(R.string.startup_notification_permission_not_now)) } }
    )
}
