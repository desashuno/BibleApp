# Data Layer

> BibleStudio — Database, Repositories & Sync Reference

---

## 1. Overview

BibleStudio stores all data locally in a single **SQLite** database managed by **SQLDelight 2.x**. There is no remote API for core Bible data — every text, annotation, and resource lives on-device. The data layer provides:

- **Type-safe queries** generated from plain SQL in `.sq` files.
- **8 query groups** (`.sq` files) scoped by bounded context.
- **17 repositories** that map between SQLDelight-generated data classes and domain entities.
- **FTS5 full-text search** across Bible text and resources.
- **Incremental migrations** from schema v1 through v16 using `.sqm` files.
- **Sync support** via last-write-wins (LWW) conflict resolution and soft deletes.

---

## 2. BibleStudioDatabase

SQLDelight generates `BibleStudioDatabase` from all `.sq` files. The database is instantiated with a platform-specific `SqlDriver` provided via Kotlin `expect`/`actual`.

### 2.1 Gradle Configuration

```kotlin
// shared/build.gradle.kts
plugins {
    id("app.cash.sqldelight") version "2.0.2"
}

sqldelight {
    databases {
        create("BibleStudioDatabase") {
            packageName.set("org.biblestudio.database")
            srcDirs("src/commonMain/sqldelight")
            deriveSchemaFromMigrations.set(true)
            verifyMigrations.set(true)
        }
    }
}
```

### 2.2 Platform Drivers

```kotlin
// commonMain — expect declaration
expect fun createSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver

// androidMain
actual fun createSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver =
    AndroidSqliteDriver(
        schema = schema,
        context = applicationContext,
        name = "biblestudio.db",
    )

// iosMain
actual fun createSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver =
    NativeSqliteDriver(
        schema = schema,
        name = "biblestudio.db",
    )

// desktopMain (JVM)
actual fun createSqlDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver {
    val dbPath = appDataPath() / "biblestudio.db"
    return JdbcSqliteDriver("jdbc:sqlite:$dbPath").also { driver ->
        schema.create(driver)
    }
}
```

| Platform | Driver | Notes |
|----------|--------|-------|
| Android | `AndroidSqliteDriver` | Uses Android framework SQLite, background thread via coroutines |
| iOS | `NativeSqliteDriver` | Linked against system SQLite via C interop |
| Desktop (JVM) | `JdbcSqliteDriver` | JDBC wrapper; file lives in platform app-data directory |

### 2.3 Database Instantiation

```kotlin
// commonMain — di/Modules.kt
val coreModule = module {
    single<SqlDriver> { createSqlDriver(BibleStudioDatabase.Schema) }
    single { BibleStudioDatabase(get<SqlDriver>()) }
}
```

The database is created once at application start and registered as a Koin singleton. All query wrappers (`database.bibleQueries`, `database.annotationQueries`, etc.) are accessed from this single instance.

---

## 3. Schema

### 3.1 Global Verse ID Convention

Every verse in the database is addressable by a **global verse ID** using the format:

```
BBCCCVVV
│ │   └── Verse number (3 digits, 001–176)
│ └────── Chapter number (3 digits, 001–150)
└──────── Book number (2 digits, 01–66)
```

Examples:
- Genesis 1:1 → `01001001`
- Psalm 119:176 → `19119176`
- Revelation 22:21 → `66022021`

This integer format enables efficient range queries, sorting, and cross-referencing without string parsing.

### 3.2 Table Catalog

#### Bible Text (4 tables)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `bibles` | `id` (INTEGER AUTOINCREMENT) | `abbreviation`, `name`, `language`, `text_direction` | Bible version metadata |
| `books` | `id` (INTEGER AUTOINCREMENT) | `bible_id` FK, `book_number`, `name`, `testament` | 66 rows per Bible |
| `chapters` | `id` (INTEGER AUTOINCREMENT) | `book_id` FK, `chapter_number`, `verse_count` | Chapter metadata |
| `verses` | `id` (INTEGER AUTOINCREMENT) | `chapter_id` FK, `global_verse_id`, `verse_number`, `text`, `html_text` | Plain + formatted text |

