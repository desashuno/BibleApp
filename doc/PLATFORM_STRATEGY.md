# Platform Strategy

> BibleStudio — Multi-Platform Builds, Adaptive Shells & Native Dependencies

---

## 1. Platform Matrix

| Platform | Status | Min Version | Runtime | Shell | Distribution |
|----------|--------|-------------|---------|-------|-------------|
| **Android** | Primary | API 24 (7.0) | ART | Mobile | Play Store |
| **iOS** | Primary | iOS 15.0 | Native (K/N) | Mobile | App Store |
| **Windows** | Primary | Windows 10 1903+ | JVM (JDK 17) | Desktop | MSIX / Sideload |
| **macOS** | Secondary | macOS 12.0+ | JVM (JDK 17) | Desktop | DMG / App Store |
| **Linux** | Secondary | Ubuntu 22.04+ | JVM (JDK 17) | Desktop | DEB / Flatpak |

**Primary** platforms receive parity features and are tested in CI. **Secondary** platforms receive best-effort support and community-driven testing.

> **Note:** Desktop targets run on the JVM, not native. Compose Multiplatform for Desktop uses Skia for rendering, identical to Android's compose rendering pipeline.

---

## 2. Adaptive Shell Architecture

BibleStudio uses two shell variants that adapt the overall chrome to the platform form factor:

```
┌───────────────────────────────────────────┐
│              BibleStudioApp               │
│   ┌─────────────┬─────────────────────┐   │
│   │ MobileShell  │   DesktopShell      │   │
│   │ (< 840 dp)  │   (≥ 840 dp)       │   │
│   ├─────────────┼─────────────────────┤   │
│   │ BottomNav   │   ActivityBar       │   │
│   │ Drawer      │   Multi-pane split  │   │
│   │ Single pane │   Drag-to-resize    │   │
│   └─────────────┴─────────────────────┘   │
└───────────────────────────────────────────┘
```

### 2.1 MobileShell

- **Bottom navigation** with 5 slots (configurable).
- **Module drawer** accessible via swipe from left edge (`ModalNavigationDrawer`).
- **Single visible pane** at a time.
- **Swipe gestures** for chapter navigation.
- **Pull-to-refresh** for applicable modules.
- **Status bar** adapts color to theme.

### 2.2 DesktopShell

- **Activity bar** (48 dp) on the left with module icons.
- **Multi-pane workspace** with drag-to-resize splits.
- **Tab groups** within split regions.
- **Keyboard shortcuts** for pane management.
- **Window title** shows active workspace name.
- **Menu bar** (Windows/Linux) or native menu (macOS) via Compose Desktop `MenuBar`.

---

## 3. KMP Architecture: `expect`/`actual`

Platform-specific code is isolated using Kotlin's `expect`/`actual` mechanism. Shared business logic lives in `commonMain`, with platform implementations in `androidMain`, `iosMain`, and `desktopMain`.

### 3.1 Source Set Structure

```
shared/src/
├── commonMain/       # All shared code (≥95% of codebase)
├── androidMain/      # Android-specific implementations
├── iosMain/          # iOS-specific implementations
├── desktopMain/      # JVM Desktop (Windows + macOS + Linux)
├── jvmMain/          # Shared between Android and Desktop (if needed)
└── nativeMain/       # Shared between iOS targets (if needed)
```

### 3.2 Platform Abstractions

| Abstraction | `expect` Declaration | Purpose |
|-------------|---------------------|---------|
| `createSqlDriver()` | `expect fun createSqlDriver(schema): SqlDriver` | SQLite driver per platform |
| `appDataPath()` | `expect fun appDataPath(): String` | App-private storage path |
| `openUrl()` | `expect fun openUrl(url: String)` | Open external URLs |
| `shareText()` | `expect fun shareText(text: String)` | Native share sheet |
| `pickFile()` | `expect suspend fun pickFile(vararg extensions: String): ByteArray?` | Native file picker |
| `reduceMotion` | `expect val reduceMotion: Boolean` | Accessibility setting |
| `platformName` | `expect val platformName: String` | Analytics / diagnostics |

