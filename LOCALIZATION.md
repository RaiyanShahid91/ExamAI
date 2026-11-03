# App localization

The UI supports English (`en`), Hindi (`hi`), Bengali (`bn`), Marathi (`mr`), Telugu (`te`), Tamil (`ta`), Urdu (`ur`), Kannada (`kn`), Malayalam (`ml`) and Gujarati (`gu`). Select a language in Settings or using the globe on the welcome tour. System default follows the phone's preferred supported language.

Android 13+ stores the selection in the system's per-app language settings. Android 8–12 saves the language locally and applies it when recreating the activity. Urdu uses right-to-left layout and mirrored navigation arrows; its answered/goal ratio keeps left-to-right numeric order. Navigation labels allow two lines and use compact translated labels. Notifications and the study widget use the selected language too; opening the app refreshes existing widget text and notification channel names.

Each translation file covers all 386 translatable strings and eight plural resources from `app/src/main/res/values/strings.xml`. App branding is not translated. Questions, subject/topic names, fetched study content, About, AI/source information, Terms and Privacy Policy remain in English. CSV column names remain stable for data portability.

Translations were drafted with Google Translate during development, with corrections for UI context, counters, countdowns and reward wording. Translations are bundled; the app makes no translation-service requests. Native-speaker review of all nine languages remains a release-quality follow-up, especially study terminology and longer explanatory text. This implementation does not claim native-speaker approval.

## Maintaining resources

Add or change a string in the default file and all nine `values-<language>/strings.xml` files. Preserve numbered placeholders (`%1$s`, `%2$d`) and `%%` in formatted strings. Use `formatted="false"` for plain text containing a literal percent sign. Escape apostrophes and quotes for Android resources. Every plural needs an `other` form; add other grammatical forms where necessary.

`TranslationResourcesTest` checks exact resource coverage, duplicate names, plural fallback, placeholder parity, Java formatting and agreement between the language picker and system locale configuration. `LanguageFeatureTest` checks Android resource selection and plural formatting for all ten languages, then exercises the actual picker in Hindi, Tamil, Urdu and English, activity recreation and return to system default. Tests restore the prior app-language choice.

Run from the project root:

```sh
./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ai.studyassistant.i18n.LanguageFeatureTest
```

The November 3, 2026 verification used the Pixel 8 API 35 emulator. The Android 8–12 language wrapper has not been device-tested in this pass. Store listings and store screenshots were not changed.
