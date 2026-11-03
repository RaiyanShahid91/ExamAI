# ExamAI - Smart Study Assistant 🎓

![Android](https://img.shields.io/badge/Platform-Android-green)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple)
![Free](https://img.shields.io/badge/Cost-100%25%20Free-brightgreen)
![API](https://img.shields.io/badge/API-26%2B-blue)

A completely FREE Android app for Indian students preparing for 
JEE, NEET, and UPSC exams. No subscriptions, no login, 
no API keys — just open and start studying!

---

## 📸 Screenshots

<p align="center">
<img width="200" height="433" alt="Screenshot_20260622_191411" src="https://github.com/user-attachments/assets/81b26611-9833-416e-8c34-1c670d74a78b" />
<img width="200" height="433" alt="Screenshot_20260622_191358" src="https://github.com/user-attachments/assets/33cfd666-59f1-4e09-84af-f2adc5a65269" />
<img width="200" height="433" alt="Screenshot_20260622_191346" src="https://github.com/user-attachments/assets/d272b674-72a3-4be0-8a6e-f9a1961e29dc" />
<img width="200" height="433" alt="Screenshot_20260622_191337" src="https://github.com/user-attachments/assets/833f2a64-3b8e-4f86-83d7-4ba6383b0f36" />
<img width="200" height="433" alt="Screenshot_20260622_191306" src="https://github.com/user-attachments/assets/f74ca735-13ae-4051-9b50-f2299a9dcd74" />
</p>



---

## ✨ Features

- **Indian Languages** — Use English, Hindi, Bengali, Marathi, Telugu, Tamil, Urdu, Kannada, Malayalam or Gujarati. Choose a language in Settings or the welcome tour; Urdu supports right-to-left layout. Questions, subject/topic names and information/legal pages remain in English. See [localization notes](LOCALIZATION.md).
- **Personalized Appearance** — Choose Ocean, Violet, Forest, Rose or Amber with light, dark or system mode. Change your look in Settings or the refreshed welcome tour; preferences are saved locally.
- **Daily Study Planner** — Set your exam and a daily goal of 5–50 questions from the home screen. Get a topic recommendation based on earlier quiz results, open due reviews, and track progress automatically from completed quizzes. Goals are saved locally and progress resets at local midnight.

- 🎯 **3 Exams Covered** — JEE, NEET, UPSC
- 📚 **200+ MCQ Questions** — Curated real exam-level questions
- 🌐 **Live Questions** — Fetches fresh questions from Open Trivia DB
- 📖 **Study Notes** — Topic summaries powered by Wikipedia
- 📴 **Offline Mode** — Works without internet after first use
- 🌙 **Dark Mode** — Easy on eyes during night study
- ⚡ **No Login Required** — Just install and start
- 💰 **Completely Free** — No hidden charges ever

---

## 📚 Subjects Covered

### 🔬 JEE (Engineering)
- Physics (40 Questions)
- Chemistry (40 Questions)  
- Mathematics (40 Questions)

### 🧬 NEET (Medical)
- Biology (40 Questions)
- Physics (20 Questions)
- Chemistry (20 Questions)

### 🏛️ UPSC (Civil Services)
- History (30 Questions)
- Polity (30 Questions)
- Geography (30 Questions)
- Economy (coming soon)

---

## 🛠️ Built With

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Jetpack Compose | Modern UI toolkit |
| Material 3 | Design system |
| MVVM Architecture | Code structure |
| Retrofit + OkHttp | Network calls |
| Room Database | Offline storage |
| Open Trivia DB API | Free live MCQ questions |
| Wikipedia REST API | Topic explanations |
| Jsoup | Web content parsing |
| Coroutines + StateFlow | Async operations |
| Claude Code (AI) | Used to build this app |

---

## 🚀 How to Run Locally

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+
- Internet connection for first launch

### Steps

1. Clone the repository
   git clone https://github.com/yourusername/ExamAI.git

2. Open in Android Studio
   File → Open → Select the ExamAI folder

3. Sync Gradle
   Click "Sync Now" when prompted

4. Run the app
   Click the green Run button or press Shift+F10

5. That's it! No API keys needed.

---

## 📡 APIs Used (All Free)

| API | Usage | Cost |
|---|---|---|
| Open Trivia DB | Live MCQ questions | Free forever |
| Wikipedia REST API | Topic summaries | Free forever |
| NCERT Website | Study content | Free forever |

---

## 🏗️ Project Structure

ExamAI/
├── app/src/main/
│   ├── java/com/examai/
│   │   ├── MainActivity.kt
│   │   ├── navigation/
│   │   │   └── NavGraph.kt
│   │   ├── ui/screens/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── SubjectScreen.kt
│   │   │   ├── TopicScreen.kt
│   │   │   ├── QuizScreen.kt
│   │   │   ├── ResultScreen.kt
│   │   │   └── StudyNotesScreen.kt
│   │   ├── ui/theme/
│   │   │   ├── Theme.kt
│   │   │   └── Color.kt
│   │   ├── data/
│   │   │   ├── model/
│   │   │   ├── api/
│   │   │   ├── local/
│   │   │   └── repository/
│   │   └── viewmodel/
│   └── assets/
│       └── questions.json
└── README.md

---

## 🤝 Contributing

Contributions are welcome! Here's how:

1. Fork the project
2. Create your feature branch
   git checkout -b feature/AmazingFeature
3. Commit your changes
   git commit -m 'Add some AmazingFeature'
4. Push to the branch
   git push origin feature/AmazingFeature
5. Open a Pull Request

---

## 📋 Roadmap

- [ ] Add GATE exam support
- [x] Hindi and eight additional Indian UI languages
- [ ] Video links for each topic
- [ ] Leaderboard between friends
- [ ] Daily streak system
- [ ] Performance analytics

---

## 👨‍💻 Built By

**Raiyan Shahid**  
**contact@brynflow.com**

---

## 📄 License

This project is licensed under the MIT License.
Free to use, modify, and distribute.

---

## Learn on Brynflow

Continue your exam preparation at [Brynflow Exam Prep](https://brynflow.com/exams). You can also open it from Home, Study Notes, exam subjects, Settings, and About in the app.

For support, email [contact@brynflow.com](mailto:contact@brynflow.com).

## ⭐ Support

If this app helped you, please give it a ⭐ on GitHub!
It motivates me to add more features.

## Advertising placements

- Browsing screens retain banner ads; quiz completion may show an interstitial, with at least three minutes between full-screen ad dismissals and the next interstitial.
- Home, Results and Settings offer an optional rewarded ad for a 15-minute break from banners and interstitials. The reward persists across restarts until its expiry.
- Notes offers an explicit rewarded-ad button for two additional searches. Two free searches replenish daily; unused earned searches persist. Failed searches consume no credit, and saved notes are always accessible.
- Ads require the UMP consent SDK to allow requests. Settings exposes ad privacy choices when required.
- Debug builds use Google test ad units. Release builds retain the configured production units. No new AdMob console units were created; rewarded placements reuse the existing rewarded inventory.
- Firebase events record ad impressions/rewards by placement and SDK paid events (`ad_revenue`, including currency and value in micros). Actual earnings and serving approval must be checked in AdMob.

## Study reminders

Open the calendar icon on Home, or Settings → My reminders.

- Daily study reminders are off until the user explicitly enables them. They target 10 AM and 8 PM in the device's local timezone. They suggest an unfinished quiz, due reviews or a recently practiced topic, and skip nudges while the app is in use. A delayed daily reminder is suppressed outside its morning/evening window.
- Personal reminders have a title, optional note and a one-time date/time. Users can edit, delete or schedule them again. Titles and notes stay in the local Room database.
- Notification taps open the relevant study screen or personal reminder list. Notifications have separate daily/personal channels and private lock-screen content.
- Schedules restore after restart, app updates and clock/timezone changes. The Room 4→5 migration preserves existing study data.
- Notification permission is required. Android may delay daily reminders; optional Alarms & reminders access enables precise personal reminders. Notifications cannot bypass Android force-stop or manufacturer restrictions.

## Notification policy review (November 1, 2026)

- Notification permission is requested on the first app opening and retried on later openings when denied. Android may stop displaying the runtime dialog after repeated denials; a dismissible settings explanation handles that case. Requests are limited to one per app-opening session, and returning from notification settings does not immediately prompt again. Enabling notification access does not opt the user into daily nudges.
- Daily nudges require a fresh opt-in to the current twice-daily schedule. They are capped at the morning/evening slots, have an immediate notification opt-out action, and contain no ads or website promotions.
- Daily scheduling remains inexact. Only user-created personal reminders may use the user-granted SCHEDULE_EXACT_ALARM special access. No USE_EXACT_ALARM or USE_FULL_SCREEN_INTENT permission is requested.
- The in-app Privacy policy reads `app/src/main/assets/privacy_policy.md`, which must stay identical to `PRIVACY_POLICY.md`.
- Before release, publish this policy at the public privacy-policy URL configured in Play Console. Reconcile Data safety with AdMob, Firebase Analytics/Crashlytics and the actual SDK configuration; confirm target-audience/Families declarations. These external console/hosting settings were not changed by this code update, and store approval is not guaranteed.

References reviewed:
https://developer.android.com/develop/ui/compose/notifications/notification-permission
https://developer.android.com/develop/background-work/services/alarms
https://support.google.com/googleplay/android-developer/answer/16558241
https://support.google.com/googleplay/android-developer/answer/10144311