### 3.3 Example: File Picker

```kotlin
// commonMain
expect suspend fun pickFile(vararg extensions: String): ByteArray?

// androidMain
actual suspend fun pickFile(vararg extensions: String): ByteArray? {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_MIME_TYPES, extensions.map { "application/$it" }.toTypedArray())
    }
    // Launch activity for result via Activity Result API
    return activityResultLauncher.launch(intent)?.readBytes()
}

// iosMain
actual suspend fun pickFile(vararg extensions: String): ByteArray? {
    return suspendCancellableCoroutine { cont ->
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = extensions.map { UTType(it) }
        )
        picker.delegate = object : UIDocumentPickerDelegateProtocol {
            override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAt: List<NSURL>) {
                cont.resume(didPickDocumentsAt.first().readBytes())
            }
        }
        presentViewController(picker)
    }
}

// desktopMain (JVM)
actual suspend fun pickFile(vararg extensions: String): ByteArray? {
    val chooser = JFileChooser().apply {
        fileFilter = FileNameExtensionFilter("Allowed files", *extensions)
    }
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile.readBytes()
    } else null
}
```

---

## 4. Storage Paths

| Platform | Database | Bundled Data | User Exports |
|----------|----------|-------------|-------------|
| Android | `context.filesDir` | APK assets | SAF via file picker |
| iOS | `NSDocumentDirectory` | App bundle resources | Same as database |
| Windows | `%APPDATA%\BibleStudio\` | Bundled in JAR / jpackage | `%USERPROFILE%\Documents\BibleStudio\` |
| macOS | `~/Library/Application Support/BibleStudio/` | Bundled in .app / JAR | `~/Documents/BibleStudio/` |
| Linux | `~/.local/share/biblestudio/` | Bundled in JAR / Flatpak | `~/Documents/BibleStudio/` |

Implementation via `expect`/`actual`:

```kotlin
// commonMain
expect fun appDataPath(): String

// androidMain
actual fun appDataPath(): String = applicationContext.filesDir.absolutePath

// iosMain
actual fun appDataPath(): String {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    return paths.first() as String
}

// desktopMain
actual fun appDataPath(): String {
    val os = System.getProperty("os.name").lowercase()
    return when {
        "win" in os -> System.getenv("APPDATA") + "\\BibleStudio"
        "mac" in os -> System.getProperty("user.home") + "/Library/Application Support/BibleStudio"
        else -> System.getProperty("user.home") + "/.local/share/biblestudio"
    }.also { File(it).mkdirs() }
}
```

---

## 5. File Permissions

| Platform | Read External | Write External | File Picker | Share Sheet |
|----------|--------------|----------------|-------------|-------------|
| Android | Scoped storage | Scoped storage | SAF intent | Android Sharesheet |
| iOS | N/A | N/A | UIDocumentPicker | UIActivityViewController |
| Windows | Unrestricted | Unrestricted | JFileChooser | Clipboard |
| macOS | App Sandbox | App Sandbox | NSOpenPanel | NSSharingService |
| Linux | Unrestricted | Unrestricted | JFileChooser / Portal | Clipboard |

- Android 13+ uses granular media permissions; BibleStudio requires no media permissions (only SAF picker intents).
- macOS App Sandbox requires entitlements for user-selected files (granted via file picker).
- See [SECURITY.md](SECURITY.md) for full permission analysis.

---

## 6. Native Dependencies

### 6.1 Gradle Dependencies

| Dependency | Purpose | Targets |
|-----------|---------|---------|
| `app.cash.sqldelight:*-driver` | Platform SQLite driver | All (3 drivers) |
| `org.jetbrains.compose` | Compose Multiplatform UI | All |
| `com.arkivanov.decompose` | Navigation, components, lifecycle | All |
| `io.insert-koin:koin-core` | Dependency injection | All |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core` | Async / Flow | All |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | JSON serialization | All |
| `io.github.aakira:napier` | Multiplatform logging | All |

