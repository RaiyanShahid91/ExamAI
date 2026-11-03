package com.ai.studyassistant.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.ai.studyassistant.R
import com.ai.studyassistant.i18n.LanguageManager

object AppLinks {
    const val SUPPORT_EMAIL = "contact@brynflow.com"
    const val EXAM_PREP_URL = "https://brynflow.com/exams"

    fun openExamPrep(context: Context) = open(context, Intent(Intent.ACTION_VIEW, Uri.parse(EXAM_PREP_URL)), R.string.link_no_browser)

    fun contactSupport(context: Context) = open(context,
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL")),
        R.string.link_no_email_app, SUPPORT_EMAIL)

    private fun open(context: Context, intent: Intent, @androidx.annotation.StringRes fallback: Int, vararg args: Any) {
        try { context.startActivity(intent) }
        catch (_: ActivityNotFoundException) { Toast.makeText(context, LanguageManager.localized(context).getString(fallback, *args), Toast.LENGTH_LONG).show() }
    }
}
