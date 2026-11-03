# Privacy Policy — ExamAI Smart Study Assistant

Last updated: November 3, 2026 (app version 1.2.3)

App: ExamAI – Smart Study Assistant (com.ai.studyassistant)
Developer: Raiyan Shahid
Support and privacy contact: contact@brynflow.com
Website: https://brynflow.com/exams

## 1. About this policy

This policy explains the information used by ExamAI for quizzes, study plans, revision, flashcards, notes, progress insights, reminders, language settings and sharing, as well as the third-party services included in the app. No account is required. Our app uses Google AdMob, Firebase Analytics and Firebase Crashlytics; these services process information as described below. The sections below describe their data processing alongside the app’s local storage.

Your saved study records are held on your device, while content providers, advertising, analytics and crash-reporting services can receive the information described below. Local storage does not mean the app has no network activity or third-party data processing.

Reading or using the app does not itself grant notification permission, advertising consent, or precise alarm access. Those choices have separate controls.

## 2. Study data stored on your device

The app stores quiz attempts and answers, scores, unfinished quizzes, bookmarks, cached questions, saved study notes, revision schedules, daily study goals, your optional exam date, theme choices, app language and other preferences in its local database or preference files.

**Study plans and exam dates.** Your selected exam, daily question goal and optional exam date are saved locally. The app combines them with saved quiz and review records to show daily progress, a countdown and a suggested practice pace. The suggestion is a study-planning aid, not an official exam schedule or a prediction of your result. You can change the goal, change the date or clear the exam date from the daily-plan editor.

**Revision and flashcards.** Missed questions are saved for spaced revision, with review history and due dates. Flashcards are assembled on your device from missed and bookmarked questions, including the question, options, correct answer, explanation and exam/subject/topic labels. A flashcard session keeps its current queue and learned/retry counts in memory; choosing “Got it” only retires the card from that session. It does not delete the source question or change its spaced-review schedule. The app does not upload a flashcard deck to a generative-AI or translation service.

**Progress insights.** Counts, accuracy, practice days, streaks, weak-topic suggestions and achievement badges are calculated on your device from saved quiz attempts. These local calculations are separate from Firebase usage analytics described in section 7. They are study aids and are not shared automatically as a report or CSV export.

**Language and appearance.** Your chosen language, color palette and light/dark/system mode are preferences. Android 13 and later manages the app-language selection through its per-app language service; older Android versions store the language choice in the app's preferences. Interface translations are bundled with the app, so choosing a language does not send text to an online translation service. Questions, subject/topic names, fetched study content and legal/information pages may remain in English. Language selection is not an advertising or analytics opt-out.

Personal reminders store the title and optional note you enter, the selected date/time, an identifier and delivery status. Daily-reminder preferences and the last delivery date for each daily slot are also stored locally. Search allowances and the expiry of earned ad-free periods are saved locally.

We do not operate a server that receives your personal reminder titles, notes or schedules. Reminder notifications are scheduled on your device, not sent by a remote push-notification service. The operating system uses notification content to display the reminder. The app allows Android backup and device transfer and does not exclude any of its data, so Google Drive backup or a device-to-device transfer may copy locally stored app data (such as your study history, notes and reminders) according to your device settings and the applicable platform policies; local storage is not a promise that device backups never occur.

Avoid entering sensitive information in a reminder. Notifications use private lock-screen visibility and a generic public preview; what is visible also depends on your device's notification settings.

## 3. Notifications and your choices

Daily study reminders are optional and off until you enable them in Settings or My reminders. Enabling notifications for a personal reminder does not automatically enable daily study nudges. Earlier app-level permission grants are not treated as consent to the new daily schedule.

If you enable daily reminders, we schedule up to two study notifications per day, targeting 10 AM and 8 PM in your device's local timezone. Their content may use your locally stored unfinished quiz, due revision questions or recently practiced topic to suggest a useful next step. We skip these nudges while the app is in use and suppress substantially delayed daily reminders outside their morning/evening window. We do not use notification content to serve ads, rewarded-ad offers or website promotions.

Personal reminders send one notification for each reminder you create at the date/time you choose. You can edit, delete or schedule a delivered reminder again. Personal reminders are independent of the daily-reminder switch. Tapping a notification opens the relevant study screen or reminder list; it does not automatically open a website or rewarded ad.

