# Resource Library — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 Resource

```kotlin
data class Resource(
    val uuid: String,
    val type: ResourceType,
    val title: String,
    val author: String,
    val version: String,
    val format: String,
)

enum class ResourceType { Commentary, Dictionary, Devotional, Map, Chart }
```

### 1.2 ResourceEntry

```kotlin
data class ResourceEntry(
    val id: Long,
    val resourceId: String,
    val globalVerseId: Int,
    val content: String,
    val sortOrder: Int,
)
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `resources`

```sql
CREATE TABLE resources (
    uuid    TEXT NOT NULL PRIMARY KEY,
    type    TEXT NOT NULL,
    title   TEXT NOT NULL,
    author  TEXT NOT NULL DEFAULT '',
    version TEXT NOT NULL DEFAULT '1.0',
    format  TEXT NOT NULL DEFAULT 'text'
);
```

#### Table: `resource_entries`

```sql
CREATE TABLE resource_entries (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    resource_id     TEXT    NOT NULL REFERENCES resources(uuid),
    global_verse_id INTEGER NOT NULL,
    content         TEXT    NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0
);
```

### 2.2 FTS5 Virtual Table

```sql
CREATE VIRTUAL TABLE fts_resources USING fts5(
    content,
    content=resource_entries, content_rowid=id
);
```

---

## 3. Repositories

### 3.1 ResourceRepository Interface

```kotlin
interface ResourceRepository {
    suspend fun getAllResources(): Result<List<Resource>>
    suspend fun getResourceById(uuid: String): Result<Resource?>
    suspend fun getEntriesForVerse(verseId: Int, resourceId: String? = null): Result<List<ResourceEntry>>
    suspend fun importResource(resource: Resource, entries: List<ResourceEntry>): Result<Unit>
    suspend fun deleteResource(uuid: String): Result<Unit>
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `allResources` | `Resource.sq` | -- | `List<Resource>` | Full scan (small set) |
| `resourceById` | `Resource.sq` | `uuid: String` | `Resource?` | O(1) PK |
| `entriesForVerse` | `Resource.sq` | `globalVerseId, resourceId?` | `List<ResourceEntry>` | Indexed |
| `insertResource` | `Resource.sq` | Resource fields | `Unit` | O(1) |
| `insertEntry` | `Resource.sq` | Entry fields | `Unit` | O(1) |
| `deleteResource` | `Resource.sq` | `uuid` | `Unit` | Cascade entries |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v3 -> v4 | Created `resources`, `resource_entries` tables | `3.sqm` |
| v5 -> v6 | Created `fts_resources` FTS5 virtual table + triggers | `5.sqm` |

---

## 6. Relations with Other Modules

```
resource_entries.resource_id -> resources.uuid (FK)
resource_entries.global_verse_id -> verses.global_verse_id (BBCCCVVV convention)
```

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | `resource_entries.global_verse_id -> verses.global_verse_id` | Convention-based |
| `fts_resources` | Mirror of `resource_entries` for FTS5 | Content sync triggers |
