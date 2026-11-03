package com.ai.studyassistant.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ai.studyassistant.ads.*
import com.ai.studyassistant.ui.util.findActivity
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.ai.studyassistant.R

/** Each tap explicitly opts in to one ad. No reward is given on dismissal alone. */
@Composable
fun RewardedOfferCard(
    title: String,
    description: String,
    placement: String,
    onReward: () -> Unit
) {
    if (!ADS_ENABLED) return
    val context = LocalContext.current
    val ready by ConsentManager.adsReady.collectAsState()
    val status by RewardedAdManager.status.collectAsState()
    var message by remember { mutableStateOf<String?>(null) }
    var watching by remember { mutableStateOf(false) }
    val currentReward by rememberUpdatedState(onReward)
    LaunchedEffect(ready) {
        if (ready) {
            while (true) {
                RewardedAdManager.preload(context)
                delay(60_000)
            }
        }
    }
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.rewarded_offer_optional_reward), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Button(
                enabled = !watching && ready && status != RewardedStatus.LOADING && status != RewardedStatus.SHOWING,
                onClick = {
                    message = null
                    if (status != RewardedStatus.READY) {
                        RewardedAdManager.preload(context)
                        message = context.getString(R.string.rewarded_offer_requesting_an_ad_please_try)
                    } else {
                        val activity = context.findActivity()
                        if (activity != null) {
                            watching = true
                            RewardedAdManager.showIfAvailable(activity, placement, onRewarded = { currentReward() }, onDismissed = { earned ->
                                watching = false
                                message = context.getString(if (earned) R.string.reward_unlocked else R.string.reward_not_earned)
                            })
                        } else message = context.getString(R.string.rewarded_offer_unable_to_show_an_ad)
                    }
                }
            ) {
                Icon(Icons.Default.OndemandVideo, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(when {
                    watching -> stringResource(R.string.reward_ad_in_progress)
                    !ready -> stringResource(R.string.reward_ads_unavailable)
                    status == RewardedStatus.LOADING -> stringResource(R.string.reward_loading_ad)
                    status == RewardedStatus.READY -> stringResource(R.string.reward_watch_ad)
                    else -> stringResource(R.string.reward_retry_loading)
                })
            }
            Text(stringResource(R.string.rewarded_offer_complete_one_ad_to_receive), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun FocusRewardCard(placement: String) {
    if (!ADS_ENABLED) return
    val context = LocalContext.current
    val remaining by AdExperience.remainingMinutes.collectAsState()
    if (remaining > 0) {
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.rewarded_offer_focus_break_active_min_left, remaining), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        RewardedOfferCard(stringResource(R.string.reward_focus_title), stringResource(R.string.reward_focus_body), placement) {
            AdExperience.grantFocus(context)
        }
    }
}
