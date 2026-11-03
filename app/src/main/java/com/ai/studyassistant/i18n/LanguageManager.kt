package com.ai.studyassistant.i18n

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * In-app language choice.
 *
 * On Android 13+ the choice is handed to the system's per-app language service, so it also
 * shows up in Settings > Apps > ExamAI > Language and the OS recreates the UI for us.
 * On older versions we remember the tag ourselves and wrap the activity's context with that
 * locale (see [wrap]); the activity is recreated to pick it up.
 *
 * A null tag means "follow the phone's language".
 */
object LanguageManager {

    data class Language(val tag: String, val nativeName: String)

    /** Each language is shown in its own script so people can find theirs whatever the current UI language. */
    val supported = listOf(
        Language("en", "English"),
        Language("hi", "हिन्दी"),
        Language("bn", "বাংলা"),
        Language("mr", "मराठी"),
        Language("te", "తెలుగు"),
        Language("ta", "தமிழ்"),
        Language("ur", "اردو"),
        Language("kn", "ಕನ್ನಡ"),
        Language("ml", "മലയാളം"),
        Language("gu", "ગુજરાતી")
    )

    private const val PREFS = "language"
    private const val KEY = "tag"

    fun nativeNameFor(tag: String?): String? = supported.firstOrNull { it.tag == tag }?.nativeName

    /** The chosen language tag, or null when following the system language. */
    fun currentTag(context: Context): String? =
        if (Build.VERSION.SDK_INT >= 33) {
            val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
            if (locales.isEmpty) null else locales[0].language.takeIf { code -> supported.any { it.tag == code } }
        } else {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
        }

    fun apply(activity: Activity, tag: String?) {
        if (Build.VERSION.SDK_INT >= 33) {
            activity.getSystemService(LocaleManager::class.java).applicationLocales =
                if (tag == null) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(tag)
        } else {
            activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
                if (tag == null) remove(KEY) else putString(KEY, tag)
            }.apply()
            activity.recreate()
        }
    }

    /**
     * For Activity.attachBaseContext and for background code (notifications, widget) that needs
     * strings in the chosen language. On Android 13+ the system already applies it, so this is
     * a no-op there.
     */
    fun wrap(base: Context): Context {
        if (Build.VERSION.SDK_INT >= 33) return base
        val tag = base.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
        val locale = tag?.let(Locale::forLanguageTag) ?: base.resources.configuration.locales[0]
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }

    /** Alias that reads better at call sites that only want to look strings up. */
    fun localized(context: Context): Context = wrap(context)
}
