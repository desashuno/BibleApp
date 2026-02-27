# Search — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
┌───────────────────────────────────────────────────┐
│                       UI                          │
│  SearchPane / SyntaxSearchPane (@Composable)      │
│  └── Observes Component.state (StateFlow)         │
├───────────────────────────────────────────────────┤
│                     LOGIC                         │
│  DefaultSearchComponent (Decompose)               │
│  ├── Manages StateFlow<SearchState>               │
│  ├── 300ms debounced search                       │
│  ├── Publishes SearchResult to VerseBus           │
│  └── Calls SearchRepository methods               │
├───────────────────────────────────────────────────┤
│                      DATA                         │
│  SearchRepository (interface)                     │
│  SearchRepositoryImpl                             │
│  └── SearchQueries (SQLDelight)                   │
│       └── FTS5: fts_verses, fts_notes,            │
│           fts_resources, fts_lexicon, fts_sermons  │
│       └── search_history table                    │
└───────────────────────────────────────────────────┘
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Text Search

1. **User types query** — Text input updates `SearchState.query`.
2. **Debounce (300ms)** — Component waits 300ms after last keystroke before executing.
3. **FTS5 search** — `SearchRepository.searchAll(query)` queries all 5 FTS tables with `MATCH` and `bm25()` ranking.
4. **Results populate** — State updates with ranked results; UI renders `LazyColumn` of snippets.
5. **Result tap** — Publishes `LinkEvent.SearchResult(globalVerseId)` → Bible Reader scrolls to verse.

### 2.2 Secondary Flows

- **Syntax search** — User writes morphology query (e.g. `[LEMMA:H1234]`) → parser tokenizes → AST → SQL builder combines morphology + FTS5 queries.
- **Search scope filtering** — User selects book range or testament → results filtered by `global_verse_id` range.
- **Search history** — Each executed search is saved to `search_history` (last 20); history dropdown on focus.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Search.sq` | `searchVersesFts` | `query, limit` | `List<SearchResult>` | FTS5 Bible text search |
| `Search.sq` | `searchNotesFts` | `query, limit` | `List<SearchResult>` | FTS5 notes search |
| `Search.sq` | `searchResourcesFts` | `query, limit` | `List<SearchResult>` | FTS5 commentary/dictionary search |
| `Search.sq` | `searchLexiconFts` | `query, limit` | `List<SearchResult>` | FTS5 lexicon search |
| `Search.sq` | `searchSermonsFts` | `query, limit` | `List<SearchResult>` | FTS5 sermon search |
| `Search.sq` | `recentSearches` | `limit` | `List<SearchHistory>` | Recent search history |
| `Search.sq` | `insertSearch` | `query, scope, count` | `Unit` | Save search to history |

---

## 4. Dependency Injection

```kotlin
val searchModule = module {
    singleOf(::SearchRepositoryImpl) bind SearchRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultSearchComponent(
            componentContext = ctx,
            repository = get(),
            verseBus = get(),
        )
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `SearchRepositoryImpl` | Abstracts FTS5 queries behind interface |
| Debounce | `DefaultSearchComponent` | Prevent excessive DB queries during typing |
| Strategy | `SearchSource` enum | Different FTS tables queried via same interface |
| Parser (Syntax Search) | Tokenizer → AST → SQL Builder | Morphology-aware search grammar |

---

## 6. Performance Considerations

- **FTS5 + BM25 ranking** < 50 ms per query with `LIMIT` clause.
- **Debounce (300ms)** prevents query flood during rapid typing.
- **Parallel FTS queries**: All 5 FTS tables can be queried concurrently via `async {}` and combined.
- **History limit**: Capped at 20 entries; older entries auto-pruned on insert.

---

## 7. Design Decisions

| Decision | Alternatives considered | Justification |
|----------|------------------------|---------------|
| FTS5 for all search | Lucene, Elasticsearch, manual LIKE | Zero external dependencies; native SQLite; BM25 ranking |
| 300ms debounce | No debounce, 500ms | Balance between responsiveness and query reduction |
| Combined result list | Separate tabs per source | Single ranked list is more useful; source badges distinguish origin |
| Custom syntax search grammar | Regex-based search, CQL | Purpose-built for biblical morphology queries |
