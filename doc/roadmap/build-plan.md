# Build Plan — Platform Packaging & Release

> Platform builds, CI/CD pipeline, performance optimization, accessibility, security, launch.
> **Prerequisites**: Phase 8 (Module Review) complete; all modules verified and data-clean.

---

## Android Build & Packaging

- [ ] Configure `build.gradle.kts` for release signing: keystore, key alias, passwords via environment variables
- [ ] Enable R8 (ProGuard) code shrinking and obfuscation for release builds
- [ ] Configure resource shrinking to remove unused drawables/strings
- [ ] Set `minSdk = 26`, `targetSdk = 35`, `compileSdk = 35`
- [ ] Generate Android App Bundle (`.aab`) with `bundleRelease` task
- [ ] Verify APK size < 30 MB (base module, excluding downloadable bible data)
- [ ] Test on emulators: Pixel 4 API 26 (min), Pixel 8 API 35 (target), Tablet API 34
- [ ] Configure `AndroidManifest.xml`: permissions, intent filters, deep links
- [ ] Write instrumented UI test suite (Compose UI Test on device)
- [ ] Prepare Google Play Store listing: screenshots (phone + tablet), description, feature graphic

## iOS Build & Packaging

- [ ] Configure Xcode project via KMP `iosArm64` + `iosSimulatorArm64` targets
- [ ] Generate `linkReleaseFrameworkIosArm64` shared framework
- [ ] Create SwiftUI `App` entry point embedding Compose Multiplatform `UIViewController`
- [ ] Configure code signing: provisioning profiles, entitlements, team ID
- [ ] Set deployment target: iOS 16.0 minimum
- [ ] Configure `Info.plist`: permissions, URL schemes, deep links
- [ ] Test on simulators: iPhone 15, iPhone SE 3, iPad Air
- [ ] Verify app size < 40 MB (framework + resources)
- [ ] Write XCTest UI test suite for critical flows
- [ ] Prepare App Store listing: screenshots (iPhone + iPad), description, preview video

## Desktop Builds (Windows, macOS, Linux)

- [ ] Configure `compose.desktop` DSL in `build.gradle.kts`
- [ ] Windows: generate MSI installer with `packageMsi` task
- [ ] Windows: configure app icon (.ico), version info, publisher name
- [ ] Windows: test on Windows 10 and Windows 11
- [ ] macOS: generate DMG with `packageDmg` task
- [ ] macOS: configure app icon (.icns), `Info.plist`, code signing, notarization
- [ ] macOS: test on macOS 14 (Sonoma) and macOS 15 (Sequoia)
- [ ] Linux: generate DEB package with `packageDeb` task
- [ ] Linux: configure `.desktop` file, app icon, dependencies
- [ ] Linux: test on Ubuntu 22.04 and Fedora 39
- [ ] Verify desktop app cold start < 3 seconds
- [ ] Implement window state persistence: size, position, maximized/fullscreen

## CI/CD Pipeline (GitHub Actions)

- [ ] Create `.github/workflows/build.yml`: build all targets on push/PR
- [ ] Configure matrix strategy: `[android, ios, desktop-windows, desktop-macos, desktop-linux]`
- [ ] Android job: `assembleDebug` + `bundleRelease` + instrumented tests (Firebase Test Lab or emulator)
- [ ] iOS job: `linkDebugFrameworkIosSimulatorArm64` + XCTest on macOS runner
- [ ] Desktop job: `packageMsi` (Windows runner), `packageDmg` (macOS runner), `packageDeb` (Ubuntu runner)
- [ ] Add `detekt` static analysis step (fail on issues)
- [ ] Add `ktlint` formatting check step (fail on violations)
- [ ] Add `Kover` code coverage step with minimum 80% gate
- [ ] Add dependency vulnerability scan (Gradle dependency check or Snyk)
- [ ] Create `.github/workflows/release.yml`: tag-triggered release pipeline
- [ ] Upload Android AAB to Google Play (internal track) via Gradle Play Publisher
- [ ] Upload iOS build to App Store Connect via Fastlane
- [ ] Upload desktop installers to GitHub Releases
- [ ] Add build status badges to README.md

## Performance Optimization

