# Search — Component & State

> Decompose components, StateFlow state management, and side effects.

---

## 1. Components

| Component | Scope | File | Description |
|-----------|-------|------|-------------|
| `DefaultSearchComponent` | Scoped (per pane) | `shared/.../features/search/component/DefaultSearchComponent.kt` | Full-text search with debounce, history, filters |

---

## 2. SearchComponent

### 2.1 Interface

```kotlin
interface SearchComponent {
    val state: StateFlow<SearchState>
    fun onQueryChanged(query: String)
    fun onSearch()
    fun onResultSelected(result: SearchResult)
    fun onFilterChanged(filter: SearchFilter)
    fun onClearHistory()
}
```

### 2.2 State

```kotlin
data class SearchState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val resultCount: Int = 0,
    val filters: SearchFilter = SearchFilter(),
    val history: List<SearchHistoryEntry> = emptyList(),
    val error: AppError? = null,
)

data class SearchFilter(
    val scope: SearchScope = SearchScope.All,
    val bookRange: IntRange? = null,
    val testament: Testament? = null,
)

enum class SearchScope { All, Bible, Notes, Resources, Lexicon, Sermons }
enum class Testament { OT, NT }
```

| Field | Type | Description |
|-------|------|-------------|
| `query` | `String` | Current search input text |
| `results` | `List<SearchResult>` | Ranked search results |
| `isSearching` | `Boolean` | Whether a search is in progress |
| `resultCount` | `Int` | Total results found |
| `filters` | `SearchFilter` | Active scope, book range, testament filters |
| `history` | `List<SearchHistoryEntry>` | Recent searches (last 20) |
| `error` | `AppError?` | Error state, or null |

### 2.3 State Transitions

```
Initial (empty query, no results)
  │
  │ onQueryChanged(query)
  ▼
Typing (query updated, debounce timer starts)
  │
  │ 300ms elapsed (debounce)
  ▼
Searching (isSearching=true)
  │
  ├── success ──→ Results (results populated, resultCount set)
  │                │
  │                ├── onResultSelected(r) ──→ Results (VerseBus published)
  │                ├── onQueryChanged(q) ──→ Typing (new debounce)
  │                └── onFilterChanged(f) ──→ Searching (re-query with new filter)
  │
  └── failure ──→ Error (error set)
```

---

## 3. Side Effects

| Action | Side Effect | Description |
|--------|------------|-------------|
| `onResultSelected` | VerseBus publish | Publishes `LinkEvent.SearchResult(globalVerseId)` |
| Debounce elapsed | DB query | Executes FTS5 search across scoped tables |
| `onSearch` | History insert | Saves query to `search_history` table |
| Error catch | Logging | Logs via `Napier.e()` |

---

## 4. Interaction with Other Components

| External Component | Direction | Mechanism | Description |
|-------------------|-----------|-----------|-------------|
| `WorkspaceComponent` | ← Receives | Decompose child | Lifecycle managed by workspace |
| `VerseBus` | → Publishes | `SharedFlow<LinkEvent>` | Publishes `SearchResult` on result tap |
| `BibleReaderComponent` | → Triggers | VerseBus | Reader receives `SearchResult` and scrolls to verse |

---

## 5. Component Registration (Koin)

```kotlin
val searchModule = module {
    factory<SearchComponent> { (componentContext: ComponentContext) ->
        DefaultSearchComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get(),
        )
    }
}
```

---

## 6. Testing

```kotlin
@Test
fun `search returns ranked results`() = runTest {
    val component = DefaultSearchComponent(
        componentContext = TestComponentContext(),
        repository = FakeSearchRepository(testResults),
        verseBus = VerseBus(),
    )
    component.state.test {
        component.onQueryChanged("God created")
        advanceTimeBy(300) // debounce
        val state = awaitItem()
        assertThat(state.results).isNotEmpty()
        assertThat(state.results.first().snippet).contains("God")
    }
}
```
