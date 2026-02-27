# Word Study — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
┌───────────────────────────────────────────────────┐
│                       UI                          │
│  WordStudyPane (@Composable)                      │
│  └── Observes Component.state (StateFlow)         │
├───────────────────────────────────────────────────┤
│                     LOGIC                         │
│  DefaultWordStudyComponent (Decompose)            │
│  ├── Manages StateFlow<WordStudyState>            │
│  ├── Subscribes to VerseBus StrongsSelected       │
│  └── Calls WordStudyRepository methods            │
├───────────────────────────────────────────────────┤
│                      DATA                         │
│  WordStudyRepository (interface)                  │
│  WordStudyRepositoryImpl                          │
│  └── StudyQueries (SQLDelight)                    │
│       └── SQLite (lexicon_entries,                │
│           word_occurrences, fts_lexicon)           │
└───────────────────────────────────────────────────┘
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Strong's Lookup

1. **VerseBus event** — Morphology pane or Bible Reader publishes `LinkEvent.StrongsSelected("H1254")`.
2. **Component receives** — `DefaultWordStudyComponent` collects the event.
3. **Query** — `WordStudyRepository.getEntry(strongsNumber)` + `getOccurrences(strongsNumber)`.
4. **State updates** — Lexicon entry, occurrences, and related words populate state.
5. **UI renders** — Definition card + occurrence list + frequency chart.

### 2.2 Secondary Flows

- **Occurrence tap** — User taps verse occurrence → publishes `LinkEvent.VerseSelected(globalVerseId)`.
- **Frequency chart** — Occurrences grouped by book; bar chart rendered with book abbreviations on X axis.
- **Lexicon search** — User types in search field → `searchLexicon(query)` queries `fts_lexicon`.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Study.sq` | `lexiconByStrongs` | `strongsNumber` | `LexiconEntry?` | Lexicon lookup by Strong's ID |
| `Study.sq` | `occurrencesForStrongs` | `strongsNumber` | `List<occurrence>` | All verses containing this word |
| `Study.sq` | `occurrenceCount` | `strongsNumber` | `Long` | Total occurrence count |

---

## 4. Dependency Injection

```kotlin
val wordStudyModule = module {
    singleOf(::WordStudyRepositoryImpl) bind WordStudyRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultWordStudyComponent(
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
| Repository | `WordStudyRepositoryImpl` | Abstracts lexicon queries |
| Observer (VerseBus) | `StrongsSelected` subscription | Auto-loads on word selection |
| FTS5 | `fts_lexicon` | Searchable lexicon definitions |

---

## 6. Performance Considerations

- **Lexicon lookup O(1)**: Primary key lookup on `strongs_number`.
- **Occurrence query**: Uses `idx_morphology_strongs` index.
- **Strong's lexicon seed**: ~1.8 MB bundled JSON; ~8,700 Hebrew + ~5,600 Greek entries.

---

## 7. Design Decisions

| Decision | Alternatives considered | Justification |
|----------|------------------------|---------------|
| Strong's as primary key | Auto-increment ID | Strong's numbers are universally recognized and stable |
| Bundled lexicon data | API-fetched | Zero network dependency; lexicon is static reference data |
| FTS5 for lexicon search | In-memory filter | Scales better; supports phrase matching |
