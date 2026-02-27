# Highlights — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 Highlight

```kotlin
data class Highlight(
    val uuid: String,
    val globalVerseId: Int,
    val colorIndex: Int,
    val style: HighlightStyle,
    val startOffset: Int?,
    val endOffset: Int?,
    val createdAt: String,
    val updatedAt: String,
)

enum class HighlightStyle { Background, Underline, BoxOutline }
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `uuid` | `String` | UUID v4 primary key | No |
| `globalVerseId` | `Int` | `BBCCCVVV` verse reference | No |
| `colorIndex` | `Int` | 0–7 index into highlight palette | No |
| `style` | `HighlightStyle` | Visual style | No |
| `startOffset` | `Int?` | Character start (null = whole verse) | Yes |
| `endOffset` | `Int?` | Character end (null = whole verse) | Yes |
| `createdAt` | `String` | ISO 8601 | No |
| `updatedAt` | `String` | ISO 8601 | No |

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `highlights`

```sql
CREATE TABLE highlights (
    uuid            TEXT    NOT NULL PRIMARY KEY,
    global_verse_id INTEGER NOT NULL,
    color_index     INTEGER NOT NULL DEFAULT 0,
    style           TEXT    NOT NULL DEFAULT 'background',
    start_offset    INTEGER,
    end_offset      INTEGER,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    is_deleted      INTEGER NOT NULL DEFAULT 0
);
```

#### Indexes

```sql
CREATE INDEX idx_highlights_verse ON highlights(global_verse_id);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface HighlightRepository {
    suspend fun getForVerse(globalVerseId: Int): Result<List<Highlight>>
    suspend fun getAll(): Result<List<Highlight>>
    suspend fun getByColor(colorIndex: Int): Result<List<Highlight>>
    suspend fun create(highlight: Highlight): Result<Unit>
    suspend fun update(highlight: Highlight): Result<Unit>
    suspend fun delete(uuid: String): Result<Unit>
    fun watchForVerse(globalVerseId: Int): Flow<List<Highlight>>
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `highlightsForVerse` | `Annotation.sq` | `globalVerseId` | `List<Highlight>` | < 5 ms (indexed) |
| `allHighlights` | `Annotation.sq` | — | `List<Highlight>` | Full scan |
| `insertHighlight` | `Annotation.sq` | all fields | — | Single insert |
| `deleteHighlight` | `Annotation.sq` | `uuid` | — | Soft delete |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v1 → v2 | Created `highlights` table | `1.sqm` |

---

## 6. Relations with Other Modules

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | `global_verse_id` → `verses.global_verse_id` | Convention-based |
| `sync_log` / `delete_log` | Change tracking for sync | Sync infrastructure |
