package com.ai.studyassistant.analytics

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics

/**
 * Single entry point for Firebase Analytics + Crashlytics across the app. Screens log a
 * view once via [logScreenView] (wired centrally in NavGraph — see its destination-changed
 * listener), and call sites log the specific user action that just happened via [logEvent].
 * [recordNonFatal] surfaces errors that used to be silently swallowed, so they now show up
 * in the Crashlytics console instead of vanishing.
 */
object AnalyticsManager {

    private val analytics: FirebaseAnalytics get() = Firebase.analytics
    private val crashlytics: FirebaseCrashlytics get() = Firebase.crashlytics

    fun logScreenView(screenName: String, screenClass: String = screenName) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        crashlytics.log("screen_view: $screenName")
    }

    fun logEvent(name: String, params: Map<String, Any?> = emptyMap()) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                null -> Unit
                is Boolean -> bundle.putString(key, value.toString())
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        analytics.logEvent(name, bundle)
        crashlytics.log("event: $name ${if (params.isEmpty()) "" else params}")
    }

    /** Surfaces an error to Crashlytics without crashing the app — for failures that were
     * previously caught and silently ignored. */
    fun recordNonFatal(throwable: Throwable, context: String? = null) {
        if (context != null) crashlytics.log("non-fatal in $context")
        crashlytics.recordException(throwable)
    }
}
