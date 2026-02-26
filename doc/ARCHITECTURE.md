# Architecture

> BibleStudio — System Architecture Reference

---

## 1. Overview

BibleStudio is a cross-platform Bible study application built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**. It targets five platforms — Android, iOS, Windows, macOS, and Linux — from a single Kotlin codebase. The application follows a **feature-first** project structure with clear separation between domain, data, and presentation layers inside each feature.

### Core Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Language | Kotlin 2.x | Shared codebase across all platforms |
| UI Framework | Compose Multiplatform (JetBrains) | Declarative UI for all targets |
| Architecture | Decompose | Lifecycle-aware components, navigation, state management |
| Database | SQLDelight 2.x (SQLite) | Type-safe SQL queries, migrations |
| Dependency Injection | Koin 3.x | Lightweight multiplatform DI |
| Async | Kotlin Coroutines + Flow | Structured concurrency, reactive streams |
| Serialization | kotlinx.serialization | JSON encoding/decoding without reflection |
| Inter-pane Communication | Verse Bus (`SharedFlow`) | Reactive broadcast of navigation events |

---

## 2. Architectural Layers

```
┌──────────────────────────────────────────────────┐
│                  Presentation                     │
│   Composables · Components · Screens · Pane Shells│
├──────────────────────────────────────────────────┤
│                    Domain                         │
│   Entities (data class) · Repository Interfaces   │
│   Use Cases · Value Objects                       │
├──────────────────────────────────────────────────┤
│                     Data                          │
│   Repository Implementations · SQLDelight Queries │
│   DTOs · Mappers                                  │
├──────────────────────────────────────────────────┤
│                  Infrastructure                   │
│   BibleStudioDatabase · Verse Bus · PaneRegistry  │
│   Platform Services (expect/actual) · Koin DI     │
└──────────────────────────────────────────────────┘
```

### 2.1 Presentation

- **Composables** (`@Composable` functions) render UI. They observe `StateFlow` from Decompose components and dispatch events via component callbacks.
- **Components** (Decompose `ComponentContext`) orchestrate state transitions. Each component follows the interface/implementation convention: `BibleReaderComponent` interface + `DefaultBibleReaderComponent` implementation.
- **Screens** compose UI composables and bind them to component state.
- **Pane Shells** wrap module content inside the workspace layout system.

### 2.2 Domain

- **Entities** are Kotlin `data class` instances that represent core business objects. No code generation required — Kotlin provides `equals`, `hashCode`, `copy`, and destructuring natively.
- **Repository Interfaces** define contracts consumed by components. They live in `domain/` and know nothing about SQLDelight or SQL.
- **Use Cases** encapsulate single-purpose business logic when a component operation grows complex.

### 2.3 Data

- **Repository Implementations** satisfy domain interfaces. They depend on SQLDelight-generated query wrappers and map between database rows and domain entities.
- **SQLDelight Queries** are defined in `.sq` files as plain SQL. The Gradle plugin generates type-safe Kotlin functions at build time.
- **Mappers** convert between SQLDelight-generated data classes and domain entities.

### 2.4 Infrastructure

- **BibleStudioDatabase** is the single SQLDelight `SqlDriver`-backed database that owns all tables and queries. See [DATA_LAYER.md](DATA_LAYER.md).
- **Verse Bus** broadcasts the currently selected verse across panes using `SharedFlow`. See [MODULE_SYSTEM.md](MODULE_SYSTEM.md).
- **PaneRegistry** maps pane type identifiers to their `@Composable` builder functions. See [MODULE_SYSTEM.md](MODULE_SYSTEM.md).
- **Koin DI** wires everything together at startup. See §4.

---

## 3. Feature-First Project Structure

