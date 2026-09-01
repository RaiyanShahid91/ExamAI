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

/**
 * Preloaded when a quiz finishes, shown (at most) once landing on the Result screen —
 * never mid-quiz, never intercepting a button tap. Frequency capping (how often it's
 * actually shown) is the caller's responsibility, this only manages load/show state.
 */
object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null

    @Volatile private var isLoading = false

    fun preload(context: Context) {
        if (!ADS_ENABLED || !ConsentManager.adsReady.value || interstitialAd != null || isLoading) return
        isLoading = true
        InterstitialAd.load(
            context,
            AdIds.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    AnalyticsManager.logEvent("interstitial_ad_loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
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
        if (!ADS_ENABLED) {
            onDismissed()
            return
        }
        val ad = interstitialAd
        if (ad == null) {
            onDismissed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
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