### 6.2 Platform-Specific Dependencies

| Dependency | Purpose | Targets |
|-----------|---------|---------|
| `androidx.activity:activity-compose` | Compose activity | Android |
| `androidx.core:core-splashscreen` | Splash screen API | Android |
| `io.coil-kt:coil-compose` | Image loading | Android |
| JVM Swing (built-in) | File dialogs on Desktop | Desktop |
| Kotlin/Native Foundation | iOS platform interop | iOS |

### 6.3 Audio Playback

Audio playback for the Audio Sync module uses platform-specific APIs via `expect`/`actual`:

| Platform | API | Notes |
|----------|-----|-------|
| Android | ExoPlayer / MediaPlayer | Background playback via MediaSession |
| iOS | AVAudioPlayer | Audio session category management |
| Desktop | javax.sound / JavaFX Media | JVM media playback |

---

## 7. Platform UI Differences

### 7.1 Typography Adjustments

| Platform | System Font Fallback | Bible Text Default Size |
|----------|---------------------|------------------------|
| Android | Roboto | 18 sp |
| iOS | SF Pro | 18 sp |
| Windows | Segoe UI | 17 sp |
| macOS | SF Pro | 17 sp |
| Linux | Noto Sans | 17 sp |

The bundled fonts (Merriweather, Source Sans 3, JetBrains Mono) are used everywhere via Compose Multiplatform resources. System fonts serve as fallback for emoji and CJK characters.

### 7.2 Interaction Patterns

| Behavior | Mobile | Desktop |
|----------|--------|---------|
| Text selection | Long press → handles | Click + drag |
| Context menu | Bottom sheet | Right-click popup (`DropdownMenu`) |
| Verse selection | Tap | Click |
| Multi-select | Long press + tap | Ctrl+Click |
| Scroll | Touch scroll | Mouse wheel / scrollbar |
| Zoom | Pinch | Ctrl+Scroll |
| Search | Bottom sheet overlay | Inline panel |

### 7.3 Navigation

| Pattern | Mobile | Desktop |
|---------|--------|---------|
| Module switching | Bottom nav + drawer | Activity bar |
| Back navigation | System back button / swipe | Breadcrumb / Esc |
| Settings | Full-screen page | Side panel |
| Dialogs | Full-screen on phone, centered on tablet | Centered, max 600 dp wide |

---

## 8. Build Guides

### 8.1 Android

```bash
# Debug
./gradlew :androidApp:installDebug

# Release APK
./gradlew :androidApp:assembleRelease

# Release App Bundle (Play Store)
./gradlew :androidApp:bundleRelease

# Signing configured in androidApp/build.gradle.kts
# keystore.properties (gitignored) provides credentials
```

**androidApp/build.gradle.kts** requirements:
```kotlin
android {
    compileSdk = 34
    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }
}
```

### 8.2 iOS

```bash
# Debug (from Xcode or command line)
./gradlew :shared:linkDebugFrameworkIosArm64

# Open Xcode project
open iosApp/iosApp.xcodeproj

# Build and archive from Xcode for App Store submission
# Requires:
# - Xcode 15+
# - Valid Apple Developer account
# - Provisioning profile configured in Xcode
```

The shared KMP module produces a framework that the Xcode project links:
```kotlin
// shared/build.gradle.kts
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
}
```

### 8.3 Windows

```bash
# Debug run
./gradlew :desktopApp:run

# Package as MSI
./gradlew :desktopApp:packageMsi

# Package as EXE installer
./gradlew :desktopApp:packageExe

# Requires:
# - JDK 17+
# - WiX Toolset 3.x (for MSI packaging)
```

