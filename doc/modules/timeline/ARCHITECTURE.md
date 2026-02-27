# Timeline — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  TimelinePane                                     |
|  TimelineCanvas / EraBar / EventCard              |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultTimelineComponent (Decompose)             |
|  +-- Manages StateFlow<TimelineState>             |
|  +-- Subscribes to VerseBus VerseSelected         |
|  +-- Queries TimelineRepository                   |
+---------------------------------------------------+
|                      DATA                         |
|  TimelineRepository (interface)                   |
|  TimelineRepositoryImpl                           |
|  +-- TimelineQueries (SQLDelight)                 |
|       +-- SQLite (timeline_events)                |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Browse Timeline

1. **User opens pane** — Loads all eras and events.
2. **Zoom/Pan** — Canvas zooms to era or date range.
3. **Event tap** — Shows event detail with verse references.
4. **Verse link** — Publishes `VerseSelected` via VerseBus.

### 2.2 Secondary Flows

- **VerseBus** — On `VerseSelected`, highlights events associated with that verse.
- **Era filter** — Filter events by era (Patriarchs, Monarchy, Exile, etc.).

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Timeline.sq` | `allEvents` | — | `List<TimelineEvent>` | All events |
| `Timeline.sq` | `eventsByEra` | `era: String` | `List<TimelineEvent>` | Events in era |
| `Timeline.sq` | `eventsForVerse` | `globalVerseId: Int` | `List<TimelineEvent>` | Events linked to verse |
| `Timeline.sq` | `eventById` | `id: Long` | `TimelineEvent?` | Single event detail |

---

## 4. Dependency Injection

```kotlin
val timelineModule = module {
    singleOf(::TimelineRepositoryImpl) bind TimelineRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultTimelineComponent(ctx, get(), get())
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `TimelineRepositoryImpl` | Abstracts timeline queries |
| Observer | VerseBus subscriber | Highlights relevant events |
| Canvas rendering | Compose Canvas | Custom timeline drawing |

---

## 6. Performance Considerations

- **Event load < 10 ms** — Pre-populated, indexed on `era`.
- **Canvas rendering** — Uses Compose Canvas with lazy node rendering; only visible events drawn.
- **Zoom levels** — 3 fixed levels (overview / era / year) to limit render complexity.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| Fixed zoom levels | Continuous zoom | Simpler UX; predictable layout |
| Pre-populated seed data | User-entered | Biblical timeline is static reference data |
| Horizontal scroll | Vertical | Matches temporal left-to-right mental model |