```
BibleStudio/
├── build.gradle.kts                          # Root build config
├── settings.gradle.kts                       # Module declarations
├── gradle.properties                         # KMP + Compose flags
│
├── shared/                                   # KMP shared module
│   ├── build.gradle.kts                      # commonMain, androidMain, iosMain, desktopMain
│   └── src/
│       ├── commonMain/kotlin/org/biblestudio/
│       │   ├── App.kt                        # Root composable
│       │   ├── di/
│       │   │   └── Modules.kt               # Koin module definitions
│       │   ├── core/
│       │   │   ├── database/
│       │   │   │   ├── BibleStudioDatabase.sq  # (tables live in sqldelight/)
│       │   │   │   └── DriverFactory.kt      # expect fun createDriver()
│       │   │   ├── verse_bus/
│       │   │   │   ├── VerseBus.kt
│       │   │   │   └── LinkEvent.kt          # sealed class
│       │   │   ├── theme/
│       │   │   │   └── AppTheme.kt
│       │   │   ├── error/
│       │   │   │   └── AppError.kt           # sealed class
│       │   │   └── extensions/
│       │   ├── features/
│       │   │   ├── bible_reader/
│       │   │   │   ├── domain/
│       │   │   │   ├── data/
│       │   │   │   └── presentation/
│       │   │   ├── search/
│       │   │   │   ├── domain/
│       │   │   │   ├── data/
│       │   │   │   └── presentation/
│       │   │   ├── word_study/
│       │   │   │   └── ...
│       │   │   └── ... (21 feature modules)
│       │   ├── shared/
│       │   │   ├── components/
│       │   │   ├── utils/
│       │   │   └── constants/
│       │   └── workspace/
│       │       ├── WorkspaceComponent.kt
│       │       ├── LayoutNode.kt             # sealed class
│       │       └── PaneRegistry.kt
│       │
│       ├── androidMain/kotlin/org/biblestudio/
│       │   └── core/database/DriverFactory.android.kt
│       ├── iosMain/kotlin/org/biblestudio/
│       │   └── core/database/DriverFactory.ios.kt
│       └── desktopMain/kotlin/org/biblestudio/
│           └── core/database/DriverFactory.desktop.kt
│
├── androidApp/                               # Android entry point
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/.../MainActivity.kt
│
├── iosApp/                                   # iOS entry point (Xcode project)
│   └── iosApp/
│       └── ContentView.swift                 # Hosts ComposeUIViewController
│
├── desktopApp/                               # JVM Desktop entry point
│   ├── build.gradle.kts
│   └── src/main/kotlin/.../Main.kt          # Window + application {}
│
└── sqldelight/                               # SQL source files
    └── org/biblestudio/database/
        ├── Bible.sq
        ├── Annotation.sq
        ├── Study.sq
        ├── Resource.sq
        ├── Writing.sq
        ├── Reference.sq
        ├── Settings.sq
        ├── Search.sq
        └── migrations/
            ├── 1.sqm
            ├── 2.sqm
            └── ... through 16.sqm
```

Each feature is self-contained. No feature imports another feature directly — cross-feature communication flows through the Verse Bus or shared services registered in the Koin DI container.

---

## 4. Dependency Injection

### 4.1 Registration

BibleStudio uses **Koin** for multiplatform dependency injection. Module definitions live in `commonMain` and platform-specific overrides use `expect`/`actual`:

```kotlin
// di/Modules.kt
val coreModule = module {
    // Database (platform-specific driver via expect/actual)
    single { BibleStudioDatabase(get<SqlDriver>()) }

    // Verse Bus
    single { VerseBus() }

    // Pane Registry
    single { PaneRegistry() }
}

val repositoryModule = module {
    single<BibleRepository> { BibleRepositoryImpl(get()) }
    single<NoteRepository> { NoteRepositoryImpl(get()) }
    single<HighlightRepository> { HighlightRepositoryImpl(get()) }
    single<BookmarkRepository> { BookmarkRepositoryImpl(get()) }
    single<WordStudyRepository> { WordStudyRepositoryImpl(get()) }
    single<MorphologyRepository> { MorphologyRepositoryImpl(get()) }
    single<ResourceRepository> { ResourceRepositoryImpl(get()) }
    single<CommentaryRepository> { CommentaryRepositoryImpl(get()) }
    single<DictionaryRepository> { DictionaryRepositoryImpl(get()) }
    single<SermonRepository> { SermonRepositoryImpl(get()) }
    single<CrossRefRepository> { CrossRefRepositoryImpl(get()) }
    single<ParallelRepository> { ParallelRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<WorkspaceRepository> { WorkspaceRepositoryImpl(get()) }
    single<SearchRepository> { SearchRepositoryImpl(get()) }
    single<ReadingPlanRepository> { ReadingPlanRepositoryImpl(get()) }
    single<TextComparisonRepository> { TextComparisonRepositoryImpl(get()) }
}

val componentModule = module {
    factory { params ->
        DefaultBibleReaderComponent(
            componentContext = params.get(),
            repository = get(),
            verseBus = get(),
        )
    }
    // ... other component factories
}

fun initKoin() {
    startKoin {
        modules(coreModule, repositoryModule, componentModule)
    }
}
```

