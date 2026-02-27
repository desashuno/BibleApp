# Theological Atlas — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 Location

```kotlin
data class Location(
    val id: Long,
    val name: String,
    val modernName: String?,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val verseReferences: List<Int>,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `Long` | Auto-increment PK | No |
| `name` | `String` | Biblical name | No |
| `modernName` | `String?` | Modern name | Yes |
| `latitude` | `Double` | Decimal degrees | No |
| `longitude` | `Double` | Decimal degrees | No |
| `description` | `String?` | Brief description | Yes |
| `verseReferences` | `List<Int>` | `BBCCCVVV` list (JSON) | No |

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `geographic_locations`

```sql
CREATE TABLE geographic_locations (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name            TEXT    NOT NULL,
    modern_name     TEXT,
    lat             REAL    NOT NULL,
    lon             REAL    NOT NULL,
    description     TEXT,
    verse_references TEXT   NOT NULL DEFAULT '[]'
);
```

#### Table: `location_verse_index` (materialized join)

```sql
CREATE TABLE location_verse_index (
    location_id     INTEGER NOT NULL REFERENCES geographic_locations(id),
    global_verse_id INTEGER NOT NULL,
    PRIMARY KEY (location_id, global_verse_id)
);
```

#### Indexes

```sql
CREATE INDEX idx_locations_name ON geographic_locations(name);
CREATE INDEX idx_location_verse ON location_verse_index(global_verse_id);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface AtlasRepository {
    suspend fun getAll(): Result<List<Location>>
    suspend fun getById(id: Long): Result<Location?>
    suspend fun getForVerse(globalVerseId: Int): Result<List<Location>>
    suspend fun search(query: String): Result<List<Location>>
}
```

---

## 4. Key Queries

| Query | Parameters | Return | Performance |
|-------|-----------|--------|-------------|
| `allLocations` | — | `List<Location>` | ~200 rows |
| `locationsForVerse` | `globalVerseId` | `List<Location>` | Via verse index |
| `searchLocations` | `name LIKE %q%` | `List<Location>` | Indexed on name |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v8 → v9 | Created `geographic_locations` | `8.sqm` |

---

## 6. Relations with Other Modules

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | `location_verse_index.global_verse_id` | Convention-based |
| `entities` | Place entities link to locations | Cross-reference |
| `timeline_events` | Events may reference locations | Cross-reference |
