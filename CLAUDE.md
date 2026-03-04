# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Compile (quick check for errors)
./gradlew :composeApp:compileKotlinDesktop      # UI module
./gradlew :shared:compileKotlinDesktop           # shared business logic module
./gradlew compileKotlinDesktop                   # both modules

# Run desktop app
./gradlew :composeApp:run

# Run tests
./gradlew :shared:desktopTest                    # shared module unit tests
./gradlew :composeApp:desktopTest                # Compose UI tests
./gradlew desktopTest                            # all desktop tests

# Run a single test class
./gradlew :shared:desktopTest --tests "org.biblestudio.features.bible_reader.data.repositories.BibleRepositoryImplTest"

# Linting
./gradlew detekt                                 # static analysis
./gradlew ktlintCheck                            # formatting check
./gradlew ktlintFormat                           # auto-format

# Full check (compile + tests + detekt + ktlint)
./gradlew check

# SQLDelight code generation (runs automatically on build)
./gradlew :shared:generateCommonMainBibleStudioDatabaseInterface

# Coverage
./gradlew koverReport
```

## Environment Setup

- Requires JDK 21 (project uses IntelliJ JBR 21). JVM target is Java 17 (Desktop + Android)
- On Windows Git Bash, set JAVA_HOME before running gradlew:
  ```bash
  JAVA_HOME="/c/path/to/jbr" PATH="$JAVA_HOME/bin:$PATH" ./gradlew <task>
  ```

## Project Structure

Two Gradle modules: `:shared` (KMP library) and `:composeApp` (Compose Multiplatform UI). Desktop JVM target is named `desktop` (defined via `jvm("desktop")`). Android targets: minSdk 24, targetSdk 34.

Key dependency versions (see `gradle/libs.versions.toml`): Kotlin 2.1.0, Compose Multiplatform 1.7.3, SQLDelight 2.0.2, Decompose 3.2.2, Koin 3.5.6.

### shared module — business logic
- `features/` — feature-first organization, each with `domain/`, `data/`, `component/` layers
- `core/` — VerseBus, PaneRegistry, navigation, database, utilities
- `di/` — Koin modules: CoreModule (DB, VerseBus), RepositoryModule, ComponentModule
- `sqldelight/` — `.sq` query files and numbered `.sqm` migrations (schema derived from migrations)

### composeApp module — UI
- `ui/panes/` — 22 pane Composables (BibleReaderPane, SearchPane, etc.)
- `ui/workspace/` — LayoutNodeRenderer, PaneContent, WorkspaceShell, CommandPalette
- `ui/theme/` — design tokens, animations, CompositionLocals
- `desktopMain/` — entry point (Main.kt) with custom title bar

### data-pipeline — Python scripts
- `python download.py` downloads open-source Bible data into `raw/`
- `python normalize.py` normalizes into `output/biblestudio-seed.db`

## Architecture

**Pane system**: PaneRegistry maps type strings (e.g. `"bible-reader"`) to builder callbacks. WorkspaceComponent manages a `LayoutNode` tree (Leaf/Split/Tabs). LayoutNodeRenderer recursively renders it. PaneContent.kt dispatches pane type strings to actual Composables, creating a standalone `ComponentContext` + `LifecycleRegistry` per pane instance.

**Decompose components**: Each feature has a `Component` interface and `Default*Component` implementation. Components take `ComponentContext` + repositories + VerseBus, expose state via `StateFlow`. Registered as Koin factories in ComponentModule.

**VerseBus**: `SharedFlow<LinkEvent>` (replay=1) for cross-pane communication. LinkEvent subtypes: VerseSelected, StrongsSelected, PassageSelected, ResourceSelected, SearchResult.

**Verse ID encoding (BBCCCVVV)**: `book * 1_000_000 + chapter * 1_000 + verse`. Example: Genesis 1:1 = 1_001_001.

**Dependency direction**: `presentation → domain ← data`. Features must not import from each other — shared code goes in `core/`.

**SQLDelight**: Schema derived from migrations (`deriveSchemaFromMigrations = true`). Never modify existing `.sqm` files, only add new ones. Current DATABASE_VERSION is in `shared/.../AppInfo.kt`.

## Code Conventions

- Feature directories: `snake_case`. Kotlin files: `UpperCamelCase.kt`
- Component pattern: `FooComponent` (interface) + `DefaultFooComponent` (impl) + `FooState` (data class)
- Repository pattern: `FooRepository` (interface in domain/) + `FooRepositoryImpl` (in data/)
- Implementations are `internal`, interfaces are `public`
- No wildcard imports. Max line length 120. Trailing commas on multi-line parameter lists
- SQLDelight queries use `lowerCamelCase:` named parameters (`:paramName` not `?`)
- Composables: `UpperCamelCase`, accept `modifier: Modifier = Modifier`, use `collectAsState()` for StateFlow
- Tests use backtick-quoted names: `` `loadChapter emits loading then loaded`() ``
- Detekt enforces zero-tolerance (`maxIssues = 0`). Function length max: 60 lines, cyclomatic complexity max: 15
- `@Composable` functions are exempt from the lowercase naming rule

## Testing

- Tests live in `desktopTest/` source sets (JVM). Frameworks: kotlin.test, JUnit 5, Turbine (Flow assertions), Compose UI Test
- Component tests: mock repositories with real in-memory SQLite via JdbcSqliteDriver
- Compose UI tests: `runComposeUiTest { setContent { ... } }` pattern
- Coverage target: 80% overall (enforced in CI)
