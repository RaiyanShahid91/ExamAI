package com.ai.studyassistant.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.ai.studyassistant.ads.*
import com.ai.studyassistant.analytics.AnalyticsManager
import com.google.android.gms.ads.*

/** Banner inventory is suppressed immediately when an earned focus break starts. */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    if (!ADS_ENABLED) return
    val adsReady by ConsentManager.adsReady.collectAsState()
    val focusMinutes by AdExperience.remainingMinutes.collectAsState()
    if (!adsReady || focusMinutes > 0) return
    val context = LocalContext.current
    val adView = remember(context) {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = AdIds.banner(context)
            setOnPaidEventListener { value ->
                AnalyticsManager.logEvent("ad_revenue", mapOf("format" to "banner", "value_micros" to value.valueMicros, "currency" to value.currencyCode))
            }
            loadAd(AdRequest.Builder().build())
        }
    }
    DisposableEffect(adView) { onDispose { adView.destroy() } }
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AndroidView(factory = { adView })
    }
}
