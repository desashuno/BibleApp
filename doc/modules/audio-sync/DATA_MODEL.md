# Audio Sync — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 AudioTimestamp

```kotlin
data class AudioTimestamp(
    val id: Long,
    val bibleId: Long,
    val globalVerseId: Int,
    val startMs: Long,
    val endMs: Long,
    val audioFile: String,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `Long` | Auto-increment PK | No |
| `bibleId` | `Long` | Foreign key to `bibles.id` | No |
| `globalVerseId` | `Int` | `BBCCCVVV` verse reference | No |
| `startMs` | `Long` | Start offset in milliseconds | No |
| `endMs` | `Long` | End offset in milliseconds | No |
| `audioFile` | `String` | Relative path to audio file | No |

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `audio_timestamps`

```sql
CREATE TABLE audio_timestamps (
    id              INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    bible_id        INTEGER NOT NULL REFERENCES bibles(id),
    global_verse_id INTEGER NOT NULL,
    start_ms        INTEGER NOT NULL,
    end_ms          INTEGER NOT NULL,
    audio_file      TEXT    NOT NULL
);
```

#### Indexes

```sql
CREATE INDEX idx_audio_bible_verse ON audio_timestamps(bible_id, global_verse_id);
CREATE INDEX idx_audio_file ON audio_timestamps(audio_file);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface AudioSyncRepository {
    suspend fun getTimestampsForChapter(
        bibleId: Long,
        bookNumber: Int,
        chapterNumber: Int,
    ): Result<List<AudioTimestamp>>

    suspend fun getTimestampForVerse(
        bibleId: Long,
        globalVerseId: Int,
    ): Result<AudioTimestamp?>

    suspend fun getAudioFile(
        bibleId: Long,
        bookNumber: Int,
        chapterNumber: Int,
    ): Result<String?>
}
```

---

## 4. Key Queries

| Query | Parameters | Return | Performance |
|-------|-----------|--------|-------------|
| `timestampsForChapter` | `bibleId`, chapter range | `List<AudioTimestamp>` | Indexed |
| `timestampForVerse` | `bibleId`, `globalVerseId` | `AudioTimestamp?` | Indexed |
| `audioFileForChapter` | `bibleId`, chapter range | `String?` | Indexed |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v13 → v14 | Created `audio_timestamps` table | `13.sqm` |

---

## 6. Relations with Other Modules

| External Table | Relation | Type |
|---------------|----------|------|
| `bibles` | `bible_id` → `bibles.id` | Foreign key |
| `verses` | `global_verse_id` reference | Convention-based |
