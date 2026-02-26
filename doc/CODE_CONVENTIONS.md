# Code Conventions

> BibleStudio — Project Structure, Naming, Patterns & Import Rules (Kotlin Multiplatform)

---

## 1. Project Structure

BibleStudio follows a **feature-first** layout inside a Kotlin Multiplatform project. Each feature owns its domain, data, and presentation layers internally. Shared infrastructure lives in `core/` and utility code in `shared/`.

```
BibleStudio/
├── build.gradle.kts                          # Root Gradle config
├── settings.gradle.kts                       # Module declarations
├── gradle.properties                         # KMP & Compose flags
├── shared/
│   ├── build.gradle.kts                      # KMP shared module
│   └── src/
│       ├── commonMain/kotlin/org/biblestudio/
│       │   ├── App.kt                        # Root @Composable entry
│       │   ├── Bootstrap.kt                  # initKoin(), registry init
│       │   ├── di/
│       │   │   ├── CoreModule.kt             # Koin: DB, VerseBus, PaneRegistry
│       │   │   ├── RepositoryModule.kt       # Koin: repo bindings
│       │   │   └── ComponentModule.kt        # Koin: Decompose component factories
│       │   ├── core/
│       │   │   ├── database/
│       │   │   │   ├── DriverFactory.kt      # expect fun createSqlDriver()
│       │   │   │   └── DatabaseProvider.kt   # BibleStudioDatabase singleton
│       │   │   ├── verse_bus/
│       │   │   │   ├── VerseBus.kt
│       │   │   │   └── LinkEvent.kt          # sealed class
│       │   │   ├── theme/
│       │   │   │   └── AppTheme.kt
│       │   │   ├── navigation/
│       │   │   │   └── RootComponent.kt      # Decompose ChildStack
│       │   │   ├── error/
│       │   │   │   └── AppError.kt           # sealed class
│       │   │   └── extensions/
│       │   │       ├── ContextExtensions.kt
│       │   │       └── StringExtensions.kt
│       │   ├── features/
│       │   │   ├── bible_reader/
│       │   │   │   ├── domain/
│       │   │   │   │   ├── entities/
│       │   │   │   │   │   └── Verse.kt      # data class
│       │   │   │   │   └── repositories/
│       │   │   │   │       └── BibleRepository.kt
│       │   │   │   ├── data/
│       │   │   │   │   ├── repositories/
│       │   │   │   │   │   └── BibleRepositoryImpl.kt
│       │   │   │   │   └── mappers/
│       │   │   │   │       └── VerseMappers.kt
│       │   │   │   └── presentation/
│       │   │   │       ├── components/
│       │   │   │       │   ├── BibleReaderComponent.kt       # Interface
│       │   │   │       │   └── DefaultBibleReaderComponent.kt
│       │   │   │       ├── content/
│       │   │   │       │   └── BibleReaderContent.kt         # Root @Composable
│       │   │   │       └── composables/
│       │   │   │           ├── VerseItem.kt
│       │   │   │           └── ChapterHeader.kt
│       │   │   ├── search/
│       │   │   │   └── ... (same structure)
│       │   │   └── ... (21 features)
│       │   ├── shared/
│       │   │   ├── composables/
│       │   │   │   ├── PaneContainer.kt
│       │   │   │   ├── LoadingIndicator.kt
│       │   │   │   └── ErrorView.kt
│       │   │   ├── utils/
│       │   │   │   └── GlobalVerseId.kt
│       │   │   └── constants/
│       │   │       └── AppConstants.kt
│       │   └── workspace/
│       │       ├── WorkspaceComponent.kt
│       │       ├── WorkspaceState.kt
│       │       ├── LayoutNode.kt             # sealed class
│       │       └── PaneRegistry.kt
│       ├── commonTest/                       # Shared tests
│       ├── androidMain/kotlin/org/biblestudio/
│       │   └── core/database/
│       │       └── DriverFactory.android.kt  # actual fun createSqlDriver()
│       ├── iosMain/kotlin/org/biblestudio/
│       │   └── core/database/
│       │       └── DriverFactory.ios.kt
│       └── jvmMain/kotlin/org/biblestudio/
│           └── core/database/
│               └── DriverFactory.jvm.kt
├── sqldelight/
│   └── org/biblestudio/
│       ├── Bible.sq
│       ├── Annotation.sq
│       ├── Study.sq
│       ├── Reference.sq
│       ├── Resource.sq
│       ├── Writing.sq
│       ├── Settings.sq
│       └── Search.sq
├── androidApp/
│   ├── build.gradle.kts
│   └── src/main/kotlin/org/biblestudio/android/
│       └── MainActivity.kt
├── iosApp/
│   └── iosApp/
│       └── ContentView.swift
└── desktopApp/
    ├── build.gradle.kts
    └── src/jvmMain/kotlin/org/biblestudio/desktop/
        └── Main.kt
```

