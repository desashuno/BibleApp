# Word Study — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 LexiconEntry

```kotlin
data class LexiconEntry(
    val strongsNumber: String,
    val originalWord: String,
    val transliteration: String,
    val definition: String,
    val usageNotes: String?,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `strongsNumber` | `String` | Strong's ID (e.g. "H1254", "G2316") | No |
| `originalWord` | `String` | Hebrew/Greek original word | No |
| `transliteration` | `String` | Romanized pronunciation | No |
| `definition` | `String` | English definition | No |
| `usageNotes` | `String?` | Extended usage and semantic range | Yes |

### 1.2 WordOccurrence

```kotlin
data class WordOccurrence(
    val id: Long,
    val strongsNumber: String,
    val globalVerseId: Int,
    val wordPosition: Int,
)
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `lexicon_entries`

```sql
CREATE TABLE lexicon_entries (
    strongs_number  TEXT NOT NULL PRIMARY KEY,
    original_word   TEXT NOT NULL,
    transliteration TEXT NOT NULL,
    definition      TEXT NOT NULL,
    usage_notes     TEXT
);
```

#### Table: `word_occurrences`

```sql
CREATE TABLE word_occurrences (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    strongs_number  TEXT    NOT NULL,
    global_verse_id INTEGER NOT NULL,
    word_position   INTEGER NOT NULL
);
```

#### Indexes

```sql
CREATE INDEX idx_morphology_strongs ON morphology(strongs_number);
```

### 2.2 FTS5 Virtual Table

```sql
CREATE VIRTUAL TABLE fts_lexicon USING fts5(
    definition, usage_notes,
    content=lexicon_entries, content_rowid=rowid
);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface WordStudyRepository {
    suspend fun getEntry(strongsNumber: String): Result<LexiconEntry?>
    suspend fun getOccurrences(strongsNumber: String): Result<List<WordOccurrence>>
    suspend fun getOccurrenceCount(strongsNumber: String): Result<Int>
    suspend fun getRelatedWords(strongsNumber: String): Result<List<LexiconEntry>>
    suspend fun searchLexicon(query: String): Result<List<LexiconEntry>>
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `lexiconByStrongs` | `Study.sq` | `strongsNumber: String` | `LexiconEntry?` | O(1) PK lookup |
| `occurrencesForStrongs` | `Study.sq` | `strongsNumber: String` | `List<WordOccurrence>` | Indexed |
| `occurrenceCount` | `Study.sq` | `strongsNumber: String` | `Long` | Count on index |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v2 → v3 | Created `lexicon_entries`, `morphology` tables | `2.sqm` |
| v14 → v15 | Created `word_occurrences`, `fts_lexicon` | `14.sqm` |

---

## 6. Relations with Other Modules

```
word_occurrences.global_verse_id → verses.global_verse_id (BBCCCVVV)
word_occurrences.strongs_number → lexicon_entries.strongs_number
morphology.strongs_number → lexicon_entries.strongs_number
```

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | `word_occurrences.global_verse_id → verses.global_verse_id` | Convention-based |
| `morphology` | Shared `strongs_number` reference | Convention-based |