Desktop packaging uses Compose Multiplatform's `jpackage` integration:
```kotlin
// desktopApp/build.gradle.kts
compose.desktop {
    application {
        mainClass = "org.biblestudio.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "BibleStudio"
            packageVersion = "1.0.0"
            windows {
                iconFile.set(project.file("icon.ico"))
                menuGroup = "BibleStudio"
                upgradeUuid = "a-unique-uuid-here"
            }
        }
    }
}
```

### 8.4 macOS

```bash
# Debug run
./gradlew :desktopApp:run

# Package as DMG
./gradlew :desktopApp:packageDmg

# Package for Mac App Store
./gradlew :desktopApp:packagePkg

# Requires:
# - JDK 17+
# - Xcode command-line tools (for codesigning)
```

```kotlin
// desktopApp/build.gradle.kts (macOS section)
compose.desktop {
    application {
        nativeDistributions {
            macOS {
                iconFile.set(project.file("icon.icns"))
                bundleID = "org.biblestudio.app"
                signing {
                    sign.set(true)
                    identity.set("Developer ID Application: ...")
                }
                notarization {
                    appleID.set("apple-id@example.com")
                    password.set("@keychain:AC_PASSWORD")
                }
            }
        }
    }
}
```

### 8.5 Linux

```bash
# Debug run
./gradlew :desktopApp:run

# Package as DEB
./gradlew :desktopApp:packageDeb

# Package as RPM
./gradlew :desktopApp:packageRpm

# Requires:
# - JDK 17+
# - dpkg (for DEB) or rpm-build (for RPM)
```

```kotlin
// desktopApp/build.gradle.kts (Linux section)
compose.desktop {
    application {
        nativeDistributions {
            linux {
                iconFile.set(project.file("icon.png"))
                packageName = "biblestudio"
                debMaintainer = "team@biblestudio.org"
            }
        }
    }
}
```

---

## 9. Pre-Release Checklist

### 9.1 All Platforms

- [ ] All tests pass (`./gradlew check`)
- [ ] No Kotlin compiler warnings
- [ ] Version bumped in `build.gradle.kts`
- [ ] CHANGELOG updated
- [ ] SQLDelight migrations verified (`verifyMigrations = true`)

### 9.2 Android

- [ ] Release signed with production keystore
- [ ] ProGuard / R8 rules verified (keep SQLDelight generated classes)
- [ ] Minimum SDK tested on API 24 emulator
- [ ] App bundle analyzed with `bundletool`
- [ ] Play Store listing updated

### 9.3 iOS

- [ ] Framework links without error in Xcode
- [ ] Archive builds successfully
- [ ] Tested on oldest supported device (iPhone 8 / iOS 15)
- [ ] App Store screenshots updated
- [ ] Privacy manifest (`PrivacyInfo.xcprivacy`) current

### 9.4 Windows

- [ ] MSI package installs and uninstalls cleanly
- [ ] Tested on Windows 10 1903
- [ ] Code signed with EV certificate (if distributing outside Store)
- [ ] JVM bundled correctly via jpackage
- [ ] SQLite JDBC driver loads correctly

### 9.5 macOS

- [ ] Sandbox entitlements minimal
- [ ] Notarized with Apple
- [ ] Tested on macOS 12
- [ ] DMG opens and drags to Applications
- [ ] JVM runtime bundled in .app

### 9.6 Linux

- [ ] DEB/RPM installs and runs
- [ ] Tested on Ubuntu 22.04
- [ ] JVM runtime bundled or documented as dependency
- [ ] Desktop file and icon registered
- [ ] SQLite JDBC driver loads correctly

---

## 10. Related Documents

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System architecture, bootstrap, routing |
| [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) | Responsive breakpoints, adaptive components |
| [CI_CD.md](CI_CD.md) | Automated builds per platform |
| [SECURITY.md](SECURITY.md) | Platform permissions, data protection |
| [GETTING_STARTED.md](GETTING_STARTED.md) | Setup for each platform |
