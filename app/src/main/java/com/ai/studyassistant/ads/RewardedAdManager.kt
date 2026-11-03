package com.ai.studyassistant.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.ai.studyassistant.analytics.AnalyticsManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RewardedStatus { IDLE, LOADING, READY, SHOWING, UNAVAILABLE }

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null
    private var loadedAt = 0L
    private var generation = 0
    private val _status = MutableStateFlow(RewardedStatus.IDLE)
    val status = _status.asStateFlow()

    fun invalidate() {
        generation++
        rewardedAd = null
        if (_status.value != RewardedStatus.SHOWING) _status.value = RewardedStatus.IDLE
    }

    fun preload(context: Context) {
        if (!ADS_ENABLED || !ConsentManager.adsReady.value || AdExperience.fullscreenShowing) return
        if (rewardedAd != null && SystemClock.elapsedRealtime() - loadedAt > 55 * 60_000) invalidate()
        if (rewardedAd != null || _status.value == RewardedStatus.LOADING) return
        _status.value = RewardedStatus.LOADING
        val requestGeneration = generation
        RewardedAd.load(context.applicationContext, AdIds.rewarded(context), AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                if (requestGeneration != generation) return
                if (!ConsentManager.adsReady.value) { invalidate(); return }
                rewardedAd = ad
                loadedAt = SystemClock.elapsedRealtime()
                _status.value = RewardedStatus.READY
                AnalyticsManager.logEvent("rewarded_ad_loaded")
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                if (requestGeneration != generation) return
                rewardedAd = null
                _status.value = RewardedStatus.UNAVAILABLE
                AnalyticsManager.logEvent("rewarded_ad_load_failed", mapOf("code" to error.code))
            }
        })
    }

    fun showIfAvailable(activity: Activity, placement: String, onRewarded: () -> Unit, onDismissed: (Boolean) -> Unit) {
        val ad = rewardedAd
        if (!ADS_ENABLED || !ConsentManager.adsReady.value || ad == null || AdExperience.fullscreenShowing ||
            activity.isFinishing || activity.isDestroyed || SystemClock.elapsedRealtime() - loadedAt > 55 * 60_000) {
            onDismissed(false)
            preload(activity)
            return
        }
        rewardedAd = null // Consume before showing, so repeated taps cannot reuse the ad.
        AdExperience.fullscreenShowing = true
        _status.value = RewardedStatus.SHOWING
        var earned = false
        var ended = false
        fun end() {
            if (ended) return
            ended = true
            AdExperience.fullscreenEnded()
            _status.value = RewardedStatus.IDLE
            onDismissed(earned)
            preload(activity.applicationContext)
        }
        ad.setOnPaidEventListener { value ->
            AnalyticsManager.logEvent("ad_revenue", mapOf("format" to "rewarded", "placement" to placement,
                "value_micros" to value.valueMicros, "currency" to value.currencyCode, "precision" to value.precisionType))
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() { AnalyticsManager.logEvent("rewarded_ad_shown", mapOf("placement" to placement)) }
            override fun onAdDismissedFullScreenContent() = end()
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                AnalyticsManager.logEvent("rewarded_ad_show_failed", mapOf("placement" to placement, "code" to error.code))
                end()
            }
        }
        ad.show(activity) {
            if (!earned) {
                earned = true
                AnalyticsManager.logEvent("rewarded_ad_earned", mapOf("placement" to placement))
                onRewarded()
            }
        }
    }
}