### 4.2 Platform-Specific Driver

```kotlin
// commonMain — expect declaration
expect fun createSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver

// androidMain
actual fun createSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver =
    AndroidSqliteDriver(
        schema = schema,
        context = applicationContext,
        name = "biblestudio.db",
    )

// iosMain
actual fun createSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver =
    NativeSqliteDriver(
        schema = schema,
        name = "biblestudio.db",
    )

// desktopMain
actual fun createSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver =
    JdbcSqliteDriver("jdbc:sqlite:${appDataPath()}/biblestudio.db").also {
        schema.create(it)
    }
```

### 4.3 Query Grouping

SQLDelight organizes queries into `.sq` files, each acting as a bounded context (equivalent to DAOs):

| `.sq` File | Bounded Context | Key Tables |
|-----------|----------------|------------|
| `Bible.sq` | Bible text, books, chapters, verses | `bibles`, `books`, `verses`, `chapters` |
| `Annotation.sq` | Notes, highlights, bookmarks | `notes`, `highlights`, `bookmarks` |
| `Study.sq` | Word study, morphology, lexicon | `lexicon_entries`, `morphology`, `word_occurrences` |
| `Resource.sq` | Commentaries, dictionaries, media | `resources`, `resource_entries` |
| `Writing.sq` | Sermon editor, outlines | `sermons`, `sermon_sections` |
| `Reference.sq` | Cross-references, parallels, links | `cross_references`, `parallel_passages` |
| `Settings.sq` | User preferences, workspace state | `settings`, `workspaces`, `workspace_layouts` |
| `Search.sq` | FTS5 indexes, search history | `search_history`, FTS5 virtual tables |

### 4.4 Access Pattern

Decompose components receive repository interfaces via constructor injection (Koin):

```kotlin
class DefaultBibleReaderComponent(
    componentContext: ComponentContext,
    private val repository: BibleRepository,
    private val verseBus: VerseBus,
) : BibleReaderComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(BibleReaderState())
    override val state: StateFlow<BibleReaderState> = _state.asStateFlow()

    private val scope = coroutineScope(Dispatchers.Main.immediate)

    override fun loadChapter(bookId: Int, chapter: Int) {
        scope.launch {
            _state.update { it.copy(loading = true) }
            repository.getVerses(bookId, chapter)
                .onSuccess { verses ->
                    _state.update { it.copy(loading = false, verses = verses, bookId = bookId, chapter = chapter) }
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false, error = error.toAppError()) }
                }
        }
    }
}
```

Repository implementations receive the database directly:

```kotlin
class BibleRepositoryImpl(
    private val database: BibleStudioDatabase,
) : BibleRepository {

    override suspend fun getVerses(bookId: Int, chapter: Int): Result<List<Verse>> =
        runCatching {
            database.bibleQueries
                .versesForChapter(bookId, chapter)
                .executeAsList()
                .map { row -> row.toVerse() }
        }
}
```

---

## 5. Data Flow

### 5.1 Read Path (User taps a chapter)

```
User Tap
  → Composable calls component.loadChapter(bookId, chapter)
    → Component launches coroutine
      → Calls BibleRepository.getVerses()
        → BibleRepositoryImpl queries SQLDelight
          → SQLDelight executes type-safe SELECT
            → Returns List<VersesForChapter> (generated)
          → Mapper converts to List<Verse>
        → Returns Result<List<Verse>>
      → Component updates MutableStateFlow
    → Composable recomposes via collectAsState()
```

