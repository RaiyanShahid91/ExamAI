package com.ai.studyassistant.ads

import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

const val FOCUS_MINUTES = 15
const val NOTE_SEARCH_REWARD = 2

/** Process-wide display guard and a persistent, time-limited reward. */
object AdExperience {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _remainingMinutes = MutableStateFlow(0)
    val remainingMinutes = _remainingMinutes.asStateFlow()
    val isFocusActive: Boolean get() = _remainingMinutes.value > 0
    private var preferences: android.content.SharedPreferences? = null
    var fullscreenShowing = false
    private var lastFullscreenAt: Long? = null

    fun initialize(context: Context) {
        if (preferences != null) return
        preferences = context.applicationContext.getSharedPreferences("ad_rewards", Context.MODE_PRIVATE)
        refresh()
        scope.launch { while (isActive) { delay(1_000); refresh() } }
    }
    private fun refresh() {
        val end = preferences?.getLong("focus_until", 0) ?: 0
        _remainingMinutes.value = ((end - System.currentTimeMillis()).coerceAtLeast(0).plus(59_999) / 60_000).toInt()
    }
    fun grantFocus(context: Context) {
        initialize(context)
        preferences?.edit()?.putLong("focus_until", System.currentTimeMillis() + FOCUS_MINUTES * 60_000L)?.apply()
        refresh()
    }
    fun canShowInterstitial(now: Long = SystemClock.elapsedRealtime()): Boolean =
        interstitialEligible(isFocusActive, fullscreenShowing, lastFullscreenAt, now)

    fun fullscreenEnded() {
        fullscreenShowing = false
        lastFullscreenAt = SystemClock.elapsedRealtime()
    }
}
