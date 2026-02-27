# Cross-References — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
┌───────────────────────────────────────────────────┐
│                       UI                          │
│  CrossReferencePane (@Composable)                 │
│  └── Observes Component.state (StateFlow)         │
├───────────────────────────────────────────────────┤
│                     LOGIC                         │
│  DefaultCrossReferenceComponent (Decompose)       │
│  ├── Manages StateFlow<CrossReferenceState>       │
│  ├── Subscribes to VerseBus VerseSelected         │
│  └── Calls CrossRefRepository + ParallelRepo      │
├───────────────────────────────────────────────────┤
│                      DATA                         │
│  CrossRefRepository / ParallelRepository          │
│  └── ReferenceQueries (SQLDelight)                │
│       └── SQLite (cross_references,               │
│                   parallel_passages)              │
└───────────────────────────────────────────────────┘
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Load References for Verse

1. **VerseBus event** — Another pane publishes `LinkEvent.VerseSelected(globalVerseId)`.
2. **Component receives** — `DefaultCrossReferenceComponent` collects the event.
3. **Query** — `CrossRefRepository.getReferences(verseId)` + `ParallelRepository.getParallels(verseId)` execute.
4. **State updates** — References and parallels populate `CrossReferenceState`.
5. **UI renders** — Reference list with type badges and verse preview snippets.

### 2.2 Secondary Flows

- **Reference tap** — User taps a reference → publishes `LinkEvent.VerseSelected(targetVerseId)` → Bible Reader navigates.
- **Inline expansion** — User taps expand icon → full target verse text loads inline.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Reference.sq` | `crossRefsForVerse` | `sourceVerseId` | `List<CrossRef>` | References from this verse |
| `Reference.sq` | `crossRefsToVerse` | `targetVerseId` | `List<CrossRef>` | References to this verse |
| `Reference.sq` | `parallelsForVerse` | `globalVerseId` | `List<Parallel>` | Synoptic parallels |

---

## 4. Dependency Injection

```kotlin
val crossReferencesModule = module {
    singleOf(::CrossRefRepositoryImpl) bind CrossRefRepository::class
    singleOf(::ParallelRepositoryImpl) bind ParallelRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultCrossReferenceComponent(
            componentContext = ctx,
            crossRefRepository = get(),
            parallelRepository = get(),
            verseBus = get(),
        )
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `CrossRefRepositoryImpl` | Abstracts reference queries |
| Observer (VerseBus) | Component subscribes | Auto-loads on verse change |
| Dual-index lookup | `idx_crossref_source` + `idx_crossref_target` | Bidirectional reference resolution |

---

## 6. Performance Considerations

- **Cross-ref load < 15 ms**: Dual-column indexes on `source_verse_id` and `target_verse_id`.
- **TSK dataset**: ~100,000 cross-reference rows seeded at first launch; import < 10 s.
- **Lazy expansion**: Target verse text loaded only when user expands, not on initial list.

---

## 7. Design Decisions

| Decision | Alternatives considered | Justification |
|----------|------------------------|---------------|
| TSK as bundled seed data | API-fetched references, user-generated | Comprehensive; no network dependency; ~2.1 MB is reasonable |
| Confidence score column | Boolean relevance | Allows future ranking and filtering by quality |
| Separate `parallel_passages` table | Combined in `cross_references` | Parallels have group semantics (synoptic grouping) |
