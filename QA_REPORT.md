# App feature verification — November 1, 2026

Environment: Pixel 8 emulator, Android 15 / API 35, debug build with Google test ad IDs. Offline tests temporarily disabled emulator Wi-Fi/mobile data; both were restored afterward. Test-created database records were removed and changed study-plan/theme preferences restored.

## Fixed during this review

- **New Topic did nothing after a shortcut quiz.** A result screen could have no topic picker in its navigation history. Reproduced this in a failing emulator UI test before the fix. The action now returns to the matching exam/subject picker when present, or opens it when missing.
- Result retry/back navigation now handles shortcuts, ordinary quizzes and resumed mock tests. Completed quizzes are removed from the back stack so returning from results does not restart them accidentally.
- Answer persistence previously rewrote an entire attempt for answer, position and completion changes. Those writes could race. Answers now update transactionally; position and completion update separate columns. Concurrent-write and complete-quiz tests pass.
- Rapid duplicate Next taps cannot skip an unanswered question. Answer selection validates the option index.
- A resumed timed quiz no longer waits for optional Wikipedia content before starting its remaining-time/expiry handling. A stale topic summary cannot replace another topic's summary.
- Onboarding completion explicitly navigates on the main thread after saving preferences. The welcome-tour test exposed the thread issue and passes after the fix.
- History detail subscriptions are cancelled when opening a different report.
- Failed Play update checks no longer report “latest version.” About shows the installed app version rather than an outdated hard-coded version.
- Optional live trivia has a three-second wait budget before cached/bundled fallback, including a total budget for mock-test blending.
- App-return permission checks also cover a stopped/resumed activity. The single-day streak label is corrected.

## Automated results

- Debug app and Android-test APKs: build passed.
- Android lint: passed (warnings remain; this is not a zero-warning claim).
- JVM unit tests: **20 passed**, covering planner/date rollover, palette contrast, search credits, interstitial spacing and reminder/permission policies.
- Core emulator tests: **19 passed** — 16 app feature tests and 3 notification/data-migration tests.

| Feature | What was verified |
|---|---|
| Home and planner | Goal and exam changes persist; completed quizzes update study progress |
| Exam browsing | Subject/topic navigation for all seven exams, including the encoded CDS/NDA name |
| Offline question data | All 22 banks / 306 questions have usable options, valid answers and explanations; quizzes and mock repositories return questions |
| Topic quizzes | Full answer/next/result flow, correct scores, retry creates a fresh attempt |
| New Topic | Works with and without a prior topic picker; Back returns to the expected exam screen |
| Timed/resumed exams | Saved attempt resumes; expired timed mock reaches results and returns to history |
| History | Completion persists; report displays stored questions and score |
| Persistence | Concurrent answer, progress and completion writes preserve all fields |
| Bookmarks | Display and removal |
| Revision | Correct review advances the box and schedules the next due date |
| Notes offline | Saved notes open without credits; removal works; failed search keeps its credit and shows a useful error |
| Personal reminders | Create, edit, delete; notification posts once; private public-preview text; opt-out leaves personal reminders intact |
| Data upgrade | Room 4→5 preserves an existing note and adds working reminder storage |
| Appearance | Palette/mode selection persists; all five palettes have contrast tests in light/dark mode |
| Onboarding | Welcome tour opens, allows look selection and finishes at Home |
| Information pages | About, AI/source information, Privacy and Terms open; notification policy section renders |
| External actions | Brynflow HTTPS link, support mailto, app share, score image share and report share launch the expected intents. Test monitors intercepted external launches; no messages were sent |

## Online and manual checks

Online service results are recorded separately from deterministic offline checks. **The final online run passed all 3 tests:** live trivia, live notes search/save/credit use, and bounded quiz loading/fallback. Combined with the unit/core suites, the final automated runs passed 42 tests.

- A live notes search for Photosynthesis returned content, saved successfully, and consumed one search credit in both online runs.
- A direct live Open Trivia request timed out during the initial online run and recovered in the final run. The final live-question and bounded-loading checks passed; offline fallback also passed with networking disabled. External availability can still vary.
- The system notification permission dialog was displayed after launching with a simulated previously denied permission state. Granted notification access was also checked. Permanent-denial and older-Android decisions have unit tests.
- Google test banner rendering was observed on the actual MainActivity screen. A Google test rewarded video was completed in Settings, and the app displayed the earned 15-minute focus break. Production ad fill/revenue is not verified by debug test inventory.

## Remaining limits and follow-up work

- **Content filtering:** bundled question records have no topic or difficulty metadata. Topic selection currently draws from the subject bank; strict topic-specific and offline-difficulty filtering is not implemented. The picker/navigation tests must not be read as content-filtering tests.
- Several newer exam subject banks contain eight questions, so their offline quizzes can have fewer than ten questions and their mocks fewer than thirty.
- Real Play Store update installation needs a Play-installed build and an available update. The emulator debug build cannot prove that path.
- AdMob approval and configured inventory were subsequently verified in the account (see below). Production fill/earnings, Google privacy forms in every region and Firebase console delivery remain unverified.
- Daily clock-time notifications were tested through scheduling logic and delivery/control tests; this review did not wait for every 10 AM/8 PM occurrence or test every manufacturer's battery restrictions/reboot behavior.
- This is a broad feature pass on one emulator, not exhaustive coverage of all Android versions, screen sizes, accessibility settings or concurrent user actions.