### 5.2 Write Path (User creates a note)

```
User submits note
  → Composable calls component.createNote(note)
    → Component launches coroutine
      → Calls NoteRepository.create(note)
        → NoteRepositoryImpl calls database.annotationQueries.insertNote(...)
          → SQLDelight executes type-safe INSERT
          → Returns generated UUID
        → Returns Note with ID
      → Component updates StateFlow with saved note
    → Composable recomposes with confirmation
```

### 5.3 Cross-Pane Navigation (Verse Bus)

```
User taps verse reference in Commentary pane
  → CommentaryComponent publishes LinkEvent.VerseSelected(globalVerseId)
    → VerseBus (SharedFlow, replay=1) broadcasts to all collectors
      → BibleReaderComponent receives event → scrolls to verse
      → CrossRefComponent receives event → loads references
      → WordStudyComponent receives event → loads lexicon entry
```

---

## 6. Error Handling

### 6.1 Error Taxonomy

```kotlin
/** Base class for all BibleStudio domain errors. */
sealed class AppError(
    open val userMessage: String,
    open val debugMessage: String,
) {
    data class Database(override val userMessage: String, override val debugMessage: String) : AppError(userMessage, debugMessage)
    data class Network(override val userMessage: String, override val debugMessage: String) : AppError(userMessage, debugMessage)
    data class FileImport(override val userMessage: String, override val debugMessage: String) : AppError(userMessage, debugMessage)
    data class Validation(override val userMessage: String, override val debugMessage: String) : AppError(userMessage, debugMessage)
    data class Permission(override val userMessage: String, override val debugMessage: String) : AppError(userMessage, debugMessage)
}
```

### 6.2 Strategy by Layer

| Layer | Strategy |
|-------|----------|
| **Data / SQLDelight** | Wrap in `runCatching {}`, map to `Result<T>` |
| **Repository** | Catch data errors, map exceptions to `AppError` subtypes via `Result.mapError()` |
| **Component** | Update `StateFlow` with error state; never throw from callbacks |
| **Composable** | Display user-facing messages from `AppError.userMessage` |

### 6.3 Rules

- **Never swallow exceptions silently.** Every `catch` must log or re-emit.
- **Components update `StateFlow` with error states** — they never throw. The composable layer reads the error from state.
- **Repositories return `Result<T>`** — they translate infrastructure exceptions into domain errors.
- **User-facing strings** always come from `AppError.userMessage`, which supports localization.

---

## 7. Logging

### 7.1 Levels

| Level | Usage |
|-------|-------|
| `verbose` | SQLDelight query tracing (debug builds only) |
| `debug` | Component state transitions |
| `info` | Module lifecycle (init, dispose), app startup milestones |
| `warning` | Recoverable errors, deprecated code paths |
| `error` | Unrecoverable errors, caught exceptions with stack traces |

### 7.2 Convention

```kotlin
import io.github.aakira.napier.Napier

// Usage in a component
Napier.d(tag = "BibleReaderComponent") { "Loading chapter: book=$bookId chapter=$chapter" }
Napier.e(tag = "BibleReaderComponent", throwable = e) { "Failed to load chapter" }
```

- **Napier** is a KMP-compatible logging library that delegates to Logcat (Android), OSLog (iOS), and SLF4J (Desktop).
- Logger tags match the class name.
- Debug and verbose logs are stripped in release builds via ProGuard/R8.

---

## 8. Bootstrap Sequence

```
main() / Application.onCreate() / ContentView
  → initKoin()
    1. Register platform-specific SqlDriver via expect/actual
    2. Create BibleStudioDatabase
       → Run pending migrations (schema v16)
       → Seed default data if first launch
    3. Register singletons in Koin
       → Database, VerseBus, PaneRegistry
       → Repository implementations
       → Component factories
    4. PaneRegistry.init()
       → Register 21 pane composable builders
    5. Restore last workspace layout from SettingsRepository
  → Compose entry point
    → AppTheme { WorkspaceShell() }
    → RootComponent (Decompose) manages ChildStack
```

---

## 9. Routing

