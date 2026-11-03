package com.ai.studyassistant.notification

/** Android decides whether a runtime dialog may appear again after a denial. */
enum class StartupPermissionAction { NONE, REQUEST, SETTINGS }

fun startupPermissionAction(notificationsEnabled: Boolean, runtimePermissionNeeded: Boolean,
    requestedBefore: Boolean, canExplainRetry: Boolean): StartupPermissionAction = when {
    notificationsEnabled -> StartupPermissionAction.NONE
    !runtimePermissionNeeded -> StartupPermissionAction.SETTINGS
    requestedBefore && !canExplainRetry -> StartupPermissionAction.SETTINGS
    else -> StartupPermissionAction.REQUEST
}
