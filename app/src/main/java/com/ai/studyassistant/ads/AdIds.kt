package com.ai.studyassistant.ads

/**
 * Live production ad unit IDs from the AdMob console (admob.google.com). Every build,
 * debug included, serves real ads through these — use a registered test device
 * (AdRequest.Builder / MobileAds.setRequestConfiguration) rather than swapping these out
 * when developing, so real impressions/clicks aren't generated from a dev device.
 */
object AdIds {
    const val BANNER = "ca-app-pub-6654896157338132/7958544314"
    const val INTERSTITIAL = "ca-app-pub-6654896157338132/3591657969"
    const val REWARDED = "ca-app-pub-6654896157338132/2903876760"
}

/**
 * Temporary kill switch: flip back to true to resume showing ads.
 * All ad placements/wiring stay in place — this just suppresses rendering.
 */
const val ADS_ENABLED = true
