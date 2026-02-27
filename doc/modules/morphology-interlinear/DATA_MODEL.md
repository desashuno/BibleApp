# Morphology / Interlinear — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 MorphWord

```kotlin
data class MorphWord(
    val id: Long,
    val globalVerseId: Int,
    val wordPosition: Int,
    val surfaceForm: String,
    val lemma: String,
    val strongsNumber: String,
    val parsingCode: String,
    val gloss: String,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `Long` | Auto-incremented primary key | No |
| `globalVerseId` | `Int` | `BBCCCVVV` verse reference | No |
| `wordPosition` | `Int` | Word order within verse (1-based) | No |
| `surfaceForm` | `String` | Original-language surface form | No |
| `lemma` | `String` | Dictionary form (lemma) | No |
| `strongsNumber` | `String` | Strong's ID (e.g. "H1254") | No |
| `parsingCode` | `String` | Morphology code (e.g. "V-AAI-3S") | No |
| `gloss` | `String` | Short English translation | No |

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `morphology`

```sql
CREATE TABLE morphology (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    global_verse_id INTEGER NOT NULL,
    word_position   INTEGER NOT NULL,
    strongs_number  TEXT    NOT NULL,
    parsing_code    TEXT    NOT NULL
);
```

#### Indexes

```sql
CREATE INDEX idx_morphology_verse ON morphology(global_verse_id);
CREATE INDEX idx_morphology_strongs ON morphology(strongs_number);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface MorphologyRepository {
    suspend fun getMorphology(verseId: Int): Result<List<MorphWord>>
    suspend fun getWordsByStrongs(strongsNumber: String): Result<List<MorphWord>>
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `morphologyForVerse` | `Study.sq` | `globalVerseId: Int` | `List<Morphology>` | Indexed via `idx_morphology_verse` |
| `morphologyByStrongs` | `Study.sq` | `strongsNumber: String` | `List<Morphology>` | Indexed via `idx_morphology_strongs` |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v2 → v3 | Created `morphology` table | `2.sqm` |
| v14 → v15 | Created `word_occurrences` table (shared with word-study) | `14.sqm` |

---

## 6. Relations with Other Modules

```
morphology.global_verse_id → verses.global_verse_id (BBCCCVVV)
morphology.strongs_number → lexicon_entries.strongs_number
```

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | `morphology.global_verse_id → verses.global_verse_id` | Convention-based |
| `lexicon_entries` | `morphology.strongs_number → lexicon_entries.strongs_number` | Convention-based |