- [ ] Profile app startup time on all platforms — target < 2s (mobile), < 3s (desktop)
- [ ] Optimize SQLDelight queries: add missing indexes, analyze `EXPLAIN QUERY PLAN`
- [ ] Profile and optimize FTS5 search latency — target < 200ms for typical queries
- [ ] Optimize Compose recomposition: ensure `@Stable` / `@Immutable` annotations on state classes
- [ ] Profile memory usage: target < 150 MB RSS on mobile with 3 panes open
- [ ] Implement lazy loading for large data sets (verse lists, search results, morphology)
- [ ] Optimize image/icon rendering: vector drawables, cached bitmaps
- [ ] Profile and optimize Knowledge Graph canvas rendering — target 60 fps with 200 nodes
- [ ] Implement database WAL mode for concurrent read/write performance
- [ ] Write benchmark tests for critical paths (chapter load, search, graph render)

## Accessibility

- [ ] Add `contentDescription` / `semantics` to all interactive elements
- [ ] Verify TalkBack (Android) navigation through Bible Reader, Search, and Notes
- [ ] Verify VoiceOver (iOS) navigation through critical flows
- [ ] Verify screen reader support on desktop (Narrator / NVDA on Windows, VoiceOver on macOS)
- [ ] Implement keyboard navigation for all desktop panes (Tab, Arrow keys, Enter, Escape)
- [ ] Implement keyboard shortcuts: `Ctrl+F` (Search), `Ctrl+G` (Go to verse), `Ctrl+N` (New note), `Ctrl+B` (Bookmarks)
- [ ] Verify minimum contrast ratio 4.5:1 (AA) for all text in both light and dark themes
- [ ] Verify touch target minimum 48dp on mobile
- [ ] Support dynamic type / system font scaling (up to 2x)
- [ ] Test with color blindness simulation (protanopia, deuteranopia)

## Security

- [ ] Audit all platform file access: restrict to app sandbox directories
- [ ] Validate all module import files: sanitize XML/USFM input, reject malformed data
- [ ] Implement SQLDelight parameterized queries everywhere (no string concatenation in SQL)
- [ ] Audit export functionality: no sensitive data leakage in JSON/CSV exports
- [ ] Enable Android network security config (cleartext traffic disabled)
- [ ] Enable iOS App Transport Security (ATS)
- [ ] Review ProGuard/R8 rules: ensure no sensitive class names exposed in stack traces
- [ ] Implement data-at-rest protection: user notes and highlights in encrypted DB (SQLCipher optional)
- [ ] Add SBOM (Software Bill of Materials) generation to CI pipeline
- [ ] Run OWASP dependency check and resolve critical/high vulnerabilities

## Launch Preparation

- [ ] Create app landing page / website with feature overview and download links
- [ ] Write user documentation: Getting Started guide, feature walkthroughs
- [ ] Create onboarding flow: first-launch tutorial (import modules, set preferences, explore panes)
- [ ] Record demo video showcasing key workflows (Bible reading, word study, sermon prep)
- [ ] Prepare changelog / release notes for v1.0.0
- [ ] Set up crash reporting: Sentry or Firebase Crashlytics (Android/iOS)
- [ ] Set up analytics (opt-in): anonymous usage metrics for feature prioritization
- [ ] Set up issue tracker: GitHub Issues with bug/feature templates
- [ ] Define support channels: GitHub Discussions, Discord/Telegram community
- [ ] Final QA pass: test all 22 modules on all 5 platforms
- [ ] Tag `v1.0.0` release and trigger release pipeline
- [ ] Publish to Google Play Store, Apple App Store, GitHub Releases

---

## Build Plan Exit Criteria

- [ ] Android AAB builds, signs, and installs correctly on API 26–35
- [ ] iOS framework links and runs on iPhone (iOS 16+) and iPad
- [ ] Desktop installers work on Windows 10/11, macOS 14/15, Ubuntu 22.04+
- [ ] CI/CD pipeline builds all targets, runs all tests, enforces quality gates
- [ ] Startup time < 2s (mobile), < 3s (desktop)
- [ ] FTS5 search < 200ms; chapter load < 100ms
- [ ] Accessibility: screen reader navigable, keyboard-operable, WCAG AA contrast
- [ ] Security audit passed with no critical/high vulnerabilities
- [ ] v1.0.0 published to all distribution channels
