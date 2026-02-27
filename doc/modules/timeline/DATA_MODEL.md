# Timeline — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 TimelineEvent

```kotlin
data class TimelineEvent(
    val id: Long,
    val title: String,
    val description: String?,
    val yearStart: Int,
    val yearEnd: Int?,
    val era: String,
    val globalVerseId: Int?,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `Long` | Auto-increment PK | No |
| `title` | `String` | Event name | No |
| `description` | `String?` | Brief description | Yes |
| `yearStart` | `Int` | Year (BCE negative) | No |
| `yearEnd` | `Int?` | End year for spans | Yes |
| `era` | `String` | Period category | No |
| `globalVerseId` | `Int?` | Key verse reference | Yes |

### 1.2 Era (Enum)

```kotlin
enum class Era(val label: String) {
    Creation("Creation"),
    Patriarchs("Patriarchs"),
    Exodus("Exodus & Conquest"),
    Judges("Judges"),
    UnitedMonarchy("United Monarchy"),
    DividedMonarchy("Divided Monarchy"),
    Exile("Exile"),
    Return("Return & Restoration"),
    Intertestamental("Intertestamental"),
    NewTestament("New Testament"),
    EarlyChurch("Early Church"),
}
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `timeline_events`

```sql
CREATE TABLE timeline_events (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    title           TEXT    NOT NULL,
    description     TEXT,
    year_start      INTEGER NOT NULL,
    year_end        INTEGER,
    era             TEXT    NOT NULL,
    global_verse_id INTEGER
);
```

#### Indexes

```sql
CREATE INDEX idx_timeline_era ON timeline_events(era);
CREATE INDEX idx_timeline_verse ON timeline_events(global_verse_id);
CREATE INDEX idx_timeline_years ON timeline_events(year_start, year_end);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface TimelineRepository {
    suspend fun getAll(): Result<List<TimelineEvent>>
    suspend fun getByEra(era: String): Result<List<TimelineEvent>>
    suspend fun getForVerse(globalVerseId: Int): Result<List<TimelineEvent>>
    suspend fun getById(id: Long): Result<TimelineEvent?>
}
```

---

## 4. Key Queries

| Query | Parameters | Return | Performance |
|-------|-----------|--------|-------------|
| `allEvents` | — | `List<TimelineEvent>` | ~300 rows |
| `eventsByEra` | `era` | `List<TimelineEvent>` | Indexed |
| `eventsForVerse` | `globalVerseId` | `List<TimelineEvent>` | Indexed |
| `eventsByRange` | `yearFrom, yearTo` | `List<TimelineEvent>` | Indexed |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v7 → v8 | Created `timeline_events` table | `7.sqm` |

---

## 6. Relations with Other Modules

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | `global_verse_id` reference | Convention-based |
| `entities` | Events link to knowledge graph entities | Cross-reference |
| `geographic_locations` | Events may reference locations | Cross-reference |
