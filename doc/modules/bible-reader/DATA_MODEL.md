# Bible Reader — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 Bible

```kotlin
data class Bible(
    val id: Long,
    val abbreviation: String,
    val name: String,
    val language: String,
    val textDirection: String,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `Long` | Auto-incremented primary key | No |
| `abbreviation` | `String` | Short version code (e.g. "KJV") | No |
| `name` | `String` | Full display name | No |
| `language` | `String` | ISO 639-1 language code | No |
| `textDirection` | `String` | `"ltr"` or `"rtl"` | No |

### 1.2 Book

```kotlin
data class Book(
    val id: Long,
    val bibleId: Long,
    val bookNumber: Int,
    val name: String,
    val testament: String,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `Long` | Auto-incremented primary key | No |
| `bibleId` | `Long` | FK to `bibles.id` | No |
| `bookNumber` | `Int` | 1–66 canonical order | No |
| `name` | `String` | Localized book name | No |
| `testament` | `String` | `"OT"` or `"NT"` | No |

### 1.3 Chapter

```kotlin
data class Chapter(
    val id: Long,
    val bookId: Long,
    val chapterNumber: Int,
    val verseCount: Int,
)
```

### 1.4 Verse

```kotlin
data class Verse(
    val id: Long,
    val chapterId: Long,
    val globalVerseId: Int,
    val verseNumber: Int,
    val text: String,
    val htmlText: String?,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `Long` | Auto-incremented primary key | No |
| `chapterId` | `Long` | FK to `chapters.id` | No |
| `globalVerseId` | `Int` | `BBCCCVVV` unique verse identifier | No |
| `verseNumber` | `Int` | Verse number within chapter | No |
| `text` | `String` | Plain text content | No |
| `htmlText` | `String?` | Formatted HTML text | Yes |

### 1.5 VersionComparison

```kotlin
data class VersionComparison(
    val globalVerseId: Int,
    val versions: List<VersionText>,
)

data class VersionText(
    val bibleId: Long,
    val abbreviation: String,
    val text: String,
)
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `bibles`

```sql
CREATE TABLE bibles (
    id           INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    abbreviation TEXT    NOT NULL,
    name         TEXT    NOT NULL,
    language     TEXT    NOT NULL DEFAULT 'en',
    text_direction TEXT  NOT NULL DEFAULT 'ltr'
);
```

#### Table: `books`

```sql
CREATE TABLE books (
    id          INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    bible_id    INTEGER NOT NULL REFERENCES bibles(id),
    book_number INTEGER NOT NULL,
    name        TEXT    NOT NULL,
    testament   TEXT    NOT NULL
);
```

#### Table: `chapters`

```sql
CREATE TABLE chapters (
    id             INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    book_id        INTEGER NOT NULL REFERENCES books(id),
    chapter_number INTEGER NOT NULL,
    verse_count    INTEGER NOT NULL
);
```

#### Table: `verses`

```sql
CREATE TABLE verses (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    chapter_id      INTEGER NOT NULL REFERENCES chapters(id),
    global_verse_id INTEGER NOT NULL,
    verse_number    INTEGER NOT NULL,
    text            TEXT    NOT NULL,
    html_text       TEXT
);
```

#### Indexes

```sql
CREATE INDEX idx_verses_global  ON verses(global_verse_id);
CREATE INDEX idx_verses_chapter ON verses(chapter_id, verse_number);
```

### 2.2 FTS5 Virtual Tables

```sql
CREATE VIRTUAL TABLE fts_verses USING fts5(
    text,
    content=verses,
    content_rowid=id
);
```

FTS5 triggers maintain sync on INSERT, UPDATE, and DELETE (see `Bible.sq`).

---

## 3. Repositories

### 3.1 BibleRepository Interface

```kotlin
interface BibleRepository {
    suspend fun getAvailableBibles(): Result<List<Bible>>
    suspend fun getVerses(bookId: Int, chapter: Int): Result<List<Verse>>
    suspend fun getVerseByGlobalId(globalVerseId: Int): Result<Verse?>
    suspend fun getVersesInRange(startId: Int, endId: Int): Result<List<Verse>>
    fun watchBibles(): Flow<List<Bible>>
    suspend fun searchVerses(query: String, maxResults: Int = 100): Result<List<Verse>>
}
```

### 3.2 TextComparisonRepository Interface

```kotlin
interface TextComparisonRepository {
    suspend fun compareVersions(globalVerseId: Int, bibleIds: List<Long>): Result<VersionComparison>
    suspend fun compareRange(startId: Int, endId: Int, bibleIds: List<Long>): Result<List<VersionComparison>>
}
```

### 3.3 Implementation

```kotlin
class BibleRepositoryImpl(
    private val database: BibleStudioDatabase,
) : BibleRepository {

    override suspend fun getAvailableBibles(): Result<List<Bible>> = runCatching {
        database.bibleQueries.allBibles().executeAsList().map { it.toBible() }
    }

    override suspend fun getVerses(bookId: Int, chapter: Int): Result<List<Verse>> = runCatching {
        database.bibleQueries.versesForChapter(bookId, chapter)
            .executeAsList().map { it.toVerse() }
    }

    override fun watchBibles(): Flow<List<Bible>> =
        database.bibleQueries.allBibles()
            .asFlow().mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toBible() } }
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `versesForChapter` | `Bible.sq` | `bookId`, `chapter` | `List<Verse>` | < 10 ms (indexed) |
| `verseByGlobalId` | `Bible.sq` | `globalVerseId` | `Verse?` | O(1) indexed |
| `versesInRange` | `Bible.sq` | `startId`, `endId` | `List<Verse>` | Indexed range scan |
| `searchVerses` | `Bible.sq` | `query`, `maxResults` | `List<Verse>` | FTS5 + BM25 < 50 ms |
| `allBibles` | `Bible.sq` | — | `List<Bible>` | Full scan (small set) |
| `insertVerse` | `Bible.sq` | all columns | `Long` | Batch-optimized |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v1 (initial) | Created `bibles`, `books`, `chapters`, `verses` tables | `Bible.sq` |
| v5 → v6 | Created `fts_verses` FTS5 virtual table + triggers | `5.sqm` |
| v14 → v15 | Added `html_text` column to `verses` | `14.sqm` |

---

## 6. Relations with Other Modules

```
verses.global_verse_id ← cross_references.source_verse_id
verses.global_verse_id ← cross_references.target_verse_id
verses.global_verse_id ← notes.global_verse_id
verses.global_verse_id ← highlights.global_verse_id
verses.global_verse_id ← bookmarks.global_verse_id
verses.global_verse_id ← morphology.global_verse_id
```

| External Table | Relation | Type |
|---------------|----------|------|
| `cross_references` | `source_verse_id / target_verse_id → verses.global_verse_id` | Convention-based |
| `notes` | `global_verse_id → verses.global_verse_id` | Convention-based |
| `highlights` | `global_verse_id → verses.global_verse_id` | Convention-based |
| `bookmarks` | `global_verse_id → verses.global_verse_id` | Convention-based |
| `morphology` | `global_verse_id → verses.global_verse_id` | Convention-based |
| `audio_timestamps` | `global_verse_id → verses.global_verse_id` | Convention-based |
