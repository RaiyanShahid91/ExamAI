package com.ai.studyassistant.update

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.ai.studyassistant.analytics.AnalyticsManager
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.startUpdateFlowForResult

/**
 * Wraps Play Core's In-App Updates API (flexible flow: user can keep using the app while
 * the update downloads in the background, then confirms a restart). Only ever reports an
 * update as available/resumable from Play Store data — there's no custom version-check
 * backend, so this only works on a device with the app installed via Play (not sideloaded).
 */
object UpdateManager {

    /** Reports whether a new version is available, or an already-started flexible update
     *  is still in progress and should be resumed (e.g. the app was backgrounded mid-flow). */
    fun checkForUpdate(
        context: Context,
        onAvailable: (AppUpdateInfo) -> Unit,
        onResumable: (AppUpdateInfo) -> Unit = {},
        onNotAvailable: () -> Unit = {}
    ) {
        val appUpdateManager = AppUpdateManagerFactory.create(context.applicationContext)
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                when {
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> onAvailable(info)
                    info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                        onResumable(info)
                    else -> onNotAvailable()
                }
            }
            .addOnFailureListener { e ->
                AnalyticsManager.recordNonFatal(e, context = "UpdateManager.checkForUpdate")
                onNotAvailable()
            }
    }

    fun startFlexibleUpdate(
        activity: Activity,
        appUpdateInfo: AppUpdateInfo,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                launcher,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            )
            AnalyticsManager.logEvent("app_update_flow_started")
        } catch (e: Exception) {
            AnalyticsManager.recordNonFatal(e, context = "UpdateManager.startFlexibleUpdate")
        }
    }

    /** Registers a listener that fires [onDownloaded] once the flexible update has
     *  finished downloading and is ready for [completeUpdate] to apply. */
    fun registerInstallListener(context: Context, onDownloaded: () -> Unit): InstallStateUpdatedListener {
        val appUpdateManager = AppUpdateManagerFactory.create(context.applicationContext)
        val listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                onDownloaded()
            }
        }
        appUpdateManager.registerListener(listener)
        return listener
    }

    fun unregisterInstallListener(context: Context, listener: InstallStateUpdatedListener) {
        AppUpdateManagerFactory.create(context.applicationContext).unregisterListener(listener)
    }

    /** Applies a downloaded flexible update — restarts the app to install it. */
    fun completeUpdate(context: Context) {
        AnalyticsManager.logEvent("app_update_install_confirmed")
        AppUpdateManagerFactory.create(context.applicationContext).completeUpdate()
    }
}
