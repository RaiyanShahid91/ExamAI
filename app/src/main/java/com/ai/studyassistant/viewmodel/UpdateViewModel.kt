package com.ai.studyassistant.viewmodel

import android.app.Activity
import android.app.Application
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.AndroidViewModel
import com.ai.studyassistant.analytics.AnalyticsManager
import com.ai.studyassistant.update.UpdateManager
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.InstallStateUpdatedListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.ai.studyassistant.i18n.LanguageManager
import com.ai.studyassistant.R

data class UpdateUiState(
    val availableUpdateInfo: AppUpdateInfo? = null,
    val showDialog: Boolean = false,
    val installReady: Boolean = false,
    val statusMessage: String? = null
)

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private var hasAutoChecked = false
    private val installListener: InstallStateUpdatedListener =
        UpdateManager.registerInstallListener(application) {
            _uiState.update { it.copy(installReady = true, showDialog = false) }
        }

    /** Runs once per process — call from the app's root composable on first launch. */
    fun checkOnLaunch() {
        if (hasAutoChecked) return
        hasAutoChecked = true
        checkForUpdate(manual = false)
    }

    fun checkForUpdate(manual: Boolean) {
        AnalyticsManager.logEvent("app_update_check", mapOf("manual" to manual))
        UpdateManager.checkForUpdate(
            getApplication(),
            onAvailable = { info ->
                _uiState.update { it.copy(availableUpdateInfo = info, showDialog = true, statusMessage = null) }
            },
            onResumable = { info ->
                // A flexible update was already accepted and is still downloading (e.g. the
                // app was backgrounded mid-flow) — resume it silently, no need to re-prompt.
                _uiState.update { it.copy(availableUpdateInfo = info) }
            },
            onNotAvailable = {
                if (manual) {
                    _uiState.update { it.copy(statusMessage = msg(R.string.update_latest_version)) }
                }
            },
            onFailure = {
                if (manual) _uiState.update { it.copy(statusMessage = msg(R.string.update_check_failed)) }
            }
        )
    }

    fun startUpdate(activity: Activity, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        val info = _uiState.value.availableUpdateInfo ?: return
        UpdateManager.startFlexibleUpdate(activity, info, launcher)
        _uiState.update { it.copy(showDialog = false) }
    }

    private fun msg(@androidx.annotation.StringRes id: Int, vararg args: Any): String =
        LanguageManager.localized(getApplication<Application>()).getString(id, *args)

    fun dismissDialog() {
        _uiState.update { it.copy(showDialog = false) }
    }

    /** Hides the "ready to restart" prompt without discarding the download — Play still
     *  applies it on the app's next natural restart even if the user never taps Restart. */
    fun dismissInstallReady() {
        _uiState.update { it.copy(installReady = false) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun completeInstall() {
        UpdateManager.completeUpdate(getApplication())
    }

    override fun onCleared() {
        super.onCleared()
        UpdateManager.unregisterInstallListener(getApplication(), installListener)
    }
}
