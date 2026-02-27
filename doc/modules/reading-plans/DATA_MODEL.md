# Reading Plans — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 ReadingPlan

```kotlin
data class ReadingPlan(
    val id: Long,
    val name: String,
    val description: String?,
    val totalDays: Int,
    val isActive: Boolean,
    val startDate: String?,
    val readings: List<DailyReading>,
)
```

### 1.2 DailyReading

```kotlin
data class DailyReading(
    val dayNumber: Int,
    val passages: List<PassageRef>,
)

data class PassageRef(
    val startVerseId: Int,
    val endVerseId: Int,
    val label: String,
)
```

### 1.3 ReadingProgress

```kotlin
data class ReadingProgress(
    val planId: Long,
    val dayNumber: Int,
    val completedAt: String,
)
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `reading_plans`

```sql
CREATE TABLE reading_plans (
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    description TEXT,
    total_days  INTEGER NOT NULL,
    is_active   INTEGER NOT NULL DEFAULT 0,
    start_date  TEXT,
    readings    TEXT    NOT NULL DEFAULT '[]'
);
```

#### Table: `reading_plan_progress`

```sql
CREATE TABLE reading_plan_progress (
    plan_id      INTEGER NOT NULL REFERENCES reading_plans(id) ON DELETE CASCADE,
    day_number   INTEGER NOT NULL,
    completed_at TEXT    NOT NULL DEFAULT (datetime('now')),
    PRIMARY KEY (plan_id, day_number)
);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface ReadingPlanRepository {
    suspend fun getAll(): Result<List<ReadingPlan>>
    suspend fun getById(id: Long): Result<ReadingPlan?>
    suspend fun getActivePlan(): Result<ReadingPlan?>
    suspend fun activatePlan(planId: Long, startDate: String): Result<Unit>
    suspend fun getProgress(planId: Long): Result<List<ReadingProgress>>
    suspend fun markDayComplete(planId: Long, dayNumber: Int): Result<Unit>
    suspend fun getCurrentDay(planId: Long): Result<Int>
}
```

---

## 4. Key Queries

| Query | Parameters | Return | Performance |
|-------|-----------|--------|-------------|
| `allPlans` | — | `List<ReadingPlan>` | Small set (~10) |
| `activePlan` | — | `ReadingPlan?` | Indexed on `is_active` |
| `progressForPlan` | `planId` | `List<ReadingProgress>` | PK composite |
| `markDayComplete` | `planId, dayNumber` | — | Single insert |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v11 → v12 | Created `reading_plans`, `reading_plan_progress` | `11.sqm` |

---

## 6. Relations with Other Modules

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | Passage refs use `BBCCCVVV` IDs | Convention-based |
| `bookmarks` | Completed readings can auto-bookmark | Optional integration |