#### Annotations (3 tables)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `notes` | `uuid` (TEXT) | `global_verse_id`, `title`, `content`, `created_at`, `updated_at` | Rich text content |
| `highlights` | `uuid` (TEXT) | `global_verse_id`, `color_index`, `style` | Verse-level highlights |
| `bookmarks` | `uuid` (TEXT) | `global_verse_id`, `label`, `folder_id`, `sort_order` | Organized in folders |

#### Study (3 tables)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `lexicon_entries` | `strongs_number` (TEXT) | `original_word`, `transliteration`, `definition`, `usage_notes` | Hebrew + Greek lexicon |
| `morphology` | `id` (INTEGER AUTOINCREMENT) | `global_verse_id`, `word_position`, `strongs_number`, `parsing_code` | Per-word morphology |
| `word_occurrences` | `id` (INTEGER AUTOINCREMENT) | `strongs_number`, `global_verse_id`, `word_position` | Word frequency index |

#### Resources (2 tables)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `resources` | `uuid` (TEXT) | `type`, `title`, `author`, `version`, `format` | Resource metadata |
| `resource_entries` | `id` (INTEGER AUTOINCREMENT) | `resource_id` FK, `global_verse_id`, `content`, `sort_order` | Per-verse entries |

#### Writing (2 tables)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `sermons` | `uuid` (TEXT) | `title`, `scripture_ref`, `created_at`, `updated_at`, `status` | Sermon metadata |
| `sermon_sections` | `id` (INTEGER AUTOINCREMENT) | `sermon_id` FK, `type`, `content`, `sort_order` | Ordered sections |

#### References (2 tables)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `cross_references` | `id` (INTEGER AUTOINCREMENT) | `source_verse_id`, `target_verse_id`, `type`, `confidence` | Bidirectional refs |
| `parallel_passages` | `id` (INTEGER AUTOINCREMENT) | `group_id`, `global_verse_id`, `label` | Synoptic parallels |

#### Settings (3 tables)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `settings` | `key` (TEXT) | `value`, `type`, `category` | Key-value config |
| `workspaces` | `uuid` (TEXT) | `name`, `is_active`, `created_at` | Named workspaces |
| `workspace_layouts` | `id` (INTEGER AUTOINCREMENT) | `workspace_id` FK, `layout_json`, `updated_at` | JSON-serialized `LayoutNode` |

#### Search (1 table)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `search_history` | `id` (INTEGER AUTOINCREMENT) | `query`, `scope`, `result_count`, `created_at` | Recent searches |

#### Sync (2 tables)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `sync_log` | `id` (INTEGER AUTOINCREMENT) | `table_name`, `row_id`, `operation`, `timestamp`, `device_id` | Change tracking |
| `delete_log` | `id` (INTEGER AUTOINCREMENT) | `table_name`, `row_uuid`, `deleted_at`, `device_id` | Soft delete log |

#### Timeline & Geography (2 tables)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `timeline_events` | `id` (INTEGER AUTOINCREMENT) | `title`, `year_start`, `year_end`, `global_verse_id`, `era` | Historical events |
| `geographic_locations` | `id` (INTEGER AUTOINCREMENT) | `name`, `latitude`, `longitude`, `global_verse_id` | Biblical places |

#### Audio (1 table)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `audio_timestamps` | `id` (INTEGER AUTOINCREMENT) | `bible_id`, `global_verse_id`, `start_ms`, `end_ms`, `audio_file` | Per-verse timing |

#### Reading Plans (2 tables)

| Table | Primary Key | Key Columns | Notes |
|-------|------------|-------------|-------|
| `reading_plans` | `uuid` (TEXT) | `title`, `description`, `duration_days`, `type` | Plan definitions |
| `reading_plan_progress` | `id` (INTEGER AUTOINCREMENT) | `plan_id` FK, `day`, `completed`, `completed_at` | User progress |

**Total: 27 tables.**

### 3.3 FTS5 Virtual Tables

