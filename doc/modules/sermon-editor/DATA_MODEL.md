# Sermon Editor — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 Sermon

```kotlin
data class Sermon(
    val id: Long,
    val title: String,
    val passage: String?,
    val date: String?,
    val createdAt: String,
    val updatedAt: String,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `Long` | Auto-increment PK | No |
| `title` | `String` | Sermon title | No |
| `passage` | `String?` | Key passage ref (display) | Yes |
| `date` | `String?` | Sermon/preach date | Yes |
| `createdAt` | `String` | ISO 8601 | No |
| `updatedAt` | `String` | ISO 8601 | No |

### 1.2 SermonSection

```kotlin
data class SermonSection(
    val id: Long,
    val sermonId: Long,
    val type: SectionType,
    val title: String,
    val content: String,
    val sortOrder: Int,
)

enum class SectionType { Introduction, Point, Illustration, Application, Conclusion }
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `sermons`

```sql
CREATE TABLE sermons (
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    title       TEXT    NOT NULL,
    passage     TEXT,
    date        TEXT,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at  TEXT    NOT NULL DEFAULT (datetime('now'))
);
```

#### Table: `sermon_sections`

```sql
CREATE TABLE sermon_sections (
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    sermon_id   INTEGER NOT NULL REFERENCES sermons(id) ON DELETE CASCADE,
    type        TEXT    NOT NULL DEFAULT 'point',
    title       TEXT    NOT NULL,
    content     TEXT    NOT NULL DEFAULT '',
    sort_order  INTEGER NOT NULL DEFAULT 0
);
```

#### FTS5 Virtual Table

```sql
CREATE VIRTUAL TABLE fts_sermons USING fts5(
    title, content,
    content='sermons',
    content_rowid='id'
);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface SermonRepository {
    suspend fun getAll(): Result<List<Sermon>>
    suspend fun getById(id: Long): Result<Sermon?>
    suspend fun getSections(sermonId: Long): Result<List<SermonSection>>
    suspend fun create(sermon: Sermon): Result<Long>
    suspend fun update(sermon: Sermon): Result<Unit>
    suspend fun updateSection(section: SermonSection): Result<Unit>
    suspend fun addSection(sermonId: Long, type: SectionType): Result<Long>
    suspend fun deleteSection(sectionId: Long): Result<Unit>
    suspend fun delete(id: Long): Result<Unit>
    suspend fun search(query: String): Result<List<Sermon>>
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `allSermons` | `Writing.sq` | — | `List<Sermon>` | Small set |
| `sermonById` | `Writing.sq` | `id` | `Sermon?` | PK lookup |
| `sectionsForSermon` | `Writing.sq` | `sermonId` | `List<SermonSection>` | FK indexed |
| `searchSermons` | `Writing.sq` | FTS query | `List<Sermon>` | FTS5 |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v5 → v6 | Created `sermons`, `sermon_sections`, `fts_sermons` | `5.sqm` |

---

## 6. Relations with Other Modules

| External Table | Relation | Type |
|---------------|----------|------|
| `notes` | Sermons may reference notes | Convention-based |
| `verses` | Passage references to verse IDs | Convention-based |
