# Bookmarks & History — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 Bookmark

```kotlin
data class Bookmark(
    val uuid: String,
    val globalVerseId: Int,
    val label: String,
    val folderId: String?,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `uuid` | `String` | UUID v4 primary key | No |
| `globalVerseId` | `Int` | `BBCCCVVV` verse reference | No |
| `label` | `String` | User-defined label | No |
| `folderId` | `String?` | Parent folder (null = root) | Yes |
| `sortOrder` | `Int` | Position within folder | No |
| `createdAt` | `String` | ISO 8601 | No |
| `updatedAt` | `String` | ISO 8601 | No |

### 1.2 BookmarkFolder

```kotlin
data class BookmarkFolder(
    val uuid: String,
    val name: String,
    val sortOrder: Int,
)
```

### 1.3 HistoryEntry

```kotlin
data class HistoryEntry(
    val id: Long,
    val globalVerseId: Int,
    val timestamp: String,
)
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `bookmarks`

```sql
CREATE TABLE bookmarks (
    uuid            TEXT    NOT NULL PRIMARY KEY,
    global_verse_id INTEGER NOT NULL,
    label           TEXT    NOT NULL,
    folder_id       TEXT,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now')),
    is_deleted      INTEGER NOT NULL DEFAULT 0
);
```

#### Indexes

```sql
CREATE INDEX idx_bookmarks_folder ON bookmarks(folder_id, sort_order);
CREATE INDEX idx_bookmarks_verse ON bookmarks(global_verse_id);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface BookmarkRepository {
    suspend fun getAll(): Result<List<Bookmark>>
    suspend fun getInFolder(folderId: String?): Result<List<Bookmark>>
    suspend fun create(bookmark: Bookmark): Result<Unit>
    suspend fun update(bookmark: Bookmark): Result<Unit>
    suspend fun delete(uuid: String): Result<Unit>
    suspend fun isBookmarked(globalVerseId: Int): Result<Boolean>
    suspend fun getHistory(limit: Int = 100): Result<List<HistoryEntry>>
    suspend fun addToHistory(globalVerseId: Int): Result<Unit>
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `bookmarksInFolder` | `Annotation.sq` | `folderId` | `List<Bookmark>` | Indexed |
| `isBookmarked` | `Annotation.sq` | `globalVerseId` | `Boolean` | Indexed |
| `allBookmarks` | `Annotation.sq` | — | `List<Bookmark>` | Full scan |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v1 → v2 | Created `bookmarks` table | `1.sqm` |

---

## 6. Relations with Other Modules

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | `global_verse_id` → `verses.global_verse_id` | Convention-based |
| `reading_plan_progress` | Reading plans record bookmark-like progress | Parallel data |