On Android 13 and later, notifications require your permission. The app requests notification permission on the first opening and may ask again on a later opening if access was denied. It does not repeatedly request permission within the same app session. When Android no longer permits the system dialog, a dismissible explanation offers a link to notification settings. Returning from that settings screen does not immediately show another prompt. You may also enable notifications when setting up a reminder. Declining permission does not prevent quizzes, notes or other study features. You can disable daily reminders in the app or with the notification's “Turn off daily reminders” action, delete personal reminders, or turn off either notification channel or all notifications in Android settings.

Android can delay notification delivery because of battery restrictions, force-stop, disabled channels or other system conditions. Daily reminders use inexact scheduling. Personal reminders can use precise scheduling only where supported and permitted. The optional “Allow precise timing” button opens Android's Alarms & reminders access screen; without access, reminders remain available with potentially delayed delivery. We do not request the restricted USE_EXACT_ALARM permission or full-screen notification access. Saved schedules are restored after restart, app updates and relevant clock/timezone changes without re-enabling opted-out daily reminders.

## 4. Permissions and platform services

- INTERNET and ACCESS_NETWORK_STATE support content retrieval, ads and the other network services described here.
- POST_NOTIFICATIONS is optional on Android 13 and later and supports the reminders you choose.
- RECEIVE_BOOT_COMPLETED restores pending schedules after a restart.
- SCHEDULE_EXACT_ALARM is optional special access for precisely timed personal reminders. Daily engagement reminders do not use exact alarms.
- Bundled Google SDKs may declare additional platform permissions, including advertising-ID and network-related permissions, for their documented functions.

The app does not request access to your contacts, microphone, camera, SMS or precise device location for its study/reminder features.

## 5. Content requests, sharing and app updates

Open Trivia Database provides quiz content, Wikipedia provides topic summaries, and the app may request content from NCERT. Requests can include the category, subject or topic/search term and normal network metadata such as your IP address and app user-agent. These providers process requests under their own policies.

The app may read the NCERT textbook index page to find chapter links. Those requests do not attach your saved answers, exam date or reminder records, but the provider can still receive normal network metadata. Public-source content is supplementary study material, not a promise of official or previously asked exam questions.

Provider information:
https://opentdb.com/
https://foundation.wikimedia.org/wiki/Policy:Privacy_policy
https://ncert.nic.in/

**Sharing your score.** If you tap Share on a result, the app creates a score-card image from your result (such as exam, score and date), saves it temporarily in the app's cache and hands it to the app you choose in Android's share sheet. We do not receive the image; the app you share to handles it under its own policy.

**Exporting your history.** If you tap "Export my history" on the Insights screen, the app creates a CSV file of your completed quizzes (date/time, exam, subject, topic, practice/timed/mock type, total and answered question counts, correct answers, accuracy and duration) in its cache and hands it to the app you choose in Android's share sheet. The file can reveal your study history, so check the receiving app and recipient before sharing. Copies saved or sent outside ExamAI are controlled by you and the receiving service; deleting local quiz records does not delete those copies. We do not receive the file through an app-operated upload service; the app you share it with handles it under its own policy.

**Home-screen widget.** The optional study widget shows your study streak and number of reviews due. It is built from data stored on your device, uses the selected app language, and makes no network requests of its own. Anyone who can view your unlocked home screen may see those counts. Removing the widget does not delete your study records.

**App updates.** The app uses Google Play's In-App Updates to check whether a newer version is available and to download it. This check is performed by Google Play services using your Play account and device information under Google's terms. We do not run our own update server and the app logs update-check and update-flow events (for example, started or install confirmed) in analytics. Updates work only for installs from Google Play.

Brynflow exam-preparation links open https://brynflow.com/exams in your browser only after you tap them. The app records a link-open event and source screen in analytics; the website and browser may separately process standard browsing data under their own policies. Other external links, including official exam-authority links, also open only after a user action. Do not assume a website's data practices are identical to this app's practices.

## 6. Advertising and rewards

ExamAI includes AdMob banner, interstitial and optional rewarded ads. Google and its advertising partners may process identifiers such as the advertising ID, IP-derived approximate location, device/app information and ad-interaction data for ad delivery, measurement, fraud prevention and related purposes.

