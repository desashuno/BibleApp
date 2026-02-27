# Phase 1 — Foundation

> Gradle project setup, KMP targets, SQLDelight schema, Koin DI, and basic app shell.
> **Prerequisites**: None — this is the starting point.

---

## 1.1 Gradle Project Setup

- [x] Initialize root `settings.gradle.kts` with project name `BibleStudio`
- [x] Create `gradle/libs.versions.toml` version catalog with all dependencies
- [x] Configure root `build.gradle.kts` with KMP plugin, Compose Multiplatform plugin
- [x] Create `shared/` module (`build.gradle.kts` with `kotlin("multiplatform")`)
- [x] Create `composeApp/` module (`build.gradle.kts` with Compose Multiplatform)
- [x] Configure `gradle.properties` with KMP + Compose flags
- [x] Add `.gitignore` for Gradle, IDE files, build outputs
- [x] Verify `./gradlew build` compiles successfully (empty project)

## 1.2 KMP Target Configuration

- [x] Configure `shared/` targets: `androidTarget()`, `iosArm64()`, `iosSimulatorArm64()`, `jvm("desktop")`
- [x] Configure `composeApp/` targets: same as shared + `compose.desktop { application {} }`
- [x] Set up `commonMain`, `androidMain`, `iosMain`, `desktopMain` source sets in `shared/`
- [x] Set up `commonMain`, `androidMain`, `iosMain`, `desktopMain` source sets in `composeApp/`
- [x] Add `expect`/`actual` declarations for `createSqlDriver(schema)` per platform
- [x] Add `expect`/`actual` declarations for `appDataPath()` per platform
- [x] Verify all 3 targets compile: `./gradlew :shared:build`

## 1.3 SQLDelight Schema

- [x] Add SQLDelight Gradle plugin to `shared/build.gradle.kts`
- [x] Configure `sqldelight { databases { create("BibleStudioDatabase") { ... } } }`
- [x] Create `Bible.sq` — tables: `bibles`, `books`, `chapters`, `verses` + core queries
- [x] Create `Annotation.sq` — tables: `notes`, `highlights`, `bookmarks` + CRUD queries
- [x] Create `Study.sq` — tables: `lexicon_entries`, `morphology`, `word_occurrences` + queries
- [x] Create `Resource.sq` — tables: `resources`, `resource_entries` + queries
- [x] Create `Writing.sq` — tables: `sermons`, `sermon_sections` + queries
- [x] Create `Reference.sq` — tables: `cross_references`, `parallel_passages` + queries
- [x] Create `Settings.sq` — tables: `settings`, `workspaces`, `workspace_layouts` + queries
- [x] Create `Search.sq` — table: `search_history` + FTS5 virtual tables (`fts_verses`, `fts_notes`, `fts_resources`, `fts_lexicon`, `fts_sermons`)
- [x] Create sync tables: `sync_log`, `delete_log`
- [x] Create timeline/geo tables: `timeline_events`, `geographic_locations`
- [x] Create audio table: `audio_timestamps`
- [x] Create reading plan tables: `reading_plans`, `reading_plan_progress`
- [x] Add indexes on all `global_verse_id` columns and FK columns
- [x] Create migration files `1.sqm` through `16.sqm`
- [x] Set `deriveSchemaFromMigrations = true` and `verifyMigrations = true`
- [x] Verify `./gradlew :shared:generateCommonMainBibleStudioDatabaseInterface` succeeds

## 1.4 Platform Drivers

- [x] Implement `actual fun createSqlDriver()` for Android using `AndroidSqliteDriver`
- [x] Implement `actual fun createSqlDriver()` for iOS using `NativeSqliteDriver`
- [x] Implement `actual fun createSqlDriver()` for Desktop/JVM using `JdbcSqliteDriver`
- [x] Write test: in-memory `JdbcSqliteDriver` creates all 27 tables successfully
- [x] Write test: schema version matches expected v16

## 1.5 Koin Dependency Injection

- [x] Add Koin dependencies to `libs.versions.toml`
- [x] Create `coreModule` — registers `SqlDriver`, `BibleStudioDatabase`, `VerseBus`
- [x] Create `repositoryModule` — registers all 17 repository implementations
- [x] Create `componentModule` — registers Decompose component factories (placeholder)
- [x] Create `initKoin()` function in `commonMain` (`Bootstrap.kt`)
- [x] Call `initKoin()` from platform entry points (Android `MainActivity`, iOS `MainViewController`, Desktop `main()`)
- [x] Write test: all Koin modules resolve without circular dependencies (`checkModules()`)

## 1.6 Repository Interfaces

> **Note:** Per ARCHITECTURE.md, repositories were split into 17 granular interfaces
> instead of the original 8 consolidated ones for better separation of concerns.

- [x] Define `BibleRepository` + `TextComparisonRepository` (bible text & version comparison)
- [x] Define `NoteRepository`, `HighlightRepository`, `BookmarkRepository` (annotations)
- [x] Define `WordStudyRepository`, `MorphologyRepository`, `DictionaryRepository` (study tools)
- [x] Define `ResourceRepository`, `CommentaryRepository` (resources & commentary)
- [x] Define `SermonRepository` (sermon CRUD, section CRUD)
- [x] Define `CrossRefRepository`, `ParallelRepository` (cross-references & parallels)
- [x] Define `SettingsRepository`, `WorkspaceRepository` (settings & workspace management)
- [x] Define `SearchRepository` (FTS search, search history)
- [x] Define `ReadingPlanRepository` (reading plans & progress tracking)
- [x] Implement all 17 repository classes using SQLDelight-generated queries
- [x] Create domain entities (25 data classes across all features)
- [x] Create mappers (15 files bridging SQLDelight ↔ domain)
- [x] Write unit tests for repositories using in-memory `JdbcSqliteDriver`

## 1.7 App Shell (Minimum Viable App)

- [x] Create Android `MainActivity` with `setContent { App() }` entry point
- [x] Create iOS `ContentView` with `ComposeUIViewController` entry point
- [x] Create Desktop `main()` with `application { Window { App() } }` entry point
- [x] Create shared `App()` composable that calls `initKoin()` and shows placeholder text
- [x] Verify app launches on Android emulator
- [x] Verify app launches on iOS simulator
- [x] Verify app launches on Desktop (JVM)
- [x] Add Napier logging initialization per platform

## 1.8 Base Configuration

- [x] Add `detekt.yml` with project rules (see CODE_CONVENTIONS §9)
- [x] Add `.editorconfig` for ktlint
- [x] Verify `./gradlew detekt` passes
- [x] Verify `./gradlew ktlintCheck` passes
- [x] Add Kover plugin and verify `./gradlew koverReport` generates coverage report

---

## Phase 1 Exit Criteria

- [x] `./gradlew build` succeeds for all targets (Android, iOS, Desktop)
- [x] SQLDelight generates all query interfaces from 8 `.sq` files
- [x] All 27 tables + 5 FTS5 tables created via migration chain (v1→v16)
- [x] Koin resolves all dependencies without errors
- [x] App shell launches on all 3 target platforms
- [x] detekt + ktlint pass with zero violations
- [x] Repository unit tests pass with ≥80% coverage on data layer