BibleStudio uses **Decompose** `ChildStack` for navigation. The root component manages a stack of screen configurations:

```kotlin
sealed class RootConfig {
    data object Workspace : RootConfig()
    data object Settings : RootConfig()
    data object Import : RootConfig()
    data class DeepLink(val globalVerseId: Int) : RootConfig()
}

class DefaultRootComponent(
    componentContext: ComponentContext,
    private val verseBus: VerseBus,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<RootConfig>()

    override val childStack: Value<ChildStack<RootConfig, RootChild>> =
        childStack(
            source = navigation,
            initialConfiguration = RootConfig.Workspace,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    private fun createChild(config: RootConfig, context: ComponentContext): RootChild =
        when (config) {
            is RootConfig.Workspace -> RootChild.Workspace(workspaceComponent(context))
            is RootConfig.Settings -> RootChild.Settings(settingsComponent(context))
            is RootConfig.Import -> RootChild.Import(importComponent(context))
            is RootConfig.DeepLink -> {
                verseBus.navigate(LinkEvent.VerseSelected(config.globalVerseId))
                navigation.replaceCurrent(RootConfig.Workspace)
                RootChild.Workspace(workspaceComponent(context))
            }
        }

    override fun navigateTo(config: RootConfig) {
        navigation.push(config)
    }

    override fun onBack() {
        navigation.pop()
    }
}
```

- **Deep links** resolve `/verse/:id` by publishing to the Verse Bus and navigating to Workspace.
- The `WorkspaceShell` composable provides the activity bar, bottom navigation, and pane layout.
- Settings and import have dedicated full-screen destinations outside the workspace.
- Decompose manages component lifecycle — components survive configuration changes on Android.

---

## 10. Caching Strategy

| Data Type | Strategy | TTL |
|-----------|----------|-----|
| Bible text | Pre-loaded into SQLite, no HTTP cache | Permanent |
| FTS5 indexes | Built at import time, persisted | Permanent |
| User annotations | SQLite, write-through | Permanent |
| Resource entries | SQLite, lazy-loaded from modules | Permanent |
| Thumbnails / media | Platform file cache via Coil / image loader | 30 days |
| Workspace layout | SQLite via SettingsRepository | Permanent |

- There is no remote API for core Bible data. All data lives in the local SQLite database.
- Module resources (commentaries, dictionaries) are imported from files and stored locally.

---

## 11. Analytics & Telemetry

| Metric | Source | Purpose |
|--------|--------|---------|
| Active panes per session | WorkspaceComponent | Understand usage patterns |
| Module popularity | PaneRegistry open counts | Prioritize development |
| Search query patterns | Search queries (anonymized) | Improve FTS tuning |
| Crash reports | Platform crash handler | Stability monitoring |
| Cold start time | Bootstrap timestamps | Performance regression |

### Rules

- **No PII is collected.** All analytics are aggregate and anonymized.
- **Opt-out available** in Settings → Privacy.
- Analytics are **disabled by default** in debug builds.
- Crash reports include stack traces but no user content (notes, highlights, etc.).

---

## 12. Related Documents

| Document | Description |
|----------|-------------|
| [DATA_LAYER.md](DATA_LAYER.md) | Database schema, migrations, repositories, sync |
| [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) | Visual identity, theming, components, accessibility |
| [PLATFORM_STRATEGY.md](PLATFORM_STRATEGY.md) | Per-platform builds, adaptive shells, native deps |
| [MODULE_SYSTEM.md](MODULE_SYSTEM.md) | Pane registry, Verse Bus, workspace layout, module creation |
| [CODE_CONVENTIONS.md](CODE_CONVENTIONS.md) | Naming, file structure, Decompose patterns, imports |
| [GETTING_STARTED.md](GETTING_STARTED.md) | Environment setup, first build, contribution guide |
| [TESTING.md](TESTING.md) | Test pyramid, patterns, coverage targets |
| [CI_CD.md](CI_CD.md) | GitHub Actions, quality gates, release process |
| [SECURITY.md](SECURITY.md) | Threat model, data protection, input validation |
| [INDEX.md](INDEX.md) | Documentation hub and navigation |
