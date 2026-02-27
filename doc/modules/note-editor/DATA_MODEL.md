# Note Editor — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 Note

```kotlin
data class Note(
    val uuid: String,
    val globalVerseId: Int,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `uuid` | `String` | UUID v4 primary key | No |
| `globalVerseId` | `Int` | `BBCCCVVV` verse reference | No |
| `title` | `String` | Note title | No |
| `content` | `String` | Rich text content (Markdown-like) | No |
| `createdAt` | `String` | ISO 8601 creation timestamp | No |
| `updatedAt` | `String` | ISO 8601 last-modified timestamp | No |

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `notes`

```sql
CREATE TABLE notes (
    uuid            TEXT NOT NULL PRIMARY KEY,
    global_verse_id INTEGER NOT NULL,
    title           TEXT NOT NULL DEFAULT '',
    content         TEXT NOT NULL DEFAULT '',
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT NOT NULL DEFAULT (datetime('now'))
);
```

### 2.2 FTS5 Virtual Table

```sql
CREATE VIRTUAL TABLE fts_notes USING fts5(
    title, content,
    content=notes, content_rowid=rowid
);
```

### 2.3 FTS5 Triggers

```sql
CREATE TRIGGER notes_ai AFTER INSERT ON notes BEGIN
    INSERT INTO fts_notes(rowid, title, content)
    VALUES (new.rowid, new.title, new.content);
END;

CREATE TRIGGER notes_au AFTER UPDATE ON notes BEGIN
    INSERT INTO fts_notes(fts_notes, rowid, title, content)
    VALUES ('delete', old.rowid, old.title, old.content);
    INSERT INTO fts_notes(rowid, title, content)
    VALUES (new.rowid, new.title, new.content);
END;

CREATE TRIGGER notes_ad AFTER DELETE ON notes BEGIN
    INSERT INTO fts_notes(fts_notes, rowid, title, content)
    VALUES ('delete', old.rowid, old.title, old.content);
END;
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface NoteRepository {
    suspend fun getNotesForVerse(verseId: Int): Result<List<Note>>
    suspend fun getNoteByUuid(uuid: String): Result<Note?>
    suspend fun getAllNotes(): Result<List<Note>>
    suspend fun insertNote(note: Note): Result<Unit>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun deleteNote(uuid: String): Result<Unit>
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `notesForVerse` | `Annotation.sq` | `globalVerseId: Int` | `List<Note>` | Indexed |
| `noteByUuid` | `Annotation.sq` | `uuid: String` | `Note?` | O(1) PK lookup |
| `allNotes` | `Annotation.sq` | -- | `List<Note>` | Full scan |
| `insertNote` | `Annotation.sq` | `Note` fields | `Unit` | O(1) |
| `updateNote` | `Annotation.sq` | `Note` fields | `Unit` | O(1) |
| `deleteNote` | `Annotation.sq` | `uuid: String` | `Unit` | O(1) |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v1 | Created `notes` table | Initial schema |
| v5 -> v6 | Created `fts_notes` FTS5 virtual table + triggers | `5.sqm` |

---

## 6. Relations with Other Modules

```
notes.global_verse_id -> verses.global_verse_id (BBCCCVVV)
```

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | `notes.global_verse_id -> verses.global_verse_id` | Convention-based |
| `fts_notes` | Mirror of `notes` for full-text search | FTS5 content sync |
