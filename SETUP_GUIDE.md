# NH BOX — Setup & Deployment Guide

## 1. Replace `google-services.json`
1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Create or select your project, add an Android app with package name **`com.nhbox.app`**, and download `google-services.json`.
3. Replace `app/google-services.json` (currently a buildable template) with your downloaded `google-services.json`.
4. Enable **Firebase Authentication** (Email/Password provider) and **Firebase Realtime Database** (in the same Firebase project).

## 2. Configure TMDB API Key
1. Copy `local.properties.example` to `local.properties` (Android Studio generates `local.properties` automatically with `sdk.dir`).
2. Add your TMDB v3 API key to `local.properties`:
   ```properties
   TMDB_API_KEY=your_actual_tmdb_v3_api_key
   ```
3. Never commit `local.properties` or `google-services.json` to a public repository (both are listed in `.gitignore`).

## 3. Deploy Firebase Realtime Database Security Rules
1. In Firebase Console → **Realtime Database → Rules**, paste the contents of `firebase/database.rules.json` and click **Publish**.
2. Notice:
   - Firebase Storage and Cloud Firestore are **not used** anywhere in NH BOX.
   - All application-managed images are compressed to WebP/PNG (`100–200 KB`), Base64-encoded, and stored under `/images/{imageId}` in RTDB.

## 4. Safely Assign Your First Admin Account
1. Register a normal account inside the **NH BOX** app (this creates `/users/{uid}` with `role: "user"` and `accountStatus: "active"`).
2. Open Firebase Console → **Realtime Database → Data** → `/users/{your_uid}`.
3. Manually change `role` from `"user"` to `"admin"`.
4. Re-open **Settings** (or the Home drawer) in NH BOX — the **ADMIN** button will now appear, and Firebase RTDB Security Rules will authorize admin writes for your UID.

## 5. Build & Run in Android Studio
1. Open `NH_BOX_Android_Studio` in **Android Studio (Ladybug / Meerkat or newer)** with **JDK 17**.
2. Let Gradle sync (`libs.versions.toml` handles all Compose, Hilt, Firebase, Media3, Coil, and Retrofit dependencies).
3. Run on a real Android device or emulator (`minSdk 24`, `targetSdk 35`).
