# CREAZZY SPACE

CREAZZY SPACE combines a responsive Arabic web dashboard with an early native Android app-management prototype.

## Components
- Web dashboard: manage workspaces, categories, links, JSON backups, CSV exports, quick templates, and a local activity log.
- Native Android prototype: view/search/favorite launchable apps, import an APK, inspect package/version/target SDK/requested permissions/native ABIs, show heuristic compatibility warnings, and hand it to Android's official package installer.
- Android build workflow: GitHub Actions builds a debug APK artifact when Android source changes or when manually triggered.

## Start the web dashboard in Termux
```sh
pkg update -y
pkg install nodejs git -y
git clone https://github.com/c83491307-gif/Crazy-ai.git
cd Crazy-ai
npm start
```
Open http://127.0.0.1:8787 in your phone browser.

## Native Android build
See [ANDROID-NATIVE-ROADMAP.md](ANDROID-NATIVE-ROADMAP.md) and [CLONING-ENGINE-RESEARCH.md](CLONING-ENGINE-RESEARCH.md) for researched failure modes, architecture choices, mitigations, and release gates. The source is in the android directory. A successful GitHub Actions run publishes an artifact named `creazzy-space-debug-apk`.

## Important limitations
This project does **not** clone Android applications, isolate app-private data, create virtual Android devices, or provision Google Play Services per clone. Those features require a separate, compatible Android virtualization/isolation architecture and cannot be supplied by a web dashboard. APK installation remains subject to Android's security checks and device policy. Install APKs only from trusted sources.

The native app is an early prototype and has not yet been device-tested; a debug APK is not a production or Play Store release.
