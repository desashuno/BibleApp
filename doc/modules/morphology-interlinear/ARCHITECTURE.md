# Morphology / Interlinear — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
┌───────────────────────────────────────────────────┐
│                       UI                          │
│  InterlinearPane (@Composable)                    │
│  └── Observes Component.state (StateFlow)         │
├───────────────────────────────────────────────────┤
│                     LOGIC                         │
│  DefaultInterlinearComponent (Decompose)          │
│  ├── Manages StateFlow<InterlinearState>          │
│  ├── Subscribes to VerseBus VerseSelected         │
│  ├── ParsingDecoder: V-AAI-3S → human text        │
│  └── Calls MorphologyRepository methods           │
├───────────────────────────────────────────────────┤
│                      DATA                         │
│  MorphologyRepository (interface)                 │
│  MorphologyRepositoryImpl                         │
│  └── StudyQueries (SQLDelight)                    │
│       └── SQLite (morphology table)               │
└───────────────────────────────────────────────────┘
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Interlinear Display

1. **VerseBus event** — Bible Reader publishes `LinkEvent.VerseSelected(globalVerseId)`.
2. **Component receives** — Loads morphology data for the verse.
3. **Parsing decode** — Each `parsingCode` is decoded (e.g. `V-AAI-3S` → "Verb, Aorist, Active, Indicative, 3rd Person, Singular").
4. **State updates** — Word list populates `InterlinearState.words`.
5. **UI renders** — Word grid with 4 rows: original, transliteration, gloss, parsing.

### 2.2 Secondary Flows

- **Word tap** — User taps word → publishes `LinkEvent.StrongsSelected(strongsNumber)` → Word Study loads.
- **Display mode change** — User toggles between Interlinear/Parallel/Inline views.
- **Hebrew RTL** — Hebrew text uses proper RTL rendering with bidirectional layout.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Study.sq` | `morphologyForVerse` | `globalVerseId` | `List<Morphology>` | All words for a verse, ordered by position |
| `Study.sq` | `morphologyByStrongs` | `strongsNumber` | `List<Morphology>` | All occurrences of a Strong's number |

---

## 4. Dependency Injection

```kotlin
val morphologyModule = module {
    singleOf(::MorphologyRepositoryImpl) bind MorphologyRepository::class
    single { ParsingDecoder() }
    factory { (ctx: ComponentContext) ->
        DefaultInterlinearComponent(
            componentContext = ctx,
            repository = get(),
            parsingDecoder = get(),
            verseBus = get(),
        )
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `MorphologyRepositoryImpl` | Abstracts morphology queries |
| Decoder | `ParsingDecoder` | Separates morphology code parsing from component logic |
| Observer (VerseBus) | `VerseSelected` subscription | Auto-loads on verse change |

---

## 6. Performance Considerations

- **Morphology load**: Indexed by `global_verse_id`; typical verse has 10–30 words.
- **Parsing decoder**: In-memory lookup table; decode is O(1) per word.
- **RTL rendering**: Hebrew text uses `TextDirection.Rtl` in Compose; no layout recalculation needed.

---

## 7. Design Decisions

| Decision | Alternatives considered | Justification |
|----------|------------------------|---------------|
| Parsing code stored as string | Separate columns per morphology field | Compact storage; decoded at display time |
| Single `morphology` table | Separate tables for Hebrew/Greek | Unified schema; language inferred from Strong's prefix |
| `ParsingDecoder` as separate class | Inline decode in component | Testable; reusable across modules |
