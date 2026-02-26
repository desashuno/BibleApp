# Testing

> BibleStudio — Test Pyramid, Patterns, Tools & Coverage Targets (Kotlin Multiplatform)

---

## 1. Testing Pyramid

```
          ┌─────────────┐
          │  Integration │   Few — full Compose screens, multi-component flows
          ├─────────────┤
          │  Compose UI  │   Medium — individual composable rendering
          ├─────────────┤
          │    Unit      │   Many — Components, repositories, queries, mappers
          └─────────────┘
```

| Layer | Count | Speed | Isolation |
|-------|-------|-------|-----------|
| **Unit** | High | < 1 ms each | Full (mocked deps) |
| **Compose UI** | Medium | < 100 ms each | Partial (test rule) |
| **Integration** | Low | Seconds | None (real DB, real components) |

---

## 2. Tools

| Library | Purpose |
|---------|---------|
| `kotlin.test` | Core assertions and test annotations |
| `JUnit 5` | JVM test runner |
| `Turbine` | Flow / StateFlow assertion DSL |
| `MockK` | Kotlin-idiomatic mocking |
| `SQLDelight` (JdbcSqliteDriver) | In-memory database for query tests |
| `Compose UI Test` | Composable rendering and interaction tests |

### Setup in `build.gradle.kts` (shared module)

```kotlin
kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.turbine)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter)
                implementation(libs.mockk)
                implementation(libs.sqldelight.jvm.driver) // JdbcSqliteDriver for in-memory tests
                implementation(libs.compose.ui.test.junit4)
            }
        }
    }
}
```

---

## 3. Test File Structure

Tests mirror the source structure under `commonTest/` and `jvmTest/`:

```
shared/src/
├── commonTest/kotlin/org/biblestudio/
│   ├── core/
│   │   └── verse_bus/
│   │       └── VerseBusTest.kt
│   ├── features/
│   │   ├── bible_reader/
│   │   │   ├── domain/
│   │   │   │   └── entities/
│   │   │   │       └── VerseTest.kt
│   │   │   ├── data/
│   │   │   │   ├── repositories/
│   │   │   │   │   └── BibleRepositoryImplTest.kt
│   │   │   │   └── mappers/
│   │   │   │       └── VerseMappersTest.kt
│   │   │   └── presentation/
│   │   │       └── components/
│   │   │           └── DefaultBibleReaderComponentTest.kt
│   │   └── search/
│   │       └── ...
│   ├── shared/
│   │   └── utils/
│   │       └── GlobalVerseIdTest.kt
│   └── workspace/
│       ├── WorkspaceComponentTest.kt
│       └── LayoutNodeTest.kt
└── jvmTest/kotlin/org/biblestudio/
    ├── core/database/
    │   ├── queries/
    │   │   ├── BibleQueriesTest.kt
    │   │   └── AnnotationQueriesTest.kt
    │   └── migrations/
    │       └── MigrationTest.kt
    └── features/
        └── bible_reader/
            └── presentation/
                └── composables/
                    └── VerseItemTest.kt
```

---

## 4. Test Patterns

### 4.1 Component Test (with Turbine)