---

## 2. Naming Conventions

### 2.1 Files

| Type | Convention | Example |
|------|-----------|---------|
| Kotlin files | `UpperCamelCase.kt` | `BibleReaderComponent.kt` |
| Feature directories | `snake_case` | `word_study/` |
| SQLDelight files | `UpperCamelCase.sq` | `Bible.sq` |
| Migration files | `<version>.sqm` | `2.sqm` |
| Test files | `*Test.kt` | `BibleReaderComponentTest.kt` |
| Gradle files | `build.gradle.kts` | — |

### 2.2 Kotlin Symbols

| Type | Convention | Example |
|------|-----------|---------|
| Classes / interfaces | `UpperCamelCase` | `BibleReaderComponent` |
| Objects | `UpperCamelCase` | `AppConstants` |
| Enums | `UpperCamelCase` | `PaneCategory` |
| Enum entries | `UpperCamelCase` | `PaneCategory.Study` |
| Functions / methods | `lowerCamelCase` | `getVerses()` |
| Properties / parameters | `lowerCamelCase` | `globalVerseId` |
| Constants (`const val`) | `UPPER_SNAKE_CASE` | `MAX_PANES_PER_WORKSPACE` |
| Top-level vals | `lowerCamelCase` | `coreModule` |
| Private members | `_lowerCamelCase` or `private` | `private val _state` |
| Type parameters | `T`, `E`, `K`, `V` | `Result<T>` |
| Composable functions | `UpperCamelCase` | `@Composable fun VerseItem()` |

### 2.3 SQLDelight

| Type | Convention | Example |
|------|-----------|---------|
| `.sq` file | `UpperCamelCase.sq` (query group) | `Bible.sq` |
| Table names | `snake_case` | `verses` |
| Column names | `snake_case` | `global_verse_id` |
| Named queries | `lowerCamelCase:` | `versesForChapter:` |
| Generated class | `<File>Queries` (auto) | `BibleQueries` |

### 2.4 Component Naming

| Component | Pattern | Example |
|-----------|---------|---------|
| Component interface | `<Feature>Component` | `BibleReaderComponent` |
| Default implementation | `Default<Feature>Component` | `DefaultBibleReaderComponent` |
| Root composable | `<Feature>Content` | `BibleReaderContent` |
| State data class | `<Feature>State` | `BibleReaderState` |
| Sub-composables | Descriptive noun | `VerseItem`, `ChapterHeader` |

---

## 3. Decompose Component Convention

Every feature's presentation layer follows the **Component + Content** pattern:

### 3.1 Component Interface

```kotlin
// presentation/components/BibleReaderComponent.kt
interface BibleReaderComponent {
    val state: StateFlow<BibleReaderState>

    fun loadChapter(bookId: Int, chapter: Int)
    fun selectVerse(globalVerseId: Int)
    fun nextChapter()
    fun previousChapter()
}
```

### 3.2 State Data Class

```kotlin
// presentation/components/BibleReaderComponent.kt (same file or separate)
data class BibleReaderState(
    val loading: Boolean = false,
    val verses: List<Verse> = emptyList(),
    val bookId: Int = 0,
    val chapter: Int = 0,
    val selectedVerseId: Int? = null,
    val error: AppError? = null,
)
```

### 3.3 Default Implementation

