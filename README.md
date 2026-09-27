# Expense Tracker

A full-stack expense tracking app built as a system design learning project. Android client backed by a Kotlin server deployed on the cloud.

## Demo

<!-- Drag and drop your demo video here on GitHub's web editor -->

## Features

- **Expense Management** — Add, edit, delete expenses with category tagging and date tracking
- **Receipt Attachments** — Upload receipt images via Cloudinary
- **Expense Splitting** — Split expenses with other users (equal, custom amounts)
- **Balance Tracking** — Real-time balance calculation across all shared expenses
- **Push Notifications** — Firebase-powered notifications when someone adds a split
- **JWT Authentication** — Secure login/registration with encrypted token storage

## Architecture

```
┌─────────────────┐         ┌─────────────────┐        ┌──────────────┐
│  Android App    │  HTTPS  │  Ktor Server    │  JDBC  │  PostgreSQL  │
│  Jetpack Compose├────────►│  (Render)       ├───────►│  (Neon)      │
└────────┬────────┘         └────────┬────────┘        └──────────────┘
         │                           │
         │                           │
    ┌────▼────┐                ┌─────▼──────┐
    │Cloudinary│               │  Firebase   │
    │ (Images) │               │  (FCM Push) │
    └─────────┘                └────────────┘
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Android** | Kotlin, Jetpack Compose, Material 3 |
| **Server** | Kotlin, Ktor, Exposed ORM |
| **Database** | PostgreSQL (Neon) / SQLite (local dev) |
| **Auth** | JWT (JSON Web Tokens) |
| **Images** | Cloudinary (unsigned upload) |
| **Notifications** | Firebase Cloud Messaging |
| **Deployment** | Render (Docker) |

## Project Structure

```
expense-tracker/
├── server/           # Ktor backend
│   ├── models/       # Database tables (Exposed ORM)
│   ├── routes/       # API endpoints
│   ├── plugins/      # JWT auth, serialization
│   ├── services/     # Push notification service
│   └── Dockerfile    # Render deployment
│
└── android/          # Android client
    └── app/src/main/kotlin/com/expensetracker/
        ├── ui/           # Compose screens
        ├── viewmodel/    # ViewModels
        ├── network/      # API client, Cloudinary uploader
        └── notifications/# FCM service
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Create account |
| POST | `/auth/login` | Login, returns JWT |
| GET | `/expenses` | List user's expenses |
| POST | `/expenses` | Create expense |
| PUT | `/expenses/{id}` | Update expense |
| DELETE | `/expenses/{id}` | Delete expense |
| GET | `/split-expenses` | List splits involving user |
| POST | `/split-expenses` | Create split expense |
| PUT | `/split-expenses/{id}/settle/{userId}` | Mark participant as settled |
| GET | `/split-expenses/balances` | Net balances with other users |
| GET | `/users` | List all users (for splitting) |
| POST | `/device/register` | Register FCM token |

## Setup

### Prerequisites

- Android Studio (Hedgehog or later)
- JDK 17
- A Firebase project ([console.firebase.google.com](https://console.firebase.google.com))
- A Cloudinary account ([cloudinary.com](https://cloudinary.com)) — free tier
- A Neon account ([console.neon.tech](https://console.neon.tech)) — free tier (for production)
- A Render account ([render.com](https://render.com)) — free tier (for production)

### 1. Clone the repo

```bash
git clone https://github.com/Himanshu-0103Sharma/expense-tracker.git
cd expense-tracker
```

### 2. Set up Firebase

1. Go to [Firebase Console](https://console.firebase.google.com) → Create a new project
2. Add an Android app with package name `com.expensetracker`
3. Download `google-services.json` → place it in `android/app/`
4. Go to Project Settings → Service Accounts → Generate new private key
5. Save the downloaded JSON as `server/firebase-service-account.json`

### 3. Set up Cloudinary

1. Sign up at [cloudinary.com](https://cloudinary.com)
2. Go to Dashboard → copy your **Cloud Name**
3. Go to Settings → Upload → Add an **unsigned upload preset** → copy the preset name

### 4. Run the server locally

```bash
cd server
./gradlew run
```

Server starts on `http://localhost:8080` with a local SQLite database. No additional setup needed.

### 5. Run the Android app

1. Create `android/local.properties` (if it doesn't exist) and add:
   ```properties
   API_BASE_URL=http://10.0.2.2:8080
   CLOUDINARY_CLOUD=your_cloud_name
   CLOUDINARY_PRESET=your_upload_preset
   ```
2. Open the `android/` folder in Android Studio
3. Sync Gradle and run on an emulator or device

### 6. Production deployment (optional)

#### Server on Render

1. Create a new **Web Service** on [Render](https://render.com)
2. Connect your GitHub repo and set the root directory to `server/`
3. Set environment to **Docker**
4. Add these environment variables:
   - `DATABASE_URL` — your Neon PostgreSQL connection string (JDBC format)
   - `JWT_SECRET` — run `openssl rand -hex 32` to generate one
   - `FIREBASE_CREDENTIALS` — paste the entire contents of `firebase-service-account.json`
   - `PORT` — `8080`

#### Database on Neon

1. Create a project at [console.neon.tech](https://console.neon.tech)
2. Copy the JDBC connection string from Connection Details
3. Paste it as the `DATABASE_URL` env var on Render

#### Point the Android app to production

Update `android/local.properties`:
```properties
API_BASE_URL=https://your-app.onrender.com
```

Tables are created automatically on first server startup.

## License

MIT