```kotlin
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultBibleReaderComponentTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<BibleRepository>()
    private val verseBus = mockk<VerseBus>(relaxed = true)
    private val busEvents = MutableSharedFlow<LinkEvent>()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { verseBus.events } returns busEvents
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadChapter emits loading then loaded`() = runTest {
        val verses = listOf(
            Verse(id = 1, globalVerseId = 1001001, verseNumber = 1, text = "In the beginning..."),
            Verse(id = 2, globalVerseId = 1001002, verseNumber = 2, text = "And the earth was..."),
        )
        coEvery { repository.getVerses(1, 1) } returns Result.success(verses)

        val component = createComponent()

        component.state.test {
            assertEquals(BibleReaderState(), awaitItem()) // initial

            component.loadChapter(1, 1)
            assertEquals(true, awaitItem().loading)       // loading
            val loaded = awaitItem()                      // loaded
            assertEquals(false, loaded.loading)
            assertEquals(2, loaded.verses.size)
            assertEquals(1, loaded.bookId)
        }
    }

    @Test
    fun `loadChapter emits error on failure`() = runTest {
        coEvery { repository.getVerses(1, 1) } returns
            Result.failure(Exception("Table not found"))

        val component = createComponent()

        component.state.test {
            awaitItem() // initial
            component.loadChapter(1, 1)
            awaitItem() // loading
            val errorState = awaitItem()
            assertEquals(false, errorState.loading)
            assertNotNull(errorState.error)
        }
    }

    private fun createComponent(): DefaultBibleReaderComponent {
        return DefaultBibleReaderComponent(
            componentContext = TestComponentContext(),
            repository = repository,
            verseBus = verseBus,
        )
    }
}
```

### 4.2 SQLDelight Query Test (In-Memory Database)

```kotlin
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BibleQueriesTest {
    private lateinit var driver: JdbcSqliteDriver
    private lateinit var database: BibleStudioDatabase
    private lateinit var queries: BibleQueries

    @BeforeTest
    fun setup() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BibleStudioDatabase.Schema.create(driver)
        database = BibleStudioDatabase(driver)
        queries = database.bibleQueries
    }

    @AfterTest
    fun tearDown() {
        driver.close()
    }

    @Test
    fun `versesForChapter returns ordered verses`() {
        // Seed test data
        queries.insertBible(abbreviation = "KJV", name = "King James Version", language = "en")
        queries.insertBook(bibleId = 1, bookNumber = 1, name = "Genesis", testament = "OT")
        queries.insertChapter(bookId = 1, chapterNumber = 1, verseCount = 31)
        queries.insertVerse(chapterId = 1, globalVerseId = 1001002, verseNumber = 2, text = "And the earth was...")
        queries.insertVerse(chapterId = 1, globalVerseId = 1001001, verseNumber = 1, text = "In the beginning...")

        val result = queries.versesForChapter(bookId = 1, chapter = 1).executeAsList()

        assertEquals(2, result.size)
        assertEquals(1, result[0].verse_number) // Ordered
        assertEquals(2, result[1].verse_number)
    }
}
```

### 4.3 Repository Test

```kotlin
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BibleRepositoryImplTest {
    private val database = mockk<BibleStudioDatabase>()
    private val queries = mockk<BibleQueries>()
    private val repository = BibleRepositoryImpl(database)

    init {
        coEvery { database.bibleQueries } returns queries
    }

    @Test
    fun `getVerses maps query rows to domain entities`() = runTest {
        val rows = listOf(
            VersesForChapter(
                id = 1, chapter_id = 1, global_verse_id = 1001001,
                verse_number = 1, text = "In the beginning...",
            ),
        )
        coEvery { queries.versesForChapter(1, 1).executeAsList() } returns rows

        val result = repository.getVerses(1, 1)

        assertTrue(result.isSuccess)
        val verses = result.getOrThrow()
        assertEquals(1, verses.size)
        assertEquals(1001001, verses[0].globalVerseId)
    }
}
```

### 4.4 Compose UI Test

```kotlin
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class VerseItemTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `displays verse number and text`() {
        val verse = Verse(
            id = 1,
            globalVerseId = 1001001,
            verseNumber = 1,
            text = "In the beginning God created the heaven and the earth.",
        )

        composeRule.setContent {
            AppTheme {
                VerseItem(verse = verse, onClick = {})
            }
        }

        composeRule.onNodeWithText("1").assertIsDisplayed()
        composeRule.onNodeWithText("In the beginning God created the heaven and the earth.")
            .assertIsDisplayed()
    }

    @Test
    fun `calls onClick when tapped`() {
        var clicked = false
        val verse = Verse(id = 1, globalVerseId = 1001001, verseNumber = 1, text = "Test")

        composeRule.setContent {
            AppTheme {
                VerseItem(verse = verse, onClick = { clicked = true })
            }
        }

        composeRule.onNodeWithText("Test").performClick()
        assert(clicked)
    }
}
```

### 4.5 Migration Test

```kotlin
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals

class MigrationTest {
    @Test
    fun `upgrade from v15 to v16 preserves data`() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

        // Create v15 schema
        BibleStudioDatabase.Schema.migrate(driver, oldVersion = 0, newVersion = 15)

        // Insert test data at v15 schema
        driver.execute(
            null,
            "INSERT INTO verses (chapter_id, global_verse_id, verse_number, text) " +
                "VALUES (1, 1001001, 1, 'In the beginning...')",
            0,
        )

        // Run migration to v16
        BibleStudioDatabase.Schema.migrate(driver, oldVersion = 15, newVersion = 16)

