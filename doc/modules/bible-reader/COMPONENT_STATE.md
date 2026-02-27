# Bible Reader — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | File | Description |
|-----------|-------|------|-------------|
| `DefaultBibleReaderComponent` | Scoped (per pane) | `shared/.../features/biblereader/component/DefaultBibleReaderComponent.kt` | Main reader: chapter loading, verse selection, VerseBus |
| `DefaultTextComparisonComponent` | Scoped (per pane) | `shared/.../features/biblereader/component/DefaultTextComparisonComponent.kt` | Multi-version comparison sub-feature |

---

## 2. BibleReaderComponent

### 2.1 Interface

```kotlin
interface BibleReaderComponent {
    val state: StateFlow<BibleReaderState>
    fun onLoad()
    fun goToChapter(bookId: Int, chapter: Int)
    fun onVerseSelected(globalVerseId: Int)
    fun onVerseLongPressed(globalVerseId: Int)
    fun onBookChapterPickerRequested()
    fun onToggleComparisonMode()
}
```

### 2.2 State

```kotlin
data class BibleReaderState(
    val isLoading: Boolean = true,
    val currentBible: Bible? = null,
    val currentBook: Book? = null,
    val currentChapter: Int = 1,
    val verses: List<Verse> = emptyList(),
    val selectedVerseId: Int? = null,
    val scrollPosition: Int = 0,
    val availableBibles: List<Bible> = emptyList(),
    val isComparisonMode: Boolean = false,
    val error: AppError? = null,
)
```

| Field | Type | Description |
|-------|------|-------------|
| `isLoading` | `Boolean` | Whether chapter data is being fetched |
| `currentBible` | `Bible?` | Active Bible version |
| `currentBook` | `Book?` | Currently displayed book |
| `currentChapter` | `Int` | Chapter number being displayed |
| `verses` | `List<Verse>` | Loaded verse content for the chapter |
| `selectedVerseId` | `Int?` | `global_verse_id` of the tapped verse, or null |
| `scrollPosition` | `Int` | LazyColumn scroll offset for state restoration |
| `availableBibles` | `List<Bible>` | All installed Bible versions (for picker) |
| `isComparisonMode` | `Boolean` | Whether text comparison view is active |
| `error` | `AppError?` | Error state, or null |

### 2.3 State Transitions

```
Initial (isLoading=true)
  │
  │ onLoad()
  ▼
Loading (isLoading=true, fetching default Bible + chapter)
  │
  ├── success ──→ Content (verses populated, isLoading=false)
  │                │
  │                ├── goToChapter(bookId, ch) ──→ Loading ──→ Content
  │                ├── onVerseSelected(id) ──→ Content (selectedVerseId set, VerseBus published)
  │                ├── onVerseLongPressed(id) ──→ Content (selection range mode)
  │                ├── onToggleComparisonMode() ──→ Content (isComparisonMode toggled)
  │                ├── Receives VerseBus VerseSelected ──→ Content (scroll to verse)
  │                └── Receives VerseBus PassageSelected ──→ Loading ──→ Content (new range)
  │
  └── failure ──→ Error (error set)
                   │
                   └── onLoad() (retry) ──→ Loading
```

---

## 3. TextComparisonComponent

### 3.1 Interface

```kotlin
interface TextComparisonComponent {
    val state: StateFlow<TextComparisonState>
    fun loadComparison(globalVerseId: Int, bibleIds: List<Long>)
    fun onDisplayModeChanged(mode: ComparisonDisplayMode)
}

enum class ComparisonDisplayMode { Parallel, Interleaved }
```

### 3.2 State

```kotlin
data class TextComparisonState(
    val isLoading: Boolean = false,
    val comparisons: List<VersionComparison> = emptyList(),
    val displayMode: ComparisonDisplayMode = ComparisonDisplayMode.Parallel,
    val selectedBibles: List<Bible> = emptyList(),
    val error: AppError? = null,
)
```

---

## 4. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onVerseSelected` | VerseBus publish | Publishes `LinkEvent.VerseSelected(globalVerseId)` to all subscribed panes |
| `onVerseLongPressed` | VerseBus publish | Publishes verse range for highlight creation |
| `goToChapter` | DB query | Loads verses from `BibleRepository.getVerses()` |
| `onLoad` | DB query + settings read | Fetches default Bible from settings, loads first chapter |
| VerseBus `VerseSelected` received | Scroll animation | `LazyColumn.animateScrollToItem()` to target verse |
| VerseBus `PassageSelected` received | DB query | Loads verse range via `getVerseRange()` |
| Error catch | Logging | Logs error via `Napier.e()` |

---

## 5. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `WorkspaceComponent` | ← Receives | Decompose child | Lifecycle managed by workspace; created/destroyed with pane |
| `VerseBus` | ↔ Bidirectional | `SharedFlow<LinkEvent>` | Publishes `VerseSelected` on tap; receives from other panes to scroll |
| `SettingsRepository` | → Reads | Koin DI | Reads font size, theme, default Bible preferences |
| `HighlightRepository` | → Reads | Koin DI | Loads highlight data for verse overlay rendering |
| `BookmarkRepository` | → Reads | Koin DI | Checks bookmark status per verse |

---

## 6. Component Registration (Koin)

```kotlin
val bibleReaderModule = module {
    factory<BibleReaderComponent> { (componentContext: ComponentContext) ->
        DefaultBibleReaderComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get(),
            settingsRepository = get(),
        )
    }
    factory<TextComparisonComponent> { (componentContext: ComponentContext) ->
        DefaultTextComparisonComponent(
            componentContext = componentContext,
            repository = get(),
        )
    }
}
```

---

## 7. Testing

```kotlin
@Test
fun `onLoad emits loading then content with default chapter`() = runTest {
    val fakeRepo = FakeBibleRepository(
        bibles = listOf(kjvBible),
        verses = genesisChapter1Verses,
    )
    val component = DefaultBibleReaderComponent(
        componentContext = TestComponentContext(),
        repository = fakeRepo,
        verseBus = VerseBus(),
        settingsRepository = FakeSettingsRepository(defaultBibleId = 1L),
    )
    component.state.test {
        component.onLoad()
        assertThat(awaitItem().isLoading).isTrue()
        val content = awaitItem()
        assertThat(content.isLoading).isFalse()
        assertThat(content.verses).hasSize(31) // Genesis 1 has 31 verses
    }
}

@Test
fun `onVerseSelected publishes to VerseBus`() = runTest {
    val verseBus = VerseBus()
    val component = DefaultBibleReaderComponent(
        componentContext = TestComponentContext(),
        repository = FakeBibleRepository(),
        verseBus = verseBus,
        settingsRepository = FakeSettingsRepository(),
    )
    verseBus.events.test {
        component.onVerseSelected(globalVerseId = 01001001)
        val event = awaitItem()
        assertThat(event).isInstanceOf(LinkEvent.VerseSelected::class)
        assertThat((event as LinkEvent.VerseSelected).globalVerseId).isEqualTo(01001001)
    }
}
```