| Virtual Table | Source | Content |
|---------------|--------|---------|
| `fts_verses` | `verses` | Bible text full-text search |
| `fts_notes` | `notes` | User notes full-text search |
| `fts_resources` | `resource_entries` | Commentary/dictionary search |
| `fts_lexicon` | `lexicon_entries` | Lexicon definition search |
| `fts_sermons` | `sermon_sections` | Sermon content search |

**Total: 5 FTS5 virtual tables.**

FTS5 tables are populated at import time and kept in sync via SQLite triggers defined in the `.sq` files. They support prefix queries, phrase matching, and ranked results via `bm25()`.

---

## 4. Query Groups (`.sq` Files)

### 4.1 File-to-Table Mapping

SQLDelight generates one Kotlin `Queries` class per `.sq` file. Each file acts as a bounded context equivalent to a DAO:

| `.sq` File | Generated Class | Tables | Repository Consumers |
|-----------|----------------|--------|---------------------|
| `Bible.sq` | `BibleQueries` | `bibles`, `books`, `chapters`, `verses`, `fts_verses` | `BibleRepository`, `TextComparisonRepository` |
| `Annotation.sq` | `AnnotationQueries` | `notes`, `highlights`, `bookmarks` | `NoteRepository`, `HighlightRepository`, `BookmarkRepository` |
| `Study.sq` | `StudyQueries` | `lexicon_entries`, `morphology`, `word_occurrences` | `WordStudyRepository`, `MorphologyRepository` |
| `Resource.sq` | `ResourceQueries` | `resources`, `resource_entries`, `fts_resources` | `ResourceRepository`, `CommentaryRepository`, `DictionaryRepository` |
| `Writing.sq` | `WritingQueries` | `sermons`, `sermon_sections`, `fts_sermons` | `SermonRepository` |
| `Reference.sq` | `ReferenceQueries` | `cross_references`, `parallel_passages` | `CrossRefRepository`, `ParallelRepository` |
| `Settings.sq` | `SettingsQueries` | `settings`, `workspaces`, `workspace_layouts` | `SettingsRepository`, `WorkspaceRepository` |
| `Search.sq` | `SearchQueries` | `search_history`, `fts_*` (all 5) | `SearchRepository` |

### 4.2 `.sq` File Example

```sql
-- Bible.sq

-- Table definitions (for initial schema)
CREATE TABLE bibles (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    abbreviation TEXT NOT NULL,
    name TEXT NOT NULL,
    language TEXT NOT NULL DEFAULT 'en',
    text_direction TEXT NOT NULL DEFAULT 'ltr'
);

CREATE TABLE verses (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    chapter_id INTEGER NOT NULL REFERENCES chapters(id),
    global_verse_id INTEGER NOT NULL,
    verse_number INTEGER NOT NULL,
    text TEXT NOT NULL,
    html_text TEXT
);

CREATE INDEX idx_verses_global ON verses(global_verse_id);
CREATE INDEX idx_verses_chapter ON verses(chapter_id, verse_number);

-- FTS5 virtual table
CREATE VIRTUAL TABLE fts_verses USING fts5(text, content=verses, content_rowid=id);

-- Triggers to keep FTS in sync
CREATE TRIGGER fts_verses_ai AFTER INSERT ON verses BEGIN
    INSERT INTO fts_verses(rowid, text) VALUES (new.id, new.text);
END;

CREATE TRIGGER fts_verses_ad AFTER DELETE ON verses BEGIN
    INSERT INTO fts_verses(fts_verses, rowid, text) VALUES ('delete', old.id, old.text);
END;

CREATE TRIGGER fts_verses_au AFTER UPDATE ON verses BEGIN
    INSERT INTO fts_verses(fts_verses, rowid, text) VALUES ('delete', old.id, old.text);
    INSERT INTO fts_verses(rowid, text) VALUES (new.id, new.text);
END;

-- Named queries (generate type-safe Kotlin functions)

versesForChapter:
SELECT *
FROM verses
WHERE chapter_id = (
    SELECT c.id FROM chapters c WHERE c.book_id = :bookId AND c.chapter_number = :chapter
)
ORDER BY verse_number ASC;

verseByGlobalId:
SELECT *
FROM verses
WHERE global_verse_id = :globalVerseId;

versesInRange:
SELECT *
FROM verses
WHERE global_verse_id BETWEEN :startId AND :endId
ORDER BY global_verse_id ASC;

searchVerses:
SELECT v.*
FROM verses v
INNER JOIN fts_verses fts ON fts.rowid = v.id
WHERE fts_verses MATCH :query
ORDER BY bm25(fts_verses)
LIMIT :maxResults;

allBibles:
SELECT *
FROM bibles
ORDER BY name ASC;

insertBible:
INSERT INTO bibles(abbreviation, name, language, text_direction)
VALUES (:abbreviation, :name, :language, :textDirection);

insertVerse:
INSERT INTO verses(chapter_id, global_verse_id, verse_number, text, html_text)
VALUES (:chapterId, :globalVerseId, :verseNumber, :text, :htmlText);
```

