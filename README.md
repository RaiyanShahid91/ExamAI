<img width="329" height="629" alt="Screenshot 2026-06-22 at 7 11 28 PM" src="https://github.com/user-attachments/assets/66a91cc3-1eb8-4b25-9758-2afb7deff279" />
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
<img width="1080" height="2400" alt="Screenshot_20260622_191411" src="https://github.com/user-attachments/assets/f7fc6721-a93d-4ca1-b03d-75bbcd6181c6" />
<img width="1080" height="2400" alt="Screenshot_20260622_191358" src="https://github.com/user-attachments/assets/fb34b9ac-c02c-4d53-bcad-c20bfcb407dd" />
<img width="1080" height="2400" alt="Screenshot_20260622_191346" src="https://github.com/user-attachments/assets/2d8faf1b-1618-4d86-a886-3a45a8af2a2d" />
<img width="1080" height="2400" alt="Screenshot_20260622_191337" src="https://github.com/user-attachments/assets/e87131d0-9d6b-4db0-abee-6bcbae6f442d" />
<img width="1080" height="2400" alt="Screenshot_20260622_191306" src="https://github.com/user-attachments/assets/546a624e-c280-43ff-bad0-d9bf8c67bf99" />


---

## ✨ Features

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
- [ ] Hindi language support
- [ ] Video links for each topic
- [ ] Leaderboard between friends
- [ ] Daily streak system
- [ ] Performance analytics

---

## 👨‍💻 Built By

**Raiyan Shahid**  
**raiyanshahid41@gmail.com**

---

## 📄 License

This project is licensed under the MIT License.
Free to use, modify, and distribute.

---

## ⭐ Support

If this app helped you, please give it a ⭐ on GitHub!
It motivates me to add more features.
