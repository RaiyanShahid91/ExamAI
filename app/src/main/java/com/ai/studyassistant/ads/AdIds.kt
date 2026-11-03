package com.ai.studyassistant.ads

import android.content.Context
import android.content.pm.ApplicationInfo

/** Release builds use the existing AdMob inventory; debug builds always request test ads. */
object AdIds {
    private fun debug(context: Context) = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    fun banner(context: Context) = if (debug(context)) "ca-app-pub-3940256099942544/6300978111" else "ca-app-pub-6654896157338132/7958544314"
    fun interstitial(context: Context) = if (debug(context)) "ca-app-pub-3940256099942544/1033173712" else "ca-app-pub-6654896157338132/3591657969"
    fun rewarded(context: Context) = if (debug(context)) "ca-app-pub-3940256099942544/5224354917" else "ca-app-pub-6654896157338132/2903876760"
}

const val ADS_ENABLED = true