SQLDelight generates:
- A `VersesForChapter` data class for the `versesForChapter` query result.
- A `fun versesForChapter(bookId: Int, chapter: Int): Query<VersesForChapter>` function.
- Extension methods `.executeAsList()`, `.executeAsOne()`, `.executeAsOneOrNull()`, `.asFlow()`.

### 4.3 Reactive Queries

SQLDelight queries expose a `.asFlow().mapToList()` extension for reactive streams:

```kotlin
// Reactive observation of all bibles
database.bibleQueries
    .allBibles()
    .asFlow()
    .mapToList(Dispatchers.IO)
    .map { rows -> rows.map { it.toBible() } }
    .collect { bibles -> _state.update { it.copy(bibles = bibles) } }
```

SQLDelight's `asFlow()` re-queries automatically when the underlying table is modified.

---

## 5. Repositories

### 5.1 Pattern

Every repository follows the interface/implementation split:

```kotlin
// domain/repositories/BibleRepository.kt
interface BibleRepository {
    suspend fun getAvailableBibles(): Result<List<Bible>>
    suspend fun getVerses(bookId: Int, chapter: Int): Result<List<Verse>>
    fun watchBibles(): Flow<List<Bible>>
    suspend fun searchVerses(query: String, maxResults: Int = 100): Result<List<Verse>>
}
```

```kotlin
// data/repositories/BibleRepositoryImpl.kt
class BibleRepositoryImpl(
    private val database: BibleStudioDatabase,
) : BibleRepository {

    override suspend fun getAvailableBibles(): Result<List<Bible>> = runCatching {
        database.bibleQueries
            .allBibles()
            .executeAsList()
            .map { row -> row.toBible() }
    }

    override suspend fun getVerses(bookId: Int, chapter: Int): Result<List<Verse>> = runCatching {
        database.bibleQueries
            .versesForChapter(bookId, chapter)
            .executeAsList()
            .map { row -> row.toVerse() }
    }

    override fun watchBibles(): Flow<List<Bible>> =
        database.bibleQueries
            .allBibles()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toBible() } }

    override suspend fun searchVerses(query: String, maxResults: Int): Result<List<Verse>> = runCatching {
        database.bibleQueries
            .searchVerses(query, maxResults.toLong())
            .executeAsList()
            .map { row -> row.toVerse() }
    }
}
```

### 5.2 Mapper Convention

Mappers are extension functions on SQLDelight-generated data classes:

```kotlin
// data/mappers/VerseMappers.kt
fun VersesForChapter.toVerse(): Verse = Verse(
    id = id,
    globalVerseId = global_verse_id,
    verseNumber = verse_number,
    text = text,
    htmlText = html_text,
)

fun AllBibles.toBible(): Bible = Bible(
    id = id,
    abbreviation = abbreviation,
    name = name,
    language = language,
    textDirection = text_direction,
)
```

### 5.3 Repository Catalog

