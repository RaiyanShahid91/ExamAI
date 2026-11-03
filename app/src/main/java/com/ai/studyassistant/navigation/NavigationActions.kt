package com.ai.studyassistant.navigation

import androidx.navigation.NavHostController

/** Shortcuts can reach a quiz without ever adding its picker to the back stack. */
fun NavHostController.returnToOrOpen(route: String) {
    if (!popBackStack(route, inclusive = false)) {
        navigate(route) {
            popUpTo(Routes.HOME) { inclusive = false }
            launchSingleTop = true
        }
    }
}
