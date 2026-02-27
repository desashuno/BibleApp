# Search — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 SearchResult

```kotlin
data class SearchResult(
    val globalVerseId: Int,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val snippet: String,
    val rank: Double,
    val source: SearchSource,
)

enum class SearchSource { Bible, Notes, Resources, Lexicon, Sermons }
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `globalVerseId` | `Int` | `BBCCCVVV` verse reference | No |
| `bookName` | `String` | Human-readable book name | No |
| `chapter` | `Int` | Chapter number | No |
| `verseNumber` | `Int` | Verse number | No |
| `snippet` | `String` | Text snippet with match terms highlighted | No |
| `rank` | `Double` | BM25 relevance score | No |
| `source` | `SearchSource` | Which FTS table the result came from | No |

### 1.2 SearchHistoryEntry

```kotlin
data class SearchHistoryEntry(
    val id: Long,
    val query: String,
    val scope: String,
    val resultCount: Int,
    val createdAt: String,
)
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `search_history`

```sql
CREATE TABLE search_history (
    id           INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    query        TEXT    NOT NULL,
    scope        TEXT    NOT NULL DEFAULT 'all',
    result_count INTEGER NOT NULL DEFAULT 0,
    created_at   TEXT    NOT NULL DEFAULT (datetime('now'))
);
```

### 2.2 FTS5 Virtual Tables (consumed — not owned)

| Virtual Table | Source Table | Owner Module | Content |
|---------------|-------------|--------------|---------|
| `fts_verses` | `verses` | bible-reader | Bible text search |
| `fts_notes` | `notes` | note-editor | User notes search |
| `fts_resources` | `resource_entries` | resource-library | Commentary/dictionary search |
| `fts_lexicon` | `lexicon_entries` | word-study | Lexicon definition search |
| `fts_sermons` | `sermon_sections` | sermon-editor | Sermon content search |

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface SearchRepository {
    suspend fun searchAll(query: String, maxResults: Int = 100): Result<List<SearchResult>>
    suspend fun searchVerses(query: String, bookRange: IntRange? = null, maxResults: Int = 100): Result<List<SearchResult>>
    suspend fun searchNotes(query: String, maxResults: Int = 50): Result<List<SearchResult>>
    suspend fun searchResources(query: String, maxResults: Int = 50): Result<List<SearchResult>>
    suspend fun getHistory(limit: Int = 20): Result<List<SearchHistoryEntry>>
    suspend fun addToHistory(query: String, scope: String, resultCount: Int)
    suspend fun clearHistory()
}
```

### 3.2 Implementation

```kotlin
class SearchRepositoryImpl(
    private val database: BibleStudioDatabase,
) : SearchRepository {

    override suspend fun searchVerses(query: String, bookRange: IntRange?, maxResults: Int): Result<List<SearchResult>> = runCatching {
        database.searchQueries
            .searchVersesFts(query, maxResults.toLong())
            .executeAsList()
            .map { it.toSearchResult(SearchSource.Bible) }
    }
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `searchVersesFts` | `Search.sq` | `query: String, limit: Long` | `List<SearchResult>` | < 50 ms (FTS5 + BM25) |
| `searchNotesFts` | `Search.sq` | `query: String, limit: Long` | `List<SearchResult>` | < 50 ms |
| `searchResourcesFts` | `Search.sq` | `query: String, limit: Long` | `List<SearchResult>` | < 50 ms |
| `searchLexiconFts` | `Search.sq` | `query: String, limit: Long` | `List<SearchResult>` | < 50 ms |
| `searchSermonsFts` | `Search.sq` | `query: String, limit: Long` | `List<SearchResult>` | < 50 ms |
| `recentSearches` | `Search.sq` | `limit: Long` | `List<SearchHistory>` | O(n) small set |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v5 → v6 | Created FTS5 tables (`fts_verses`, `fts_notes`, `fts_resources`) | `5.sqm` |
| v7 → v8 | Created `search_history` table | `7.sqm` |
| v14 → v15 | Added `fts_lexicon`, `fts_sermons` FTS5 tables | `14.sqm` |

---

## 6. Relations with Other Modules

The Search module does not own any verse-reference data directly. It reads from FTS5 virtual tables that mirror content from other modules:

| External Table | Relation | Type |
|---------------|----------|------|
| `fts_verses` (→ `verses`) | Read via FTS5 MATCH | Cross-module read |
| `fts_notes` (→ `notes`) | Read via FTS5 MATCH | Cross-module read |
| `fts_resources` (→ `resource_entries`) | Read via FTS5 MATCH | Cross-module read |
| `fts_lexicon` (→ `lexicon_entries`) | Read via FTS5 MATCH | Cross-module read |
| `fts_sermons` (→ `sermon_sections`) | Read via FTS5 MATCH | Cross-module read |