        // Verify data survived
        val cursor = driver.executeQuery(null, "SELECT * FROM verses", parameters = 0)
        cursor.next()
        assertEquals("In the beginning...", cursor.getString(3))

        driver.close()
    }
}
```

### 4.6 VerseBus Test

```kotlin
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VerseBusTest {
    @Test
    fun `navigate publishes event to collectors`() = runTest {
        val bus = VerseBus()

        bus.events.test {
            bus.navigate(LinkEvent.VerseSelected(1001001))
            assertEquals(LinkEvent.VerseSelected(1001001), awaitItem())
        }
    }

    @Test
    fun `current returns last published event`() = runTest {
        val bus = VerseBus()
        assertNull(bus.current)

        bus.navigate(LinkEvent.VerseSelected(1001001))
        assertEquals(LinkEvent.VerseSelected(1001001), bus.current)
    }

    @Test
    fun `new collector receives replayed event`() = runTest {
        val bus = VerseBus()
        bus.navigate(LinkEvent.VerseSelected(1001001))

        // New collector should immediately receive the last event
        bus.events.test {
            assertEquals(LinkEvent.VerseSelected(1001001), awaitItem())
        }
    }
}
```

---

## 5. Coverage Targets

| Scope | Target | Rationale |
|-------|--------|-----------|
| **Overall** | ≥ 80% | Industry standard for critical apps |
| **Components** | ≥ 90% | Components contain core business logic |
| **Repositories** | ≥ 85% | Data mapping must be correct |
| **SQLDelight Queries** | ≥ 80% | SQL query correctness |
| **Compose UI** | ≥ 70% | UI tests are slower, focus on interaction |
| **Entities** | 100% | Test `data class` `copy()`, `equals()`, serialization roundtrip |

### Coverage Commands

```bash
# Generate coverage report (Kover)
./gradlew koverReport

# View HTML report
open shared/build/reports/kover/html/index.html    # macOS
start shared/build/reports/kover/html/index.html   # Windows
```

### CI Coverage Gate

CI fails if overall line coverage drops below 80%. See [CI_CD.md](CI_CD.md).

---

## 6. Test Naming

```
<method_or_composable_under_test> <expected_behavior> [when <condition>]
```

Use backtick-quoted function names for readability:

```kotlin
@Test
fun `versesForChapter returns ordered verses`() { ... }

@Test
fun `loadChapter emits loading then loaded`() { ... }

@Test
fun `displays verse number and text`() { ... }

@Test
fun `upgrade from v15 to v16 preserves data`() { ... }
```

---

## 7. Mocking Guidelines

### 7.1 What to Mock

| Layer | Mock | Real |
|-------|------|------|
| Component tests | Repository, VerseBus | Component under test |
| Repository tests | Database/Queries | Repository under test, mappers |
| Query tests | — | Queries under test, in-memory DB |
| Compose UI tests | Component (interface mock) | Composable under test |

### 7.2 Mock Declaration

```kotlin
// MockK — no codegen needed
val repository = mockk<BibleRepository>()
val verseBus = mockk<VerseBus>(relaxed = true)
```

### 7.3 Rules

- **Never mock the class under test.**
- **Prefer `relaxed = true`** for mocks whose return values are not relevant to the assertion.
- **Use `coEvery` for suspend functions**, `every` for regular functions.
- **Use `verify` / `coVerify`** to assert interactions:
  ```kotlin
  coVerify { repository.getVerses(1, 1) }
  ```
- **Use `slot<T>()`** to capture arguments:
  ```kotlin
  val slot = slot<LinkEvent>()
  every { verseBus.navigate(capture(slot)) } returns Unit
  // ... action ...
  assertEquals(LinkEvent.VerseSelected(1001001), slot.captured)
  ```

### 7.4 TestComponentContext

For Decompose component tests, create a `TestComponentContext` utility:

```kotlin
fun TestComponentContext(): ComponentContext {
    return DefaultComponentContext(
        lifecycle = LifecycleRegistry(),
    )
}
```

---

## 8. Related Documents

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System layers that inform test boundaries |
| [CODE_CONVENTIONS.md](CODE_CONVENTIONS.md) | File naming, component patterns |
| [DATA_LAYER.md](DATA_LAYER.md) | SQLDelight queries, migration details |
| [CI_CD.md](CI_CD.md) | Automated test execution, coverage gates |
