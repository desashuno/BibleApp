# Bible Reader — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
┌───────────────────────────────────────────────────┐
│                       UI                          │
│  BibleReaderPane / BibleReaderContent             │
│  TextComparisonPane                               │
│  BookChapterPicker                                │
│  └── Observes Component.state (StateFlow)         │
├───────────────────────────────────────────────────┤
│                     LOGIC                         │
│  DefaultBibleReaderComponent (Decompose)          │
│  ├── Manages StateFlow<BibleReaderState>          │
│  ├── Publishes/subscribes VerseBus events         │
│  └── Calls BibleRepository methods                │
│                                                   │
│  TextComparisonComponent (Decompose)              │
│  ├── Manages StateFlow<TextComparisonState>       │
│  └── Calls TextComparisonRepository methods       │
├───────────────────────────────────────────────────┤
│                      DATA                         │
│  BibleRepository (interface)                      │
│  BibleRepositoryImpl                              │
│  TextComparisonRepository / Impl                  │
│  └── BibleQueries (SQLDelight)                    │
│       └── SQLite (bibles, books, chapters, verses)│
└───────────────────────────────────────────────────┘
```

---

## 2. Internal Data Flow

```
User opens Bible Reader
  → DefaultBibleReaderComponent.onLoad()
    → coroutineScope.launch { repository.getVerses(bookId, chapter) }
      → BibleQueries.versesForChapter executes (< 10 ms)
    → _state.update { it.copy(verses = result, isLoading = false) }
  → BibleReaderPane recomposes via StateFlow collection
```

### 2.1 Primary Flow — Chapter Loading

1. **User opens chapter** — Selects book/chapter from picker or navigates with swipe gesture.
2. **Component loads verses** — `BibleReaderComponent.goToChapter(bookId, chapter)` calls `BibleRepository.getVerses()`.
3. **State updates** — Verses are set in `BibleReaderState.verses`; UI recomposes with `LazyColumn`.
4. **Verse rendering** — Each verse renders with superscript verse number, text, and optional highlight overlay.

### 2.2 Secondary Flows

- **Verse selection** — User taps verse → component publishes `LinkEvent.VerseSelected(globalVerseId)` to VerseBus → all subscribed panes update.
- **Long-press selection** — User long-presses → selection range activates → triggers highlight creation flow via `highlights` module.
- **Incoming verse event** — VerseBus delivers `VerseSelected` from another pane → component calls `scrollToVerse(globalVerseId)` → `LazyColumn` animates to target verse.
- **Passage navigation** — VerseBus delivers `PassageSelected` → component loads passage range via `getVerseRange(startId, endId)`.
- **Text comparison** — User toggles comparison mode → `TextComparisonComponent` loads same passage from multiple Bible IDs → renders parallel or interleaved view.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Bible.sq` | `allBibles` | — | `List<Bibles>` | All installed Bible versions |
| `Bible.sq` | `versesForChapter` | `bookId: Int, chapter: Int` | `List<Verses>` | Chapter content for display |
| `Bible.sq` | `verseByGlobalId` | `globalVerseId: Int` | `Verses?` | Single verse lookup |
| `Bible.sq` | `versesInRange` | `startId: Int, endId: Int` | `List<Verses>` | Passage range for cross-ref navigation |
| `Bible.sq` | `searchVerses` | `query: String, maxResults: Long` | `List<Verses>` | FTS5 full-text search |
| `Bible.sq` | `insertBible` | `abbreviation, name, ...` | `Unit` | Bible import |
| `Bible.sq` | `insertVerse` | `chapterId, globalVerseId, ...` | `Unit` | Verse import |

---

## 4. Dependency Injection

```kotlin
val bibleReaderModule = module {
    singleOf(::BibleRepositoryImpl) bind BibleRepository::class
    singleOf(::TextComparisonRepositoryImpl) bind TextComparisonRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultBibleReaderComponent(
            componentContext = ctx,
            repository = get(),
            verseBus = get(),
            settingsRepository = get(),
        )
    }
    factory { (ctx: ComponentContext) ->
        DefaultTextComparisonComponent(
            componentContext = ctx,
            repository = get(),
        )
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `BibleRepositoryImpl` | Abstracts SQLDelight queries behind interface for testing |
| Component (Decompose) | `DefaultBibleReaderComponent` | Lifecycle-aware state management with `ComponentContext` |
| StateFlow | Component → UI | Reactive unidirectional data flow; UI collects state |
| Mapper Extensions | `VersesForChapter.toVerse()` | Clean separation between DB rows and domain entities |
| Event Bus (VerseBus) | `LinkEvent.VerseSelected` | Decoupled cross-pane communication |

---

## 6. Performance Considerations

- **Chapter loading < 10 ms**: `versesForChapter` query uses composite index `idx_verses_chapter(chapter_id, verse_number)` and executes on `Dispatchers.IO`.
- **Scroll-to-verse**: `LazyColumn.animateScrollToItem()` targets the verse index without re-querying.
- **FTS5 search**: `searchVerses` leverages BM25 ranking with `LIMIT` clause, targeting < 50 ms.
- **Bible import**: Batch INSERT inside `transaction {}` block; ~30,000 verses for KJV in < 30 s.
- **Reactive bibles list**: `watchBibles()` uses SQLDelight's `asFlow().mapToList()` for automatic recomposition on module install.
- **Memory**: Only one chapter's verses are held in state at a time; previous chapters are unloaded.

---

## 7. Design Decisions

| Decision | Alternatives considered | Justification |
|----------|------------------------|---------------|
| `BBCCCVVV` integer verse IDs | String IDs (`"Gen.1.1"`), composite keys | Integer enables efficient range queries, sorting, and indexing |
| Single `BibleQueries` for Bible + verses | Separate `.sq` per table | Bible data is always accessed together; single query group reduces complexity |
| `LazyColumn` for verse rendering | `RecyclerView` (Android only), `ScrollableColumn` | KMP-compatible, efficient for ~30–180 items per chapter |
| FTS5 for text search | Manual `LIKE` queries, external search engine | Native SQLite FTS5 is fast, zero-dependency, supports ranking |
| Text comparison as sub-component | Separate module | Comparison is a reader mode, not an independent study tool |
