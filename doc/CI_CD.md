# CI/CD

> BibleStudio — GitHub Actions, Quality Gates & Release Process (Kotlin Multiplatform)

---

## 1. Overview

BibleStudio uses **GitHub Actions** for continuous integration and delivery. Every push and pull request triggers analysis, testing, and (on tagged releases) platform builds. The build system is **Gradle** with Kotlin Multiplatform and Compose Multiplatform plugins.

---

## 2. Workflow

### 2.1 Pipeline (`ci.yml`)

```yaml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

jobs:
  # ──────────────────────────────────────────
  # Stage 1: Analyze
  # ──────────────────────────────────────────
  analyze:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle.kts', '**/gradle-wrapper.properties') }}

      - run: ./gradlew detekt
      - run: ./gradlew ktlintCheck

  # ──────────────────────────────────────────
  # Stage 2: Test
  # ──────────────────────────────────────────
  test:
    needs: analyze
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle.kts', '**/gradle-wrapper.properties') }}

      - run: ./gradlew check
      - run: ./gradlew koverReport

      # Coverage gate: fail if below 80%
      - name: Check coverage
        run: |
          REPORT="shared/build/reports/kover/report.xml"
          if [ -f "$REPORT" ]; then
            COVERAGE=$(grep -oP 'line-rate="\K[0-9.]+' "$REPORT" | head -1)
            PERCENT=$(echo "$COVERAGE * 100" | bc)
            echo "Line coverage: ${PERCENT}%"
            if (( $(echo "$PERCENT < 80" | bc -l) )); then
              echo "::error::Coverage ${PERCENT}% is below 80% threshold"
              exit 1
            fi
          fi

      - name: Upload coverage
        uses: actions/upload-artifact@v4
        with:
          name: coverage-report
          path: shared/build/reports/kover/

  # ──────────────────────────────────────────
  # Stage 3: Build (release tags only)
  # ──────────────────────────────────────────
  build-android:
    if: startsWith(github.ref, 'refs/tags/v')
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle.kts', '**/gradle-wrapper.properties') }}

      - run: ./gradlew :androidApp:bundleRelease

      - uses: actions/upload-artifact@v4
        with:
          name: android-bundle
          path: androidApp/build/outputs/bundle/release/*.aab

  build-ios:
    if: startsWith(github.ref, 'refs/tags/v')
    needs: test
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle.kts', '**/gradle-wrapper.properties') }}

      # Build the shared KMP framework for iOS
      - run: ./gradlew :shared:linkReleaseFrameworkIosArm64

      # Build the iOS app via xcodebuild
      - name: Build iOS
        run: |
          cd iosApp
          xcodebuild archive \
            -scheme iosApp \
            -archivePath build/BibleStudio.xcarchive \
            -configuration Release \
            CODE_SIGN_IDENTITY="" \
            CODE_SIGNING_REQUIRED=NO

      - uses: actions/upload-artifact@v4
        with:
          name: ios-archive
          path: iosApp/build/BibleStudio.xcarchive

  build-windows:
    if: startsWith(github.ref, 'refs/tags/v')
    needs: test
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle.kts', '**/gradle-wrapper.properties') }}

      - run: ./gradlew :desktopApp:packageMsi

      - uses: actions/upload-artifact@v4
        with:
          name: windows-msi
          path: desktopApp/build/compose/binaries/main/msi/*.msi

  build-macos:
    if: startsWith(github.ref, 'refs/tags/v')
    needs: test
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle.kts', '**/gradle-wrapper.properties') }}

      - run: ./gradlew :desktopApp:packageDmg

      - uses: actions/upload-artifact@v4
        with:
          name: macos-dmg
          path: desktopApp/build/compose/binaries/main/dmg/*.dmg

  build-linux:
    if: startsWith(github.ref, 'refs/tags/v')
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle.kts', '**/gradle-wrapper.properties') }}

      - run: ./gradlew :desktopApp:packageDeb

      - uses: actions/upload-artifact@v4
        with:
          name: linux-deb
          path: desktopApp/build/compose/binaries/main/deb/*.deb
```

---

## 3. Quality Gates

| Gate | Trigger | Threshold | Blocks Merge |
|------|---------|-----------|-------------|
| **Detekt** | Every push/PR | No baseline violations | Yes |
| **ktlint** | Every push/PR | `./gradlew ktlintCheck` clean | Yes |
| **Tests** | Every push/PR | `./gradlew check` — all tests pass | Yes |
| **Coverage** | Every push/PR | ≥ 80% line coverage (Kover) | Yes |
| **Build** | Tagged releases | Successful artifact generation | Release |

### Branch Protection Rules (GitHub)

- Require `analyze` and `test` jobs to pass before merge.
- Require at least 1 approving review.
- Dismiss stale approvals on new commits.
- Require branches to be up to date with target.

---

## 4. Branch Strategy

```
main ─────────────────────────────────────► (releases)
  │                                    ▲
  │                                    │ merge
  ▼                                    │
develop ──────────────────────────────────►
  │          ▲     │          ▲
  │          │     │          │
  ▼          │     ▼          │
feature/a ───┘   fix/b ──────┘
```

| Branch | Source | Merges Into | CI |
|--------|--------|-------------|-----|
| `main` | Tagged releases | — | Full |
| `develop` | `main` | `main` (via PR) | Full |
| `feature/*` | `develop` | `develop` (via PR) | Full |
| `fix/*` | `develop` | `develop` (via PR) | Full |
| `docs/*` | `develop` | `develop` (via PR) | Analyze only |

---

## 5. Release Process

### 5.1 Version Scheme

BibleStudio uses **semantic versioning**: `MAJOR.MINOR.PATCH`

| Component | Increment When |
|-----------|---------------|
| `MAJOR` | Breaking changes to data format or public API |
| `MINOR` | New features, new modules, schema migrations |
| `PATCH` | Bug fixes, performance improvements, doc updates |

### 5.2 Release Steps

1. **Prepare** on `develop`:
   - Update `version` in root `build.gradle.kts` or `gradle.properties`
   - Update `CHANGELOG.md`
   - Run full test suite locally: `./gradlew check`
   - Commit: `chore(release): prepare v1.2.0`

2. **Merge** `develop` → `main` via PR.

3. **Tag** on `main`:
   ```bash
   git tag v1.2.0
   git push origin v1.2.0
   ```

4. **CI builds** trigger for the tag:
   - Android App Bundle → uploaded as artifact
   - iOS archive → uploaded as artifact
   - Windows MSI → uploaded as artifact
   - macOS DMG → uploaded as artifact
   - Linux DEB → uploaded as artifact

5. **Distribute**:
   - Android → Google Play Console
   - iOS → App Store Connect (sign + upload via Xcode or Fastlane)
   - Windows → MSI sideload / Microsoft Store (MSIX)
   - macOS → DMG notarization + distribution or Mac App Store
   - Linux → DEB/RPM via release page or Flathub

### 5.3 Hotfix Process

1. Branch `fix/critical-bug` from `main`.
2. Fix, test, PR into `main`.
3. Tag `v1.2.1`.
4. Cherry-pick or merge back into `develop`.

---

## 6. Related Documents

| Document | Description |
|----------|-------------|
| [TESTING.md](TESTING.md) | Test pyramid, patterns, coverage targets |
| [GETTING_STARTED.md](GETTING_STARTED.md) | Build commands, branch workflow |
| [PLATFORM_STRATEGY.md](PLATFORM_STRATEGY.md) | Per-platform build guides, pre-release checklists |
| [CODE_CONVENTIONS.md](CODE_CONVENTIONS.md) | Commit message format |
