# Exegetical Guide — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  ExegeticalGuidePane                              |
|  SectionCards (Morphology / WordStudy / CrossRefs |
|               / Commentary / Context)             |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultExegeticalGuideComponent (Decompose)      |
|  +-- Manages StateFlow<ExegeticalGuideState>      |
|  +-- Subscribes to VerseBus VerseSelected         |
|  +-- Aggregates data from 5+ repositories         |
+---------------------------------------------------+
|                      DATA                         |
|  No dedicated repository — aggregation layer      |
|  +-- MorphologyRepository (morphology-interlinear)|
|  +-- WordStudyRepository  (word-study)            |
|  +-- CrossRefRepository   (cross-references)      |
|  +-- ResourceRepository   (resource-library)      |
|  +-- BibleRepository      (bible-reader)          |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Verse Exegesis

1. **VerseBus** — Receives `VerseSelected` event.
2. **Parallel queries** — Launches concurrent queries to 5 data sources.
3. **Aggregation** — Collects morphology, word studies, cross-references, commentaries, context.
4. **Render** — Displays organized sections in a scrollable guide.

### 2.2 Section Breakdown

| Section | Data Source | Description |
|---------|-----------|-------------|
| **Morphology** | `MorphologyRepository` | Greek/Hebrew parsing for each word |
| **Word Studies** | `WordStudyRepository` | Key vocabulary with definitions |
| **Cross-References** | `CrossRefRepository` | Related scripture passages |
| **Commentary** | `ResourceRepository` | Commentary excerpts from installed resources |
| **Context** | `BibleRepository` | Surrounding verses for literary context |

---

## 3. SQLDelight Query Integration

Uses queries from other modules — no dedicated `.sq` file.

| Source `.sq` | Query | Usage |
|-------------|-------|-------|
| `Morphology.sq` | `morphologyForVerse` | Word-by-word parsing |
| `Lexicon.sq` | `entryByStrongs` | Vocabulary definitions |
| `CrossRef.sq` | `refsForVerse` | Cross-references |
| `Resource.sq` | `entriesForVerse` | Commentary entries |
| `Bible.sq` | `versesInRange` | Context verses |

---

## 4. Dependency Injection

```kotlin
val exegeticalGuideModule = module {
    factory { (ctx: ComponentContext) ->
        DefaultExegeticalGuideComponent(
            ctx, get(), get(), get(), get(), get(), get()
        )
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Aggregator | Component logic | Combines 5 data sources into single view |
| Parallel loading | `coroutineScope { async {} }` | Sub-second total load |
| Observer | VerseBus subscriber | Auto-updates on verse change |

---

## 6. Performance Considerations

- **Total load < 500 ms** — 5 parallel queries, longest wins.
- **Caching** — Caches last 3 verse results to avoid re-query on back navigation.
- **Lazy sections** — Commentary section loads on scroll if large.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| Aggregation (no own tables) | Dedicated cache table | Keeps data authoritative in source modules |
| Fixed section order | User-configurable | Consistent exegetical workflow |
| VerseBus-driven | Manual verse input | Seamless integration with reader |
