# Vincera
 
A native Android workout tracking app built with **Kotlin** and **Firebase**.
 
## Tech Stack
 
| Layer | Technology |
|---|---|
| Language | Kotlin |
| Auth | Firebase Authentication |
| Database | Cloud Firestore |
| Platform | Android |
 
## Features
 
- **User authentication** — secure sign-up and login via Firebase Authentication
- **Workout logging** — log exercises, sets, reps and weights per session
- **Training plans** — create and manage personalised workout plans
- **Progress tracking** — view training history and performance over time
- **Cloud sync** — all data stored in Cloud Firestore, accessible across devices
 
## Project Structure
 
```
Vincera/
├── app/
│   ├── src/main/java/com/vincera/
│   │   ├── auth/           # Firebase Authentication logic
│   │   ├── ui/             # Activities & Fragments
│   │   ├── data/           # Firestore repository layer
│   │   └── model/          # Data classes
│   └── res/                # Layouts, drawables, strings
└── google-services.json    # Firebase config (not committed)
```
 
## Setup
 
1. Clone the repository
2. Add your own `google-services.json` from the Firebase console
3. Open in Android Studio and run on a device or emulator
> **Note:** `google-services.json` is excluded from this repository. You need your own Firebase project to run the app.
 
## Context
 
Developed as a student project at Hochschule für Wirtschaft und Recht Berlin (WiSe 2025/26).