| # | Repository | Query Group | Domain Entity |
|---|-----------|-------------|---------------|
| 1 | `BibleRepository` | `BibleQueries` | `Bible`, `Verse` |
| 2 | `TextComparisonRepository` | `BibleQueries` | `VersionComparison` |
| 3 | `NoteRepository` | `AnnotationQueries` | `Note` |
| 4 | `HighlightRepository` | `AnnotationQueries` | `Highlight` |
| 5 | `BookmarkRepository` | `AnnotationQueries` | `Bookmark` |
| 6 | `WordStudyRepository` | `StudyQueries` | `LexiconEntry` |
| 7 | `MorphologyRepository` | `StudyQueries` | `MorphologyData` |
| 8 | `ResourceRepository` | `ResourceQueries` | `Resource` |
| 9 | `CommentaryRepository` | `ResourceQueries` | `CommentaryEntry` |
| 10 | `DictionaryRepository` | `ResourceQueries` | `DictionaryEntry` |
| 11 | `SermonRepository` | `WritingQueries` | `Sermon` |
| 12 | `CrossRefRepository` | `ReferenceQueries` | `CrossReference` |
| 13 | `ParallelRepository` | `ReferenceQueries` | `ParallelPassage` |
| 14 | `SettingsRepository` | `SettingsQueries` | `AppSettings` |
| 15 | `WorkspaceRepository` | `SettingsQueries` | `Workspace`, `LayoutNode` |
| 16 | `SearchRepository` | `SearchQueries` | `SearchResult` |
| 17 | `ReadingPlanRepository` | `BibleQueries`* | `ReadingPlan`, `PlanProgress` |

*Reading plan queries may move to a dedicated `.sq` file if the domain grows.

---

## 6. Seed Data

On first launch, the database seeds:

| Data | Source | Size |
|------|--------|------|
| Default Bible (KJV) | Bundled resource `data/kjv.json` | ~4.5 MB |
| Cross-references (Treasury) | Bundled resource `data/cross_refs.json` | ~2.1 MB |
| Strong's Lexicon | Bundled resource `data/strongs.json` | ~1.8 MB |
| Timeline events | Bundled resource `data/timeline.json` | ~120 KB |
| Geographic locations | Bundled resource `data/locations.json` | ~80 KB |
| Default settings | Hard-coded in `seedDefaultData()` | Minimal |

Seed data is inserted inside a `database.transaction {}` block for atomicity. FTS5 indexes are rebuilt after seeding:

```kotlin
fun seedDefaultData(database: BibleStudioDatabase) {
    database.transaction {
        // Insert KJV Bible, books, chapters, verses
        kjvData.forEach { verse ->
            database.bibleQueries.insertVerse(
                chapterId = verse.chapterId,
                globalVerseId = verse.globalVerseId,
                verseNumber = verse.verseNumber,
                text = verse.text,
                htmlText = verse.htmlText,
            )
        }
        // ... cross-references, lexicon, etc.

        // Rebuild FTS indexes
        database.searchQueries.rebuildFtsVerses()
        database.searchQueries.rebuildFtsLexicon()
    }
}
```

Bundled resources are loaded via KMP resource access (`Res.readBytes()` on Compose Multiplatform or platform-specific asset loading).

---

## 7. Sync Strategy

### 7.1 Change Tracking

Every user-generated table includes:
- `created_at` — ISO 8601 timestamp (`TEXT`), set on insert.
- `updated_at` — ISO 8601 timestamp (`TEXT`), updated on every write.
- `device_id` — Identifier of the device that last modified the row.

The `sync_log` table records every INSERT, UPDATE, and DELETE with the table name, row ID, operation type, and timestamp.

### 7.2 Conflict Resolution: Last Write Wins (LWW)

When merging data from another device:

```
For each incoming row:
  1. Look up local row by UUID
  2. If local row does not exist → INSERT
  3. If incoming.updated_at > local.updated_at → UPDATE
  4. If incoming.updated_at ≤ local.updated_at → SKIP (local wins)
```

Implementation in Kotlin:

```kotlin
suspend fun mergeNotes(incoming: List<NoteDto>, deviceId: String) {
    database.transaction {
        incoming.forEach { remote ->
            val local = database.annotationQueries
                .noteByUuid(remote.uuid)
                .executeAsOneOrNull()

            when {
                local == null -> database.annotationQueries.insertNote(
                    uuid = remote.uuid,
                    globalVerseId = remote.globalVerseId,
                    title = remote.title,
                    content = remote.content,
                    createdAt = remote.createdAt,
                    updatedAt = remote.updatedAt,
                    deviceId = deviceId,
                )
                remote.updatedAt > local.updated_at -> database.annotationQueries.updateNote(
                    uuid = remote.uuid,
                    title = remote.title,
                    content = remote.content,
                    updatedAt = remote.updatedAt,
                    deviceId = deviceId,
                )
                else -> { /* local wins — skip */ }
            }
        }
    }
}
```

### 7.3 Soft Deletes

Rows are never physically deleted during normal operation. Instead:
1. The row is marked with `deleted_at` timestamp.
2. An entry is added to `delete_log`.
3. UI queries filter out rows where `deleted_at IS NOT NULL`.
4. A periodic cleanup job physically removes soft-deleted rows older than 90 days.

### 7.4 Export Format

Sync exports use a JSON envelope serialized with `kotlinx.serialization`:

```json
{
  "version": 1,
  "device_id": "abc123",
  "exported_at": "2026-01-15T10:30:00Z",
  "tables": {
    "notes": [ { "uuid": "...", "updated_at": "...", ... } ],
    "highlights": [ ... ],
    "bookmarks": [ ... ]
  },
  "deletes": [
    { "table": "notes", "uuid": "...", "deleted_at": "..." }
  ]
}
```

```kotlin
@Serializable
data class SyncExport(
    val version: Int = 1,
    @SerialName("device_id") val deviceId: String,
    @SerialName("exported_at") val exportedAt: String,
    val tables: SyncTables,
    val deletes: List<SyncDelete>,
)
```

---

## 8. Migrations

### 8.1 Migration Strategy

SQLDelight uses `.sqm` (SQL migration) files for incremental schema migrations. Each file represents one version bump. When `deriveSchemaFromMigrations = true`, SQLDelight builds the current schema by applying all `.sqm` files in order, ensuring the migration path is always verified.

### 8.2 Migration History

| Version | File | Description |
|---------|------|-------------|
| v1 → v2 | `1.sqm` | Add notes, highlights, bookmarks |
| v2 → v3 | `2.sqm` | Add lexicon_entries, morphology |
| v3 → v4 | `3.sqm` | Add resources, resource_entries |
| v4 → v5 | `4.sqm` | Add cross_references, parallel_passages |
| v5 → v6 | `5.sqm` | Add FTS5 tables (fts_verses, fts_notes, fts_resources) |
| v6 → v7 | `6.sqm` | Add sermons, sermon_sections |
| v7 → v8 | `7.sqm` | Add search_history |
| v8 → v9 | `8.sqm` | Add settings, workspaces |
| v9 → v10 | `9.sqm` | Add timeline_events, geographic_locations |
| v10 → v11 | `10.sqm` | Add audio_timestamps |
| v11 → v12 | `11.sqm` | Add reading_plans, reading_plan_progress |
| v12 → v13 | `12.sqm` | Add sync_log, delete_log |
| v13 → v14 | `13.sqm` | Add workspace_layouts |
| v14 → v15 | `14.sqm` | Add word_occurrences, fts_lexicon, fts_sermons |
| v15 → v16 | `15.sqm` | SQLDelight adoption: rename/recreate for conventions |

### 8.3 Writing a Migration

```sql
-- 14.sqm (v14 → v15)

-- 1. Create new tables
CREATE TABLE word_occurrences (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    strongs_number TEXT NOT NULL,
    global_verse_id INTEGER NOT NULL,
    word_position INTEGER NOT NULL
);

-- 2. Add columns to existing tables
ALTER TABLE verses ADD COLUMN html_text TEXT;

-- 3. Create FTS5 tables
CREATE VIRTUAL TABLE fts_lexicon USING fts5(
    definition, usage_notes,
    content=lexicon_entries, content_rowid=rowid
);

CREATE VIRTUAL TABLE fts_sermons USING fts5(
    content,
    content=sermon_sections, content_rowid=id
);

-- 4. Populate new table from existing data
INSERT INTO word_occurrences (strongs_number, global_verse_id, word_position)
SELECT strongs_number, global_verse_id, word_position FROM morphology;

-- 5. Rebuild FTS indexes
INSERT INTO fts_lexicon(fts_lexicon) VALUES ('rebuild');
INSERT INTO fts_sermons(fts_sermons) VALUES ('rebuild');
```

