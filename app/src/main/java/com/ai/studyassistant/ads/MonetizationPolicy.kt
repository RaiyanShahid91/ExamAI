package com.ai.studyassistant.ads

const val INTERSTITIAL_GAP_MS = 180_000L

data class SearchBalance(val total: Int, val dailyFree: Int) {
    fun nextDay(): SearchBalance = SearchBalance((total - dailyFree).coerceAtLeast(0) + 2, 2)
    fun consume(): SearchBalance = SearchBalance((total - 1).coerceAtLeast(0), (dailyFree - 1).coerceAtLeast(0))
    fun reward(): SearchBalance = copy(total = total + NOTE_SEARCH_REWARD)
}

fun interstitialEligible(focusActive: Boolean, fullscreenShowing: Boolean, lastShown: Long?, now: Long): Boolean =
    !focusActive && !fullscreenShowing && (lastShown?.let { now - it >= INTERSTITIAL_GAP_MS } ?: true)
