# Dashboard — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 DashboardData (aggregated)

```kotlin
data class DashboardData(
    val readingPlan: ReadingPlanSummary?,
    val recentNotes: List<Note>,
    val recentBookmarks: List<Bookmark>,
    val recentWorkspaces: List<Workspace>,
    val verseOfTheDay: Verse?,
)

data class ReadingPlanSummary(
    val planName: String,
    val currentDay: Int,
    val totalDays: Int,
    val todayPassages: List<String>,
)
```

---

## 2. SQLite Schema

**No dedicated tables.** Dashboard is a pure aggregation module.

---

## 3. Repositories

No dedicated repository. Directly depends on:

| Repository | Module | Usage |
|-----------|--------|-------|
| `ReadingPlanRepository` | reading-plans | Active plan summary |
| `NoteRepository` | note-editor | Recent notes |
| `BookmarkRepository` | bookmarks-history | Recent bookmarks |
| `WorkspaceRepository` | workspace | Recent workspaces |

---

## 4. Key Queries

All queries from source modules with `LIMIT` constraints:

| Query | Source | Limit | Performance |
|-------|--------|-------|-------------|
| `activePlan` | Plan.sq | 1 | PK lookup |
| `recentNotes` | Annotation.sq | 5 | Indexed on `updated_at` |
| `recentBookmarks` | Annotation.sq | 5 | Indexed on `created_at` |
| `recentWorkspaces` | Workspace.sq | 3 | Indexed on `updated_at` |

---

## 5. Migrations

N/A — No own tables.

---

## 6. Relations with Other Modules

| Module | Relation | Direction |
|--------|----------|-----------|
| reading-plans | Reads active plan | Depends on |
| note-editor | Reads recent notes | Depends on |
| bookmarks-history | Reads recent bookmarks | Depends on |
| workspace | Reads recent workspaces | Depends on |
