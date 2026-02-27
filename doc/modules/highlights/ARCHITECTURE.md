# Highlights — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  HighlightsPane / HighlightOverlay                |
|  ColorPicker / HighlightList                      |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultHighlightsComponent (Decompose)           |
|  +-- Manages StateFlow<HighlightsState>           |
|  +-- Subscribes to VerseBus VerseSelected         |
|  +-- Calls HighlightRepository                    |
+---------------------------------------------------+
|                      DATA                         |
|  HighlightRepository (interface)                  |
|  HighlightRepositoryImpl                          |
|  +-- AnnotationQueries (SQLDelight)               |
|       +-- SQLite (highlights)                     |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Create Highlight

1. **User selects text** in Bible Reader with character-level selection.
2. **Color picker** appears — user chooses from 8 highlight colors.
3. **Component** calls `HighlightRepository.create()`.
4. **State updates** — New highlight in list; Bible Reader overlays color.

### 2.2 Secondary Flows

- **Browse highlights** — View all highlights filtered by color/book.
- **Delete highlight** — Soft delete with LWW sync support.
- **VerseBus** — When a highlighted verse is selected, shows highlight info.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Annotation.sq` | `highlightsForVerse` | `globalVerseId` | `List<Highlight>` | Highlights on a verse |
| `Annotation.sq` | `allHighlights` | — | `List<Highlight>` | All highlights |
| `Annotation.sq` | `insertHighlight` | all fields | — | Create highlight |
| `Annotation.sq` | `deleteHighlight` | `uuid` | — | Soft delete |

---

## 4. Dependency Injection

```kotlin
val highlightsModule = module {
    singleOf(::HighlightRepositoryImpl) bind HighlightRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultHighlightsComponent(ctx, get(), get())
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `HighlightRepositoryImpl` | Abstracts highlight queries |
| Observer | VerseBus subscriber | Auto-loads highlights for selected verse |
| Soft delete | `is_deleted` + `delete_log` | LWW sync compatibility |

---

## 6. Performance Considerations

- **Highlight load < 5 ms** — Indexed on `global_verse_id`.
- **Overlay rendering** — Bible Reader renders highlights as background spans; no re-query per scroll.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| 8 fixed colors | Unlimited picker | Consistent palette; matches DESIGN_SYSTEM highlight colors |
| Character-level selection | Verse-level only | Finer granularity for study |
| UUID primary key | Auto-increment | Sync-safe across devices |
