# Passage Guide — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
┌───────────────────────────────────────────────────┐
│                       UI                          │
│  PassageGuidePane (@Composable)                   │
│  └── Sections: CrossRefs, Words, Commentary, Notes│
├───────────────────────────────────────────────────┤
│                     LOGIC                         │
│  DefaultPassageGuideComponent (Decompose)         │
│  ├── Manages StateFlow<PassageGuideState>         │
│  ├── Subscribes to VerseBus VerseSelected         │
│  └── Orchestrates parallel queries to 6 repos    │
├───────────────────────────────────────────────────┤
│                      DATA                         │
│  No own repositories — delegates to:              │
│  BibleRepository, CrossRefRepository,             │
│  WordStudyRepository, ResourceRepository,         │
│  NoteRepository, MorphologyRepository             │
└───────────────────────────────────────────────────┘
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Build Passage Report

1. **VerseBus event** — `LinkEvent.VerseSelected(globalVerseId)` received.
2. **Parallel queries** — Component launches 6 `async {}` coroutines against different repositories.
3. **Await all** — Results combined into `PassageReport`.
4. **State updates** — UI renders collapsible sections for each data source.

### 2.2 Secondary Flows

- **Section collapse/expand** — User toggles sections; state tracks `expandedSections: Set<String>`.
- **Item tap** — Tapping a cross-reference publishes `VerseSelected`; tapping a word publishes `StrongsSelected`.

---

## 3. Dependency Injection

```kotlin
val passageGuideModule = module {
    factory { (ctx: ComponentContext) ->
        DefaultPassageGuideComponent(
            componentContext = ctx,
            bibleRepository = get(),
            crossRefRepository = get(),
            wordStudyRepository = get(),
            resourceRepository = get(),
            noteRepository = get(),
            morphologyRepository = get(),
            verseBus = get(),
        )
    }
}
```

---

## 4. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Aggregator / Facade | `PassageGuideComponent` | Single entry point to multi-module data |
| Parallel async | `coroutineScope { async {} }` | All 6 queries execute concurrently |
| Observer (VerseBus) | VerseBus subscription | Auto-reloads on verse change |

---

## 5. Performance Considerations

- **Parallel execution**: All 6 repository calls execute simultaneously; total latency = max(individual latencies).
- **Progressive rendering**: Each section renders as its data arrives; skeleton placeholders shown for pending sections.
- **Cancellation**: If a new `VerseSelected` arrives before current report finishes, in-flight queries are cancelled.

---

## 6. Design Decisions

| Decision | Alternatives considered | Justification |
|----------|------------------------|---------------|
| No own tables | Duplicated/cached data | Avoids data inconsistency; always reads fresh data from source modules |
| Parallel queries | Sequential queries | Reduces total load time from sum → max of individual query times |
| Collapsible sections | Tabbed layout | Allows users to see/hide sections based on study needs |
