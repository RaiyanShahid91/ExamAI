package com.ai.studyassistant.ads

import android.app.Activity
import android.content.Context
import com.ai.studyassistant.analytics.AnalyticsManager
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

    private val _privacyOptionsRequired = MutableStateFlow(false)
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired
    @Volatile private var initializeStarted = false
    private var sdkInitialized = false

    private fun updatePermission(context: Context) {
        val info = UserMessagingPlatform.getConsentInformation(context)
        _privacyOptionsRequired.value = info.privacyOptionsRequirementStatus ==
            com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        _adsReady.value = sdkInitialized && info.canRequestAds()
        if (!info.canRequestAds()) {
            RewardedAdManager.invalidate()
            InterstitialAdManager.invalidate()
        } else initializeAdsIfNeeded(context)
    }

    fun showPrivacyOptions(activity: Activity) {
        _adsReady.value = false
        RewardedAdManager.invalidate()
        InterstitialAdManager.invalidate()
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { updatePermission(activity) }
    }

    fun requestConsentAndInitialize(activity: Activity) {
        AdExperience.initialize(activity)
        val params = ConsentRequestParameters.Builder().build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    updatePermission(activity)
                }
            },
            { formError ->
                // Previous consent can be reused only when the SDK allows requests.
                AnalyticsManager.recordNonFatal(
                    Exception("Consent info update failed: ${formError.errorCode} ${formError.message}"),
                    context = "ConsentManager.requestConsentAndInitialize"
                )
                updatePermission(activity)
            }
        )

        if (consentInformation.canRequestAds()) {
            initializeAdsIfNeeded(activity)
        }
    }

    private fun initializeAdsIfNeeded(context: Context) {
        if (!ADS_ENABLED || !UserMessagingPlatform.getConsentInformation(context).canRequestAds() || initializeStarted) return
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
            sdkInitialized = true
            _adsReady.value = UserMessagingPlatform.getConsentInformation(context).canRequestAds()
            if (_adsReady.value) RewardedAdManager.preload(context.applicationContext)
        }
    }
}
