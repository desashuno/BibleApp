# Dashboard — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  DashboardPane                                    |
|  ReadingPlanWidget / RecentNotesWidget            |
|  BookmarkWidget / QuickNavWidget                  |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultDashboardComponent (Decompose)            |
|  +-- Manages StateFlow<DashboardState>            |
|  +-- Aggregates data from multiple repositories   |
+---------------------------------------------------+
|                      DATA                         |
|  No dedicated repository — aggregation layer      |
|  +-- ReadingPlanRepository (reading-plans)        |
|  +-- NoteRepository (note-editor)                 |
|  +-- BookmarkRepository (bookmarks-history)       |
|  +-- WorkspaceRepository (workspace)              |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Dashboard Load

1. **Pane opens** — Component triggers parallel data load.
2. **Widget data** — Each widget queries its source repository.
3. **Render** — Widgets displayed in a responsive grid.

### 2.2 Widgets

| Widget | Data Source | Content |
|--------|-----------|---------|
| **Reading Plan** | `ReadingPlanRepository` | Today's reading + progress |
| **Recent Notes** | `NoteRepository` | Last 5 edited notes |
| **Bookmarks** | `BookmarkRepository` | Pinned/recent bookmarks |
| **Quick Nav** | `WorkspaceRepository` | Recently opened workspaces |
| **Verse of the Day** | Static/curated | Daily verse |

---

## 3. SQLDelight Query Integration

Uses queries from other modules. No dedicated `.sq` file.

| Source `.sq` | Query | Usage |
|-------------|-------|-------|
| `Plan.sq` | `activePlan` | Reading plan widget |
| `Annotation.sq` | `recentNotes` | Recent notes widget |
| `Annotation.sq` | `recentBookmarks` | Bookmark widget |
| `Workspace.sq` | `recentWorkspaces` | Quick nav widget |

---

## 4. Dependency Injection

```kotlin
val dashboardModule = module {
    factory { (ctx: ComponentContext) ->
        DefaultDashboardComponent(ctx, get(), get(), get(), get())
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Aggregator | Component logic | Combines 4+ data sources |
| Widget grid | UI layout | Modular, rearrangeable dashboard |
| Observer | StateFlow | Reactive updates when underlying data changes |

---

## 6. Performance Considerations

- **Dashboard load < 200 ms** — Parallel widget queries.
- **Lazy widgets** — Below-fold widgets load on scroll.
- **Stale-while-revalidate** — Shows cached data immediately, refreshes in background.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| Widget-based layout | Single list | More informational at-a-glance |
| Pre-defined widgets | User-created | Simpler; covers common needs |
| No own tables | Cache table | Keep data authoritative in source modules |
