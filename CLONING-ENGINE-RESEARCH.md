# CREAZZY SPACE — Cloning Engine Research & Engineering Plan

Date: 2026-10-16

## Product target

Build an Android-first app-instance manager that can import APKs, select an instance count, create clearly named instances, keep their state isolated where the selected engine supports it, and launch/manage each instance. A web dashboard can manage jobs and device records, but cannot itself clone Android packages or isolate Android app-private data.

## Research sources reviewed

- App Cloner official compatibility notes: https://appcloner.app/
- VirtualApp Android 11–16 known issues: https://github.com/ISEKHON/VirtualApp/blob/main/docs/KNOWN-ISSUES.md
- VirtualApp Android 14 fork and patch notes: https://github.com/f0restw0w/VirtualApp_16
- Shelter work-profile model and caveats: https://github.com/dahool/Shelter
- Island work-profile setup / installation constraints: https://island.oasisfeng.com/faq
- VirtualXposed support scope: https://github.com/android-hacker/VirtualXposed

## Main lessons from real projects

1. **Do not promise universal compatibility.** App Cloner itself documents failures around certificate-dependent Google login, Play Services, Play Games, Drive backup, billing, integrity checks, anti-tamper, and reCAPTCHA.
2. **Runtime virtualization is not a simple UI feature.** VirtualApp-style engines need package-manager and activity/service/provider virtualization, storage remapping, native ABI support, Binder/service compatibility, and Android-version-specific patches.
3. **Android 14+ is a major compatibility boundary.** ART internals, hidden API restrictions, background activity launch restrictions, and writable DEX restrictions can break older hook-based engines.
4. **Google services are a separate compatibility project.** Copying or pretending to provide GMS is not enough. FCM token relay, Maps, OAuth, billing, and Play Integrity have distinct requirements. Never bypass integrity or app security controls.
5. **Work profiles are a stronger OS-supported isolation path, but not unlimited cloning.** They generally provide a separate managed profile and one additional installation per profile; profile creation is system-mediated and OEM ROMs can conflict with other dual-app features.
6. **APK repackaging changes identity.** A changed package ID/signing certificate can invalidate backend allowlists, OAuth clients, Firebase configuration, signature checks, and licensing. Split APKs / app bundles need explicit handling; a single APK picker must not pretend to support every install set.
7. **Long-running builds must outlive the screen.** Store job state outside an Activity/ViewModel, report progress and failure reasons, and make jobs resumable. Avoid doing large ZIP/repack/sign operations on the UI thread.
8. **Compatibility must be evidence-driven.** Keep a matrix by Android API level, OEM/ROM, ABI, app type, and test result. Show warnings before installation, never silently weaken Play Protect or device security.

## Architecture decision

Use an engine abstraction instead of coupling UI to one technique:

- `CloneEngine`: capabilities, inspect, create instances, list instances, launch, remove, diagnostics.
- `VirtualizedEngine`: experimental; adapter for a pinned, license-reviewed VirtualApp fork. Must pass device/API tests before being the default.
- `ManagedProfileEngine`: OS-supported work-profile path; profile provisioning is user/system-mediated and device policy must be implemented correctly. Do not claim it can create unlimited profiles.
- `RepackEngine`: separate opt-in pipeline for APK transformation only where lawful and technically supported; must validate split APKs, preserve permissions, re-sign, verify output, and report identity changes. This does not create private-data isolation by itself.
- `InstanceRepository`: durable instance/job metadata, unique IDs, source hash, engine, status, last error, timestamps, and requested count.
- `CompatibilityAnalyzer`: APK metadata, requested permissions, ABI inventory, signing metadata when available, split-package detection, and heuristic warnings. Heuristics must be labeled as such, not as proof.

## Known issue → mitigation matrix

| Failure class | Detection / mitigation |
| --- | --- |
| Google login / GMS / Maps / FCM | Warn before cloning; separate compatibility tests; don't claim support without real-device evidence |
| Play Integrity / SafetyNet / certificate pinning | Mark unsupported where identity verification fails; do not bypass security checks |
| Android 14–16 ART / hidden APIs | Pin upstream commit; version-gate hooks; automated build matrix and physical-device smoke tests |
| ABI mismatch / native crash | Inspect `lib/<abi>/`; check device ABIs; test ARM64 and 32-bit separately |
| Split APK / missing config splits | Detect split metadata and request a complete install set; never treat a base APK as a complete bundle automatically |
| Signature collision on installation | Verify package/signature state and show exact installer error; do not silently uninstall the original |
| Data leakage between instances | Per-instance storage mapping, adversarial isolation tests, and explicit no-isolation status until verified |
| Process death during build | Durable queue, foreground service/WorkManager, checkpoints, and resumable jobs |
| ROM/OEM work-profile conflicts | Preflight profile availability and give recovery instructions; never auto-delete user profiles |
| Misleading clone count | Count only successfully created, launchable instances; distinguish requested, queued, installed, and verified |

## Current implementation status (honest)

- Native app inventory/search/open/favorites: implemented.
- APK import, metadata preflight, ABI inventory, and heuristic compatibility warnings: implemented in the current code.
- Official Android installer handoff: implemented.
- Multi-instance runtime engine: **not implemented yet**.
- Per-instance private-data isolation: **not implemented yet**.
- Multiple copies count UI backed by a real engine: **not implemented yet**.
- GMS compatibility certification: **not implemented**.
- GitHub Actions debug APK build: passed on commit `7591e3500c528c58eaa76426455171ed8a95f3fb`; artifact uploaded. This confirms compilation only, not runtime cloning or device compatibility.
- Device/API test matrix and physical-device validation: pending.

## Required release gates

1. Build the APK in GitHub Actions; archive build logs and the artifact.
2. Unit-test APK analyzer on valid, malformed, oversized, split, native-ABI, and permission-heavy fixtures.
3. Test API 26, 29, 30, 33, 34, 35, and 36 where device/emulator images exist.
4. Real-device smoke tests on at least one ARM64 AOSP-like device and one OEM-customized ROM.
5. Create at least two instances of a harmless test app; prove separate app-private storage, separate login state, notifications, and process restart behavior.
6. Test failure/recovery: app crash, low storage, permission denial, interrupted job, duplicate request, reboot, and uninstall cleanup.
7. Publish a compatibility report. A green build alone is not proof that cloning works.

## Security and user-data rules

- Never ask users to disable Play Protect or weaken Android security as a routine workaround.
- Do not silently install APKs, remove original apps, or change profiles.
- Treat imported APKs as untrusted; keep parsing isolated and report hashes.
- Do not collect APK contents or private app data remotely by default.
- Respect app licenses, service terms, and user authorization.
