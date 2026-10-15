# CREAZZY SPACE — Android native phase

## Added in this phase
- Native Android app shell using Kotlin and the Android SDK.
- Installed launchable app inventory with live search and launch actions.
- Persistent app favorites and a favorites-only filter, stored in private app preferences.
- APK document picker, package metadata inspection, and handoff to the official Android package installer.
- FileProvider shares APKs from the app cache using scoped content URIs.
- GitHub Actions workflow builds a debug APK and uploads it as an artifact.

## Important boundaries
This is **not** an app-cloning or virtualization engine. It does not duplicate app private data, create parallel Android user profiles, intercept other applications, or supply Google Play Services. Those capabilities require supported Android platform APIs, device/OEM support, and compatibility and policy review. APK installation can be blocked by device policy, Android version, unknown-source settings, signature conflicts, split APK packaging, or Play Protect. Install only APKs from sources you trust.

The APK picker uses Android's document picker rather than broad storage access. Android itself handles installation confirmation and package verification.

## Build
Open the android directory in Android Studio or use a compatible Gradle installation with JDK 17:

    cd android
    gradle assembleDebug

Output: android/app/build/outputs/apk/debug/app-debug.apk.

GitHub Actions builds this APK on pushes touching android/** and on manual workflow dispatch. Download the artifact from the workflow run page; a debug APK is not a Play Store release.
