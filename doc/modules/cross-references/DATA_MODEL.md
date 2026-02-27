# Cross-References — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 CrossReference

```kotlin
data class CrossReference(
    val id: Long,
    val sourceVerseId: Int,
    val targetVerseId: Int,
    val type: ReferenceType,
    val confidence: Double,
)

enum class ReferenceType { Parallel, Quotation, Allusion, Related }
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `Long` | Auto-incremented primary key | No |
| `sourceVerseId` | `Int` | `BBCCCVVV` source verse | No |
| `targetVerseId` | `Int` | `BBCCCVVV` target verse | No |
| `type` | `ReferenceType` | Classification of reference | No |
| `confidence` | `Double` | Confidence score (0.0–1.0) | No |

### 1.2 ParallelPassage

```kotlin
data class ParallelPassage(
    val id: Long,
    val groupId: Int,
    val globalVerseId: Int,
    val label: String,
)
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `cross_references`

```sql
CREATE TABLE cross_references (
    id               INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    source_verse_id  INTEGER NOT NULL,
    target_verse_id  INTEGER NOT NULL,
    type             TEXT    NOT NULL DEFAULT 'related',
    confidence       REAL    NOT NULL DEFAULT 1.0
);
```

#### Table: `parallel_passages`

```sql
CREATE TABLE parallel_passages (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    group_id        INTEGER NOT NULL,
    global_verse_id INTEGER NOT NULL,
    label           TEXT    NOT NULL
);
```

#### Indexes

```sql
CREATE INDEX idx_crossref_source ON cross_references(source_verse_id);
CREATE INDEX idx_crossref_target ON cross_references(target_verse_id);
```

---

## 3. Repositories

### 3.1 CrossRefRepository Interface

```kotlin
interface CrossRefRepository {
    suspend fun getReferences(verseId: Int): Result<List<CrossReference>>
    suspend fun getReferencesByTarget(verseId: Int): Result<List<CrossReference>>
    suspend fun getReferenceCount(verseId: Int): Result<Int>
}
```

### 3.2 ParallelRepository Interface

```kotlin
interface ParallelRepository {
    suspend fun getParallels(verseId: Int): Result<List<ParallelPassage>>
    suspend fun getParallelGroup(groupId: Int): Result<List<ParallelPassage>>
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `crossRefsForVerse` | `Reference.sq` | `sourceVerseId: Int` | `List<CrossRef>` | < 15 ms (indexed) |
| `crossRefsToVerse` | `Reference.sq` | `targetVerseId: Int` | `List<CrossRef>` | < 15 ms (indexed) |
| `parallelsForVerse` | `Reference.sq` | `globalVerseId: Int` | `List<Parallel>` | O(n) small set |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v4 → v5 | Created `cross_references`, `parallel_passages` tables | `4.sqm` |

---

## 6. Relations with Other Modules

```
cross_references.source_verse_id → verses.global_verse_id (BBCCCVVV)
cross_references.target_verse_id → verses.global_verse_id (BBCCCVVV)
parallel_passages.global_verse_id → verses.global_verse_id (BBCCCVVV)
```

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | `source_verse_id / target_verse_id → verses.global_verse_id` | Convention-based |
