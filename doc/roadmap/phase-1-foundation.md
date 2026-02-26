# Phase 1 — Foundation

> Gradle project setup, KMP targets, SQLDelight schema, Koin DI, and basic app shell.
> **Prerequisites**: None — this is the starting point.

---

## 1.1 Gradle Project Setup

- [ ] Initialize root `settings.gradle.kts` with project name `BibleStudio`
- [ ] Create `gradle/libs.versions.toml` version catalog with all dependencies
- [ ] Configure root `build.gradle.kts` with KMP plugin, Compose Multiplatform plugin
- [ ] Create `shared/` module (`build.gradle.kts` with `kotlin("multiplatform")`)
- [ ] Create `composeApp/` module (`build.gradle.kts` with Compose Multiplatform)
- [ ] Configure `gradle.properties` with KMP + Compose flags
- [ ] Add `.gitignore` for Gradle, IDE files, build outputs
- [ ] Verify `./gradlew build` compiles successfully (empty project)

## 1.2 KMP Target Configuration

- [ ] Configure `shared/` targets: `androidTarget()`, `iosArm64()`, `iosSimulatorArm64()`, `jvm("desktop")`
- [ ] Configure `composeApp/` targets: same as shared + `compose.desktop { application {} }`
- [ ] Set up `commonMain`, `androidMain`, `iosMain`, `desktopMain` source sets in `shared/`
- [ ] Set up `commonMain`, `androidMain`, `iosMain`, `desktopMain` source sets in `composeApp/`
- [ ] Add `expect`/`actual` declarations for `createSqlDriver(schema)` per platform
- [ ] Add `expect`/`actual` declarations for `appDataPath()` per platform
- [ ] Verify all 3 targets compile: `./gradlew :shared:build`

## 1.3 SQLDelight Schema

- [ ] Add SQLDelight Gradle plugin to `shared/build.gradle.kts`
- [ ] Configure `sqldelight { databases { create("BibleStudioDatabase") { ... } } }`
- [ ] Create `Bible.sq` — tables: `bibles`, `books`, `chapters`, `verses` + core queries
- [ ] Create `Annotation.sq` — tables: `notes`, `highlights`, `bookmarks` + CRUD queries
- [ ] Create `Study.sq` — tables: `lexicon_entries`, `morphology`, `word_occurrences` + queries
- [ ] Create `Resource.sq` — tables: `resources`, `resource_entries` + queries
- [ ] Create `Writing.sq` — tables: `sermons`, `sermon_sections` + queries
- [ ] Create `Reference.sq` — tables: `cross_references`, `parallel_passages` + queries
- [ ] Create `Settings.sq` — tables: `settings`, `workspaces`, `workspace_layouts` + queries
- [ ] Create `Search.sq` — table: `search_history` + FTS5 virtual tables (`fts_verses`, `fts_notes`, `fts_resources`, `fts_lexicon`, `fts_sermons`)
- [ ] Create sync tables: `sync_log`, `delete_log`
- [ ] Create timeline/geo tables: `timeline_events`, `geographic_locations`
- [ ] Create audio table: `audio_timestamps`
- [ ] Create reading plan tables: `reading_plans`, `reading_plan_progress`
- [ ] Add indexes on all `global_verse_id` columns and FK columns
- [ ] Create migration files `1.sqm` through `16.sqm`
- [ ] Set `deriveSchemaFromMigrations = true` and `verifyMigrations = true`
- [ ] Verify `./gradlew :shared:generateCommonMainBibleStudioDatabaseInterface` succeeds

## 1.4 Platform Drivers

- [ ] Implement `actual fun createSqlDriver()` for Android using `AndroidSqliteDriver`
- [ ] Implement `actual fun createSqlDriver()` for iOS using `NativeSqliteDriver`
- [ ] Implement `actual fun createSqlDriver()` for Desktop/JVM using `JdbcSqliteDriver`
- [ ] Write test: in-memory `JdbcSqliteDriver` creates all 27 tables successfully
- [ ] Write test: schema version matches expected v16

## 1.5 Koin Dependency Injection

- [ ] Add Koin dependencies to `libs.versions.toml`
- [ ] Create `coreModule` — registers `SqlDriver`, `BibleStudioDatabase`, `VerseBus`
- [ ] Create `repositoryModule` — registers all 17 repository implementations
- [ ] Create `componentModule` — registers Decompose component factories
- [ ] Create `initKoin()` function in `commonMain`
- [ ] Call `initKoin()` from platform entry points (Android `Application`, iOS `@main`, Desktop `main()`)
- [ ] Write test: all Koin modules resolve without circular dependencies (`checkModules()`)

## 1.6 Repository Interfaces

- [ ] Define `BibleRepository` interface (getVersions, getBooks, getChapters, getVerses)
- [ ] Define `AnnotationRepository` interface (notes CRUD, highlights CRUD, bookmarks CRUD)
- [ ] Define `StudyRepository` interface (lexicon lookup, morphology for verse, word occurrences)
- [ ] Define `ResourceRepository` interface (list resources, get entries for verse)
- [ ] Define `WritingRepository` interface (sermon CRUD, section CRUD)
- [ ] Define `ReferenceRepository` interface (cross-refs for verse, parallel passages)
- [ ] Define `SettingsRepository` interface (get/set setting, workspace CRUD, layout persistence)
- [ ] Define `SearchRepository` interface (FTS search, search history)
- [ ] Implement all 8 repository classes using SQLDelight-generated queries
- [ ] Write unit tests for each repository using in-memory `JdbcSqliteDriver`

## 1.7 App Shell (Minimum Viable App)

- [ ] Create Android `MainActivity` with `setContent { App() }` entry point
- [ ] Create iOS `ContentView` with `ComposeUIViewController` entry point
- [ ] Create Desktop `main()` with `application { Window { App() } }` entry point
- [ ] Create shared `App()` composable that calls `initKoin()` and shows placeholder text
- [ ] Verify app launches on Android emulator
- [ ] Verify app launches on iOS simulator
- [ ] Verify app launches on Desktop (JVM)
- [ ] Add Napier logging initialization per platform

## 1.8 Base Configuration

- [ ] Add `detekt.yml` with project rules (see CODE_CONVENTIONS §9)
- [ ] Add `.editorconfig` for ktlint
- [ ] Verify `./gradlew detekt` passes
- [ ] Verify `./gradlew ktlintCheck` passes
- [ ] Add Kover plugin and verify `./gradlew koverReport` generates coverage report

---

## Phase 1 Exit Criteria

- [ ] `./gradlew build` succeeds for all targets (Android, iOS, Desktop)
- [ ] SQLDelight generates all query interfaces from 8 `.sq` files
- [ ] All 27 tables + 5 FTS5 tables created via migration chain (v1→v16)
- [ ] Koin resolves all dependencies without errors
- [ ] App shell launches on all 3 target platforms
- [ ] detekt + ktlint pass with zero violations
- [ ] Repository unit tests pass with ≥80% coverage on data layer