### 8.4 Verification

SQLDelight verifies migrations at **build time** when `verifyMigrations = true`. It replays all `.sqm` files from the initial schema and compares the result against the current `.sq` schema, failing the Gradle build if they diverge. This eliminates the need for manual migration tests.

At runtime, the `SqlSchema.migrate()` function applies pending migrations:

```kotlin
// Happens automatically when the driver is created with the schema.
// For JdbcSqliteDriver, explicit migration is needed:
BibleStudioDatabase.Schema.migrate(
    driver = driver,
    oldVersion = currentVersion,
    newVersion = BibleStudioDatabase.Schema.version,
)
```

---

## 9. Performance

### 9.1 Indexes

Indexes are declared in the `.sq` files alongside table definitions:

| Index | Table | Columns | Purpose |
|-------|-------|---------|---------|
| `idx_verses_global` | `verses` | `global_verse_id` | O(1) verse lookup by global ID |
| `idx_verses_chapter` | `verses` | `chapter_id`, `verse_number` | Chapter loading |
| `idx_notes_verse` | `notes` | `global_verse_id` | Notes per verse |
| `idx_highlights_verse` | `highlights` | `global_verse_id` | Highlights per verse |
| `idx_bookmarks_folder` | `bookmarks` | `folder_id`, `sort_order` | Folder listing |
| `idx_morphology_verse` | `morphology` | `global_verse_id` | Morphology per verse |
| `idx_morphology_strongs` | `morphology` | `strongs_number` | Word study lookups |
| `idx_crossref_source` | `cross_references` | `source_verse_id` | Cross-ref from verse |
| `idx_crossref_target` | `cross_references` | `target_verse_id` | Cross-ref to verse |
| `idx_resource_verse` | `resource_entries` | `resource_id`, `global_verse_id` | Resource per verse |
| `idx_sync_log_table` | `sync_log` | `table_name`, `timestamp` | Sync delta queries |

### 9.2 Query Performance Targets

| Operation | Target | Strategy |
|-----------|--------|----------|
| Load chapter (30 verses) | < 10 ms | Indexed query + `Dispatchers.IO` |
| FTS5 search | < 50 ms | BM25 ranking, `LIMIT` clause |
| Load cross-references | < 15 ms | Dual-column index |
| Load workspace layout | < 5 ms | Single JSON column |
| Import Bible module | < 30 s | Batch insert via `transaction {}` |

### 9.3 Query Logging

In debug builds, a custom `SqlDriver` wrapper logs slow queries:

```kotlin
class LoggingSqlDriver(
    private val delegate: SqlDriver,
) : SqlDriver by delegate {

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> {
        val start = TimeSource.Monotonic.markNow()
        val result = delegate.execute(identifier, sql, parameters, binders)
        val elapsed = start.elapsedNow()
        if (elapsed > 50.milliseconds) {
            Napier.w(tag = "SQL") { "Slow query (${elapsed.inWholeMilliseconds}ms): $sql" }
        }
        return result
    }
}
```

Wrap the real driver in debug builds:

```kotlin
val driver = if (isDebug) LoggingSqlDriver(createSqlDriver(schema)) else createSqlDriver(schema)
```

---

## 10. Related Documents

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System architecture, layers, DI, data flow |
| [MODULE_SYSTEM.md](MODULE_SYSTEM.md) | Verse Bus, PaneRegistry, workspace layout |
| [TESTING.md](TESTING.md) | Query group and migration test patterns |
| [SECURITY.md](SECURITY.md) | SQL safety with SQLDelight, input validation |
