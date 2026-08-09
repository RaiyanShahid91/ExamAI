package com.ai.studyassistant.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Wraps Google's User Messaging Platform (UMP) SDK, required by AdMob policy to gather
 * consent (GDPR/EEA/UK) before requesting ads, and initializes the Mobile Ads SDK once
 * consent is resolved (or immediately if no consent is required in the user's region).
 */
object ConsentManager {

    private val _adsReady = MutableStateFlow(false)
    val adsReady: StateFlow<Boolean> = _adsReady

    @Volatile private var initializeStarted = false

    fun requestConsentAndInitialize(activity: Activity) {
        val params = ConsentRequestParameters.Builder().build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    initializeAdsIfNeeded(activity)
                }
            },
            {
                // Consent info update failed — proceed anyway; ads can still load non-personalized.
                initializeAdsIfNeeded(activity)
            }
        )

        if (consentInformation.canRequestAds()) {
            initializeAdsIfNeeded(activity)
        }
    }

    private fun initializeAdsIfNeeded(context: Context) {
        if (initializeStarted) return
        initializeStarted = true
        // ExamAI's audience is students preparing for competitive exams, which realistically
        // includes some under-18 users — cap ad content to "Teen" so AdMob never serves
        // mature-rated ad creative in this app, regardless of the advertiser's own targeting.
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_T)
                .build()
        )
        MobileAds.initialize(context) {
            _adsReady.value = true
        }
    }
}
