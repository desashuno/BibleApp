# Getting Started

> BibleStudio — Environment Setup, First Build & Contribution Guide (Kotlin Multiplatform)

---

## 1. Prerequisites

### 1.1 Required

| Tool | Version | Verification |
|------|---------|-------------|
| **JDK** | 17+ (LTS) | `java -version` |
| **Kotlin** | 2.0+ (bundled via Gradle) | `./gradlew --version` |
| **Gradle** | 8.x (wrapper) | `./gradlew --version` |
| **Git** | 2.x+ | `git --version` |

### 1.2 IDE

| IDE | Recommended Plugins |
|-----|---------------------|
| **IntelliJ IDEA** (recommended) | Kotlin, Compose Multiplatform, SQLDelight |
| **Android Studio** (Ladybug+) | Kotlin Multiplatform, Compose Multiplatform |

> VS Code can be used for editing but lacks full KMP/Compose tooling. IntelliJ IDEA or Android Studio is strongly recommended.

### 1.3 Platform-Specific

| Platform | Requirements |
|----------|-------------|
| **Android** | Android SDK (API 24+), via Android Studio SDK Manager |
| **iOS** | macOS, Xcode 15+, CocoaPods or SPM |
| **Windows** | JDK 17+, WiX Toolset (for MSI packaging) or jpackage |
| **macOS** | JDK 17+, Xcode command-line tools |
| **Linux** | JDK 17+, libgtk-3-dev (for desktop window) |

---

## 2. Setup

### Step 1: Clone

```bash
git clone https://github.com/<org>/bible-studio.git
cd bible-studio
```

### Step 2: Sync & Build

```bash
# Download dependencies and compile the shared module
./gradlew build
```

No separate dependency install step is needed — Gradle resolves and downloads all dependencies automatically from Maven Central and Google's Maven repository on first build.

### Step 3: Verify Setup

```bash
# Run all checks (compile, detekt, tests)
./gradlew check
```

### Step 4: Run

```bash
# Desktop (JVM)
./gradlew :desktopApp:run

# Android (deploy to connected device / emulator)
./gradlew :androidApp:installDebug

# iOS (macOS only — opens Xcode project, or use Gradle)
./gradlew :iosApp:podInstall
# Then open iosApp/iosApp.xcworkspace in Xcode and run
```

### Step 5: Open in IDE

1. **IntelliJ IDEA / Android Studio** → Open → select the root `build.gradle.kts`.
2. Wait for the Gradle sync to complete (imports, indexing).
3. Run configurations are auto-detected for `desktopApp`, `androidApp`.

---

## 3. Commands Reference

### 3.1 Development

| Command | Purpose |
|---------|---------|
| `./gradlew :desktopApp:run` | Run desktop app (JVM) |
| `./gradlew :androidApp:installDebug` | Install on Android device |
| `./gradlew build` | Compile all modules |
| `./gradlew :shared:generateCommonMainBibleStudioDatabaseInterface` | Generate SQLDelight code |

### 3.2 Analysis

| Command | Purpose |
|---------|---------|
| `./gradlew detekt` | Run detekt static analysis |
| `./gradlew ktlintCheck` | Check Kotlin code formatting |
| `./gradlew ktlintFormat` | Auto-format Kotlin code |

### 3.3 Testing

| Command | Purpose |
|---------|---------|
| `./gradlew check` | Run all tests + analysis |
| `./gradlew :shared:allTests` | Run all shared tests (commonTest + platform) |
| `./gradlew :shared:jvmTest` | Run JVM tests only |
| `./gradlew :shared:iosSimulatorArm64Test` | Run iOS tests on simulator |
| `./gradlew koverReport` | Generate code coverage (Kover) |

### 3.4 Build

| Command | Purpose |
|---------|---------|
| `./gradlew :androidApp:assembleRelease` | Android APK release |
| `./gradlew :androidApp:bundleRelease` | Android App Bundle |
| `./gradlew :desktopApp:packageMsi` | Windows MSI installer (jpackage) |
| `./gradlew :desktopApp:packageDmg` | macOS DMG (jpackage) |
| `./gradlew :desktopApp:packageDeb` | Linux DEB package |
| `./gradlew :desktopApp:packageRpm` | Linux RPM package |

---

## 4. Debugging

### 4.1 Desktop Debugging

Run desktop app from IntelliJ with debug configuration — integrated debugger supports breakpoints, watches, and step-through for shared Kotlin code and Compose UI.

### 4.2 Android Debugging

Use Android Studio's built-in debugger with `./gradlew :androidApp:installDebug` and attach debugger.

### 4.3 SQLDelight Query Logging

In debug builds, wrap the `SqlDriver` with a logging interceptor:

```kotlin
// DatabaseProvider.kt (debug build type only)
val driver = createSqlDriver(schema)
val loggingDriver = LoggingSqlDriver(driver) { log ->
    Napier.d(tag = "SQL") { log }
}
```

### 4.4 Compose UI Inspection

- **Layout Inspector** (Android Studio) — inspect Compose tree on Android emulator.
- **Desktop**: use `Modifier.border()` and `println()` debugging for layout issues.
- Compose Multiplatform supports `@Preview` annotations for composable previews in IntelliJ.

### 4.5 Common Issues

| Issue | Solution |
|-------|----------|
| Gradle sync fails | Ensure JDK 17+ is set in `JAVA_HOME` |
| SQLDelight errors | Run `./gradlew generateSqlDelightInterface` |
| Unresolved references | Invalidate caches: File → Invalidate Caches → Restart |
| Android build fails | Verify `ANDROID_HOME` is set and SDK API 24+ is installed |
| iOS build fails | Run `pod install` in `iosApp/`, ensure Xcode 15+ |
| Desktop window blank | Check Compose version compatibility in `libs.versions.toml` |

---

## 5. First Contribution

### 5.1 Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code |
| `develop` | Integration branch |
| `feature/<name>` | New features |
| `fix/<name>` | Bug fixes |
| `docs/<name>` | Documentation changes |

### 5.2 Workflow

1. Create a branch from `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/parables-explorer
   ```

2. Make changes and verify:
   ```bash
   ./gradlew check
   ```

3. Commit with conventional format:
   ```bash
   git commit -m "feat(parables): add parable entity and repository"
   git commit -m "fix(bible-reader): correct verse selection on chapter change"
   git commit -m "docs(modules): add parables-explorer documentation"
   ```

4. Push and open a pull request against `develop`.

### 5.3 Commit Message Format

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

| Type | Usage |
|------|-------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation |
| `refactor` | Code restructure |
| `test` | Test additions/changes |
| `chore` | Build, CI, deps |

### 5.4 PR Checklist

- [ ] `./gradlew check` passes (compile, detekt, tests)
- [ ] `./gradlew ktlintCheck` passes
- [ ] New code has test coverage
- [ ] Module documentation updated (if applicable)
- [ ] CHANGELOG updated (if user-facing)
- [ ] No new warnings introduced

---

## 6. Related Documents

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System overview, layers, bootstrap |
| [CODE_CONVENTIONS.md](CODE_CONVENTIONS.md) | Naming, patterns, import rules |
| [TESTING.md](TESTING.md) | Test patterns and coverage targets |
| [CI_CD.md](CI_CD.md) | Automated pipeline, quality gates |