Rewarded ads require an explicit tap on a disclosed offer. Examples include two additional note searches or a 15-minute break from banners and interstitials. A reward is granted only after the ad SDK reports it earned; closing an ad early may not earn a reward. Notifications are not used as ad placements. The app logs ad loading/showing/reward events and SDK paid-event information, such as revenue value, currency, ad format and rewarded placement, through Firebase.

Where applicable, Google's User Messaging Platform gathers privacy choices before ad requests are permitted. Ad privacy choices appear in Settings when the SDK requires that entry point. Notification permission and daily-reminder consent are separate from advertising choices.

Google advertising information: https://policies.google.com/technologies/ads

## 7. Analytics and crash reporting

Firebase Analytics and Crashlytics process information such as installation/app-instance identifiers, device and operating-system details, app version, usage events, screens visited, exam/subject/topic selections, note-search activity, quiz completion and scores, bookmark, review-session, mock-exam, timed-mode, daily-question and score-sharing actions, reminder toggles (not reminder content), opening the Insights screen or Flashcards, finishing a flashcard deck (not its contents), history exports (not their contents), app-language changes (the chosen language code), update-check and update-flow events, ad events, crash stack traces, error details and diagnostic logs. These identifiers can distinguish an app installation and can be associated with a device or app instance.

Note-search events include the topic or search text you enter, and study events can include exam/subject/topic labels, scores, counts and local attempt identifiers. The app also adds event parameters to Crashlytics diagnostic breadcrumbs. Avoid entering sensitive personal information in a topic search. App-language changes send the selected language code, or “system”; they do not send the translated interface text. Opening Insights, opening or completing flashcards, and requesting a history export log feature-use events, not the contents of the generated deck or exported file. The optional exam date and personal reminder contents are not intentionally included in these event parameters.

Ad privacy choices control the advertising SDK where applicable; they are not a separate in-app switch for Firebase Analytics or Crashlytics. The app currently has no dedicated in-app analytics or crash-reporting opt-out.

Personal reminder titles, optional notes and schedules are not intentionally included in analytics events or remote crash logs. Generic reminder-delivery errors may be reported. We use analytics, ad measurement and diagnostics to understand feature use, measure monetization and improve reliability. Google may process this information outside your country according to its service terms and privacy information.

Firebase privacy information: https://firebase.google.com/support/privacy
Google privacy information: https://policies.google.com/privacy

## 8. Contacting support

If you choose to email contact@brynflow.com, your email provider and our support email service process the sender address, message and attachments you choose to send. We use that information to respond to your request and handle related support or privacy concerns. An email address is not required to use the app. Do not include sensitive information that is unnecessary for your request.

## 9. Retention, deletion and security

Local study data and delivered personal reminders remain until you remove them using the controls available for those items, clear the app's storage in Android settings, or uninstall it. Derived insights and flashcard decks are rebuilt from the remaining source records; marking a flashcard learned is not a data-deletion action. Changing the app language or removing the widget does not clear study history. Exported CSV files, score images and text reports are created in the app cache; Android may clear cached files, and they can also remain until overwritten or the cache is cleared. Copies shared to other apps are outside the app's deletion controls. Clearing only the cache does not necessarily delete the database or preferences. Deleting a pending personal reminder cancels its scheduled notification. Turning off daily reminders cancels their schedules and removes currently posted daily nudges; it does not delete your personal reminders.

Backup copies, support correspondence and data processed by Google or other providers are subject to their applicable retention/deletion arrangements. Clearing app storage does not by itself guarantee deletion of data already processed by third-party services or stored in platform backups. Contact us with privacy requests, and use the relevant provider's controls where applicable.

Device security and Android's app sandbox protect local files. Network requests use the providers' HTTPS endpoints. No storage or transmission method can be guaranteed completely secure.

## 10. Students and children's privacy

Students, including minors, may use the app. The app does not currently include an age-verification flow. Ad requests cap creative content at Google's Teen rating; this setting does not verify a user's age. Parents or guardians with questions about advertising, analytics or a child's information can contact contact@brynflow.com.

## 11. Changes and contact

We update the date on this policy when its content changes. Notification, advertising and special-access choices remain governed by their separate controls; a policy update does not override a denial or opt-out.

For questions, concerns or privacy requests, email contact@brynflow.com. We use support correspondence to respond to your request.
