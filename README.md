# Daily Doodle Chain

A lightweight social creativity app where each user contributes exactly one panel (a single-image doodle) to an evolving chain-story started by a short prompt. Chains are shareable comic strips that surface creativity, low-friction collaboration, and daily habit formation.

## Features

### Core Features
- **Frictionless Sign-In**: Firebase Auth with email and Google sign-in
- **Create & Join Chains**: View seed prompts, create chains, or add panels to open chains
- **Drawing Canvas**: Minimal drawing interface with brush, color palette, undo, clear, and save draft
- **Feed**: Browse recent, popular, and featured chains with pagination
- **Chain Viewer**: Swipe through panels in a chain
- **Sharing**: Share completed chains as images

### Navigation & Organization
- **Bottom Navigation Bar**: Shkiper-style animated bottom navigation with sliding circle indicator
- **Favorites**: Star your favorite chains for quick access
- **Trash/Recycle Bin**: Soft delete with 30-day retention and restore capability
- **Settings**: Customizable app preferences and About section

### Onboarding
- **Beautiful Onboarding Flow**: Three-screen onboarding with Lottie animations
- **Welcome, Create & Share, Join the Community**: Engaging introduction to app features

### Monetization & Analytics
- **AdMob Integration**: Rewarded ads (hint unlock), interstitial ads, and banner/native ads
- **Moderation**: Report button per panel and basic profanity filtering
- **Analytics**: Firebase Analytics tracking for key events

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Backend**: Firebase (Auth, Firestore, Storage, Analytics, Crashlytics)
- **Image Loading**: Coil
- **Animations**: Lottie for onboarding animations
- **Navigation**: Navigation Compose with custom animated bottom bar
- **Dependency Injection**: Hilt

## Setup Instructions

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 24+ (minSdk: 24, targetSdk: 36)
- Firebase project with the following services enabled:
  - Authentication (Email/Password and Google Sign-In)
  - Cloud Firestore
  - Cloud Storage
  - Analytics
  - Crashlytics
- AdMob account with ad unit IDs

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd DailyDoodle
   ```

2. Add your Firebase configuration:
   - Download `google-services.json` from your Firebase console
   - Place it in `app/google-services.json`
   - Update the package name in Firebase if needed

3. Configure AdMob:
   - Update `app/src/main/res/values/strings.xml` with your AdMob App ID
   - Update ad unit IDs in `app/src/main/java/com/example/dailydoodle/ui/admob/AdMobManager.kt`

4. Build and run:
   ```bash
   ./gradlew build
   ```

## Project Structure

```
app/src/main/java/com/example/dailydoodle/
├── data/
│   ├── model/          # Data models (User, Chain, Panel, DeletedChain, etc.)
│   └── repository/     # Repository classes (ChainRepository, TrashRepository, FavoritesRepository)
├── di/                 # Dependency injection (Hilt modules)
├── navigation/         # Navigation setup (NavGraph, Routes)
├── ui/
│   ├── components/     # Reusable UI components
│   │   └── navigation/ # Bottom navigation bar components
│   ├── screen/         # UI screens
│   │   ├── auth/       # Authentication screens
│   │   ├── canvas/     # Drawing canvas
│   │   ├── chain/      # Chain viewer
│   │   ├── favorites/  # Favorites screen
│   │   ├── feed/       # Main feed
│   │   ├── onboarding/ # Onboarding flow
│   │   ├── settings/   # Settings & About
│   │   └── trash/      # Trash/Recycle bin
│   ├── viewmodel/      # ViewModels
│   └── theme/          # App theme (Material 3)
├── util/               # Utility classes (Analytics, etc.)
└── admob/              # AdMob integration
```

## Firebase Security Rules

### Firestore Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Chains collection
    match /chains/{chainId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null;
      
      // Panels subcollection
      match /panels/{panelId} {
        allow read: if request.auth != null;
        allow create: if request.auth != null;
        allow update, delete: if false; // Only admins can modify
      }
    }
    
    // Moderation reports
    match /moderation_reports/{reportId} {
      allow create: if request.auth != null;
      allow read, update: if false; // Only admins
    }
  }
}
```

### Storage Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /panels/{chainId}/{imageId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
        && request.resource.size < 2 * 1024 * 1024 // 2MB limit
        && request.resource.contentType.matches('image/.*');
    }
  }
}
```

## Building Release APK

1. Generate a keystore:
   ```bash
   keytool -genkey -v -keystore daily-doodle-release.keystore -alias daily-doodle -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Create `keystore.properties` in the project root:
   ```properties
   storePassword=your-store-password
   keyPassword=your-key-password
   keyAlias=daily-doodle
   storeFile=daily-doodle-release.keystore
   ```

3. Update `app/build.gradle.kts` with signing config (see Android documentation)

4. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

## Analytics Events

The app tracks the following events via Firebase Analytics:

- `sign_up` - User signs up
- `sign_in` - User signs in
- `feed_open` - User opens feed
- `chain_opened` - User opens a chain
- `panel_added` - User adds a panel
- `panel_shared` - User shares a chain
- `rewarded_ad_view` - User watches a rewarded ad
- `interstitial_ad_shown` - Interstitial ad is shown
- `purchase_made` - User makes an in-app purchase
- `report_submitted` - User submits a moderation report

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Author

**Made by Darshan K** 💜

- 📧 Email: [rushdarshan@gmail.com](mailto:rushdarshan@gmail.com)
- 🐙 GitHub: [github.com/1511Darshan/daily-doodle](https://github.com/1511Darshan/daily-doodle)

## Contributing

This is a contest submission project. For contributions, please follow the Apache 2.0 license terms.

## Acknowledgments

- Firebase team for excellent backend services
- Jetpack Compose team for modern UI framework
- Lottie for beautiful animations
- AdMob for monetization platform
