package com.ai.studyassistant.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.ai.studyassistant.ads.ADS_ENABLED
import com.ai.studyassistant.ads.AdIds
import com.ai.studyassistant.ads.ConsentManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * A standard (320x50) banner ad, only rendered once Mobile Ads has finished
 * initializing (post-consent). Renders nothing beforehand or if ads fail to load,
 * so it never leaves a dead gap in the layout.
 */
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    if (!ADS_ENABLED) return
    val adsReady by ConsentManager.adsReady.collectAsState()
    if (!adsReady) return

    val context = LocalContext.current
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdIds.BANNER
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
