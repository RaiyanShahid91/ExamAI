package com.ai.studyassistant.ads

import android.app.Activity
import android.content.Context
import com.ai.studyassistant.analytics.AnalyticsManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/** Shows at quiz completion, subject to consent, a focus reward and a three-minute cooldown. */
object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null

    @Volatile private var isLoading = false
    private var loadedAt = 0L
    private var generation = 0
    fun invalidate() { generation++; interstitialAd = null; isLoading = false }


    fun preload(context: Context) {
        if (!ADS_ENABLED || !ConsentManager.adsReady.value || AdExperience.isFocusActive || isLoading) return
        if (interstitialAd != null && android.os.SystemClock.elapsedRealtime() - loadedAt > 55 * 60_000) invalidate()
        if (interstitialAd != null) return
        isLoading = true
        val requestGeneration = generation
        InterstitialAd.load(
            context,
            AdIds.interstitial(context),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    if (requestGeneration != generation) return
                    if (!ConsentManager.adsReady.value) { isLoading = false; return }
                    loadedAt = android.os.SystemClock.elapsedRealtime()
                    interstitialAd = ad
                    isLoading = false
                    AnalyticsManager.logEvent("interstitial_ad_loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (requestGeneration != generation) return
                    interstitialAd = null
                    isLoading = false
                    AnalyticsManager.recordNonFatal(
                        Exception("Interstitial load failed: ${error.code} ${error.message}"),
                        context = "InterstitialAdManager.preload"
                    )
                }
            }
        )
    }

    fun showIfAvailable(activity: Activity, onDismissed: () -> Unit = {}) {
        if (!ADS_ENABLED || !ConsentManager.adsReady.value || !AdExperience.canShowInterstitial() || activity.isFinishing || activity.isDestroyed) {
            onDismissed()
            return
        }
        val ad = interstitialAd
        if (ad == null || android.os.SystemClock.elapsedRealtime() - loadedAt > 55 * 60_000) {
            onDismissed()
            return
        }
        interstitialAd = null
        AdExperience.fullscreenShowing = true
        ad.setOnPaidEventListener { value ->
            AnalyticsManager.logEvent("ad_revenue", mapOf("format" to "interstitial", "value_micros" to value.valueMicros, "currency" to value.currencyCode))
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                AdExperience.fullscreenEnded()
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                AdExperience.fullscreenEnded()
                AnalyticsManager.recordNonFatal(
                    Exception("Interstitial show failed: ${error.code} ${error.message}"),
                    context = "InterstitialAdManager.showIfAvailable"
                )
                onDismissed()
            }
        }
        AnalyticsManager.logEvent("interstitial_ad_shown")
        ad.show(activity)
    }
}