```kotlin
// presentation/components/DefaultBibleReaderComponent.kt
class DefaultBibleReaderComponent(
    componentContext: ComponentContext,
    private val repository: BibleRepository,
    private val verseBus: VerseBus,
) : BibleReaderComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(BibleReaderState())
    override val state: StateFlow<BibleReaderState> = _state.asStateFlow()

    private val scope = coroutineScope(Dispatchers.Main.immediate)

    init {
        scope.launch {
            verseBus.events.collect { event ->
                if (event is LinkEvent.VerseSelected) {
                    /* navigate to verse */
                }
            }
        }
    }

    override fun loadChapter(bookId: Int, chapter: Int) {
        scope.launch {
            _state.update { it.copy(loading = true) }
            repository.getVerses(bookId, chapter)
                .onSuccess { verses ->
                    _state.update {
                        it.copy(loading = false, verses = verses, bookId = bookId, chapter = chapter)
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false, error = error.toAppError()) }
                }
        }
    }

    override fun selectVerse(globalVerseId: Int) {
        _state.update { it.copy(selectedVerseId = globalVerseId) }
        verseBus.navigate(LinkEvent.VerseSelected(globalVerseId))
    }

    // ... other methods
}
```

### 3.4 Content Composable

```kotlin
// presentation/content/BibleReaderContent.kt
@Composable
fun BibleReaderContent(component: BibleReaderComponent) {
    val state by component.state.collectAsState()

    when {
        state.loading -> LoadingIndicator()
        state.error != null -> ErrorView(state.error!!)
        else -> VerseList(
            verses = state.verses,
            selectedVerseId = state.selectedVerseId,
            onVerseClick = { component.selectVerse(it.globalVerseId) },
        )
    }
}
```

---

## 4. Entity Pattern

Domain entities are immutable Kotlin `data class` types:

```kotlin
// domain/entities/Verse.kt
data class Verse(
    val id: Int,
    val globalVerseId: Int,
    val verseNumber: Int,
    val text: String,
    val htmlText: String? = null,
)
```

### Rules

- Entities live in `domain/entities/`.
- Always `data class` — never mutable classes. Use `copy()` for mutations.
- No SQLDelight types or SQL concepts in entities.
- Use value types (`Int`, `String`, `Boolean`, `Long`, `kotlinx.datetime.Instant`) — no `SqlDriver` or DB-specific types.
- Add `@Serializable` only when JSON export is needed.
- No code generation required — Kotlin `data class` provides `equals()`, `hashCode()`, `copy()`, `toString()` natively.

---

## 5. Import Rules

### 5.1 Order

Imports are organized in 3 groups, separated by blank lines:

```kotlin
// 1. Kotlin / Java stdlib
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 2. Third-party libraries (alphabetical)
import com.arkivanov.decompose.ComponentContext
import org.koin.core.module.dsl.singleOf

// 3. Project imports (alphabetical)
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.bible_reader.domain.entities.Verse
```

### 5.2 Rules

- **Use fully qualified package imports** — no wildcard `*` imports.
- **Never import across features** — if two features need to share code, elevate it to `core/` or `shared/`.
- **Never import `data/` from `domain/`** — the dependency arrow points inward only.
- **Never import `presentation/` from `domain/` or `data/`**.
- IntelliJ / Android Studio auto-organizes imports — rely on the IDE's Optimize Imports action.

### 5.3 Dependency Direction

```
presentation → domain ← data
      ↓                   ↓
    shared              core
```

- `presentation` can import `domain` and `shared`.
- `data` can import `domain` and `core`.
- `domain` imports nothing from the project (only `kotlinx.*`, stdlib).
- `shared` and `core` are foundation layers with no upward dependencies.

---

## 6. Module Visibility

Kotlin provides `internal` visibility for module-scoped encapsulation. Use it to restrict implementation details to the Gradle module boundary:

```kotlin
// Only visible within the :shared module
internal class BibleRepositoryImpl(
    private val database: BibleStudioDatabase,
) : BibleRepository { ... }
```

### Rules

- Repository implementations, mappers, and component default implementations are `internal`.
- Interfaces, entities, and component contracts are `public`.
- Koin wiring in `di/` modules references `internal` classes via the same Gradle module.
- No barrel/export files needed — Kotlin's `internal` and `public` modifiers handle this at the language level.

---

## 7. SQLDelight Conventions

### 7.1 File Organization

Each `.sq` file groups queries by domain area:

```
sqldelight/org/biblestudio/
├── Bible.sq          # Verses, books, chapters
├── Annotation.sq     # Highlights, bookmarks, notes
├── Study.sq          # Cross-references, word study, morphology
├── Reference.sq      # Cross-references, parallel passages
├── Resource.sq       # Commentaries, library entries
├── Writing.sq        # Sermon editor, document drafts
├── Settings.sq       # Preferences, workspace layouts
└── Search.sq         # FTS5 virtual tables, search queries
```

### 7.2 Query Naming

```sql
-- ✅ Good: descriptive lowerCamelCase with colon
versesForChapter:
SELECT * FROM verses
WHERE book_id = :bookId AND chapter = :chapter
ORDER BY verse_number;

-- ❌ Bad: vague name, no colon syntax
get_data:
SELECT * FROM verses WHERE book_id = ?;
```

### 7.3 Rules

- Use named parameters (`:paramName`) over positional (`?`).
- Use `CREATE INDEX` for columns used in `WHERE` and `JOIN`.
- Use `CREATE VIRTUAL TABLE ... USING fts5()` for full-text search only.
- Keep FTS tables in sync via `INSERT ... SELECT` triggers or repository logic.
- Migration files: `1.sqm`, `2.sqm`, etc. — Never modify existing migrations, only add new ones.

---

## 8. Composable Conventions

### 8.1 Pure Composables by Default

Prefer stateless composables that receive all data as parameters. Components handle all domain state via `StateFlow`.

### 8.2 Naming

- Composable functions use `UpperCamelCase` (they are conceptually "UI components").
- Composable functions that return `Unit` have noun names: `VerseItem()`, `ChapterHeader()`.
- Composable functions that return values use descriptive names: `rememberScrollState()`.

### 8.3 Composable Size

- Keep composable functions under 80 lines. Extract sub-composables for complex layouts.
- One top-level composable per file. Private helpers prefixed with nothing special (they are `private`).

### 8.4 State Collection

```kotlin
// Collect StateFlow inside composables
val state by component.state.collectAsState()
```

- Use `collectAsState()` for `StateFlow` — this integrates with Compose recomposition.
- Use `LaunchedEffect` for one-time side effects scoped to composition.
- Use `remember` / `rememberSaveable` for local UI state (scroll position, text field input).

### 8.5 Modifier Chain

- Always accept a `modifier: Modifier = Modifier` parameter as the first modifier parameter.
- Apply the passed modifier first, then internal modifiers:

```kotlin
@Composable
fun VerseItem(
    verse: Verse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Spacing.md),
    ) {
        // ...
    }
}
```

---

## 9. Code Style & Linting

### 9.1 Detekt Configuration

```yaml
# detekt.yml (abbreviated)
complexity:
  LongMethod:
    threshold: 60
  LongParameterList:
    functionThreshold: 8
    constructorThreshold: 10
  TooManyFunctions:
    thresholdInFiles: 20
    thresholdInClasses: 15

naming:
  FunctionNaming:
    functionPattern: '[a-z][a-zA-Z0-9]*'
    # Composable functions are excluded via @Composable annotation
    excludeClassPattern: 'Composable'
  TopLevelPropertyNaming:
    constantPattern: '[A-Z][A-Z_0-9]*'

style:
  WildcardImport:
    active: true
  MaxLineLength:
    maxLineLength: 120
  UnusedPrivateMember:
    active: true
```

### 9.2 Formatting

- **ktlint** or IntelliJ default Kotlin code style with max line length 120.
- Trailing commas on all multi-line parameter lists.
- Use expression body (`= ...`) for single-expression functions.
- Use `when` expressions over `if/else if` chains for 3+ branches.

### 9.3 Gradle Lint Tasks

```bash
# Run detekt analysis
./gradlew detekt

# Run ktlint check
./gradlew ktlintCheck

# Auto-format
./gradlew ktlintFormat
```

---

## 10. Related Documents

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System layers, DI, data flow |
| [MODULE_SYSTEM.md](MODULE_SYSTEM.md) | Module creation guide, PaneRegistry |
| [TESTING.md](TESTING.md) | Test file structure, naming |
| [GETTING_STARTED.md](GETTING_STARTED.md) | Setup, build commands |