## Multi-language completion — November 3, 2026

Completed Claude's language handoff: added Hindi, Bengali, Marathi, Telugu, Tamil, Urdu, Kannada, Malayalam and Gujarati resource files. Each covers all 375 translatable English strings and eight plural resources. Removed unused extracted strings and moved developer animation labels out of translation resources. Corrected literal-percent formatting, bounded navigation labels after visual review, isolated the Urdu progress ratio, removed duplicate English navigation-icon descriptions, mirrored navigation chevrons, restored system locale handling on older Android, localized all widget text and refreshed notification channel names. The bundled privacy policy now matches the current root policy, including language preferences and language-change analytics.

Final checks:

- `testDebugUnitTest`: **62 passed**, including resource coverage, duplicate names, placeholder parity, plural fallback, format validity and locale-configuration consistency.
- Debug app and Android-test APKs: **build passed**.
- `lintDebug`: **passed with warnings**.
- Pixel 8 / API 35 instrumentation: **21 passed** — two new language tests, 16 existing core feature tests and three reminder/migration tests. The picker was exercised in Hindi, Tamil, Urdu and English with activity recreation and return to system default; compiled resources/plurals were checked in all ten languages.
- Visually inspected Hindi, Tamil and Urdu Home screens. Confirmed Tamil navigation labels fit and Urdu layout mirrors with a correctly ordered `0 / 10` ratio. Screenshots are in `app/build/reports/verification/language-*.png`.
- The core suite's offline-search case requires networking disabled, and its reminder editor case requires notification permission. An initial run without those prerequisites failed those two cases. The final 21-test run supplied both prerequisites and passed; prior network and permission settings were restored. The final direct instrumentation output is at `app/build/reports/verification/languages-and-core-tests.txt`. Earlier Gradle connected-test reports reflect the initial setup failures.

Translations are development-time machine drafts with context corrections; native-speaker review of all nine remains a release-quality follow-up. Questions, content names, fetched study content and information/legal pages remain English. Android 8–12 language switching was reviewed in code but not device-tested in this pass. See `LOCALIZATION.md` for maintenance instructions and scope. No store listing or store screenshots were published.

## Privacy policy and welcome tour refresh — November 3, 2026

Updated both policy copies for locally derived flashcards and insights, optional exam dates, language preferences, widget visibility, CSV contents and shared-copy retention. Clarified that topic-search text and event parameters can reach Firebase analytics/diagnostic breadcrumbs, and that ad privacy controls do not provide a separate Firebase opt-out. Public provider references remain linked in the policy. The source-information screen now agrees with those disclosures instead of implying that no activity leaves the device. Policy section labels render in bold rather than displaying Markdown markers.

The welcome tour now has five pages: exam practice/offline use; daily goals, countdowns, reminders and the widget; revision, flashcards and notes; progress, history and sharing; and language/appearance/privacy choices. A working Privacy Policy link opens the in-app policy and returns to the same onboarding page. Feature explanations are available in all ten supported UI languages. Long header actions are bounded so translated text cannot force the app name into multiple lines.

Validation: debug app and instrumentation APK builds passed; all 62 JVM tests passed; lint passed with warnings. Eight targeted emulator test executions passed: the complete tour in English, Hindi, Tamil and Urdu; both language resource/switching tests; and existing welcome-tour and information-screen regression tests. Screenshots and test output are saved under `app/build/reports/verification/welcome/`. App language, network and notification permission were restored after testing. Translation drafts still need native-speaker review.

## AdMob account and inventory verification — November 3, 2026

Verified in Chrome while signed into the requested `raiyanshahid1996@gmail.com` account. The ExamAI Android entry is linked to Google Play package `com.ai.studyassistant`; AdMob reports **Ready** and **Ad serving enabled**. Its app ID is `ca-app-pub-6654896157338132~5721449122`, matching `AndroidManifest.xml`.

All three active units match the release IDs in `AdIds.kt` and the corresponding SDK formats:

| Format | Ad unit ID |
| --- | --- |
| Banner | `ca-app-pub-6654896157338132/7958544314` |
| Interstitial | `ca-app-pub-6654896157338132/3591657969` |
| Rewarded | `ca-app-pub-6654896157338132/2903876760` |

The rewarded unit's advanced ad-type settings have Video, Ad pods and Interactive enabled. Inspected without saving changes. The implementation uses standard rewarded ads for both note-search credits and focus breaks, so no additional format/unit is needed. No units were created and no production IDs were changed. Debug builds retain Google's test unit IDs. This account check does not prove production fill, impressions, reward delivery or revenue on a released device.
