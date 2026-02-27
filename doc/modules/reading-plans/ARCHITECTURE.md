# Reading Plans — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  ReadingPlansPane                                 |
|  PlanBrowser / PlanDetail / DailyReading          |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultReadingPlansComponent (Decompose)         |
|  +-- Manages StateFlow<ReadingPlansState>         |
|  +-- Tracks daily progress                        |
|  +-- Publishes VerseBus events                    |
+---------------------------------------------------+
|                      DATA                         |
|  ReadingPlanRepository (interface)                |
|  ReadingPlanRepositoryImpl                        |
|  +-- PlanQueries (SQLDelight)                     |
|       +-- SQLite (reading_plans,                  |
|              reading_plan_progress)               |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Follow Reading Plan

1. **User selects plan** — From pre-built or custom plans.
2. **Daily view** — Shows today's readings (passage list).
3. **Read passage** — Tap opens Bible Reader via VerseBus.
4. **Mark complete** — User marks reading done; progress persisted.

### 2.2 Secondary Flows

- **Browse plans** — List all available plans (Read the Bible in 1 Year, Psalms 30-Day, etc.).
- **Progress tracking** — Calendar view showing completion streaks.
- **Resume** — Auto-opens next unread day on launch.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Plan.sq` | `allPlans` | — | `List<ReadingPlan>` | Available plans |
| `Plan.sq` | `planById` | `id: Long` | `ReadingPlan?` | Single plan |
| `Plan.sq` | `progressForPlan` | `planId: Long` | `List<Progress>` | Completed days |
| `Plan.sq` | `markDayComplete` | `planId, dayNum` | — | Record completion |
| `Plan.sq` | `activePlan` | — | `ReadingPlan?` | Currently active plan |

---

## 4. Dependency Injection

```kotlin
val readingPlansModule = module {
    singleOf(::ReadingPlanRepositoryImpl) bind ReadingPlanRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultReadingPlansComponent(ctx, get(), get())
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `ReadingPlanRepositoryImpl` | Abstracts plan/progress queries |
| Observer | VerseBus publisher | Opens Bible Reader to today's passage |
| Seed data | Pre-built plans | Common plans bundled in app |

---

## 6. Performance Considerations

- **Plan load < 5 ms** — Small dataset; PK-indexed queries.
- **Progress query** — Indexed on `(plan_id, day_number)`.
- **Calendar view** — Computed from progress list in memory.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| Pre-built plans + custom | Only custom | Users expect standard plans (1-year, chronological) |
| Day-based granularity | Passage-based | Simpler progress; one checkbox per day |
| VerseBus to open reader | Internal navigation | Consistent with cross-pane communication pattern |
