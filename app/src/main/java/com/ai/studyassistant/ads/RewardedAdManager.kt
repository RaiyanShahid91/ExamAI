package com.ai.studyassistant.ads

import android.app.Activity
import android.content.Context
import com.ai.studyassistant.analytics.AnalyticsManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Preloaded eagerly so it's ready by the time a gate (e.g. the study-notes search limit)
 * needs it. Mirrors [InterstitialAdManager]'s load/show split; the reward is only granted
 * from [RewardedAd]'s onUserEarnedReward callback, never just for the ad finishing/dismissing.
 */
object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null

    @Volatile private var isLoading = false

    fun preload(context: Context) {
        if (!ADS_ENABLED || rewardedAd != null || isLoading) return
        isLoading = true
        RewardedAd.load(
            context,
            AdIds.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    AnalyticsManager.logEvent("rewarded_ad_loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    AnalyticsManager.recordNonFatal(
                        Exception("Rewarded ad load failed: ${error.code} ${error.message}"),
                        context = "RewardedAdManager.preload"
                    )
                }
            }
        )
    }

    /**
     * Shows the rewarded ad if one is ready. [onRewarded] fires only if the user earns the
     * reward, with the reward amount as configured in the AdMob console for this ad unit
     * (read from the SDK's own callback rather than hardcoded, so a console-side change to
     * the reward amount takes effect with no app update). [onDismissed] always fires once
     * the ad flow ends (shown-then-closed or unavailable), so callers can use it to
     * re-enable a "watch ad" button.
     */
    fun showIfAvailable(activity: Activity, onRewarded: (amount: Int) -> Unit, onDismissed: () -> Unit = {}) {
        if (!ADS_ENABLED) {
            onDismissed()
            return
        }
        val ad = rewardedAd
        if (ad == null) {
            onDismissed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                AnalyticsManager.recordNonFatal(
                    Exception("Rewarded ad show failed: ${error.code} ${error.message}"),
                    context = "RewardedAdManager.showIfAvailable"
                )
                onDismissed()
            }
        }
        AnalyticsManager.logEvent("rewarded_ad_shown")
        ad.show(activity) { rewardItem ->
            AnalyticsManager.logEvent("rewarded_ad_earned", mapOf("amount" to rewardItem.amount))
            onRewarded(rewardItem.amount)
        }
    }
}
