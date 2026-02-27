# Import / Export — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 ImportJob

```kotlin
data class ImportJob(
    val id: String,
    val fileName: String,
    val format: ImportFormat,
    val status: ImportStatus,
    val progress: Float,
    val rowCount: Int,
    val startedAt: String,
)

enum class ImportFormat { OSIS, USFM, Sword, BibleStudioSync }
enum class ImportStatus { Pending, Parsing, Previewing, Importing, Complete, Failed }
```

### 1.2 ExportJob

```kotlin
data class ExportJob(
    val id: String,
    val format: ExportFormat,
    val scope: ExportScope,
    val status: ExportStatus,
    val outputPath: String?,
)

enum class ExportFormat { JSON, OSIS, USFM, PDF }
enum class ExportScope { AllAnnotations, SelectedNotes, Sermons, FullBackup }
```

---

## 2. SQLite Schema

Import/Export does not have its own tables. It reads from and writes to existing tables:

- **Import targets**: `bibles`, `books`, `chapters`, `verses`, `resources`, `resource_entries`
- **Export sources**: `notes`, `highlights`, `bookmarks`, `sermons`, `sermon_sections`

---

## 3. Repositories

Import/Export uses `DataImporter` and `DataExporter` service classes rather than a standard repository, since it orchestrates across multiple tables.

```kotlin
class DataImporter(private val database: BibleStudioDatabase) {
    suspend fun importBible(data: ParsedBible): Result<Unit>
    suspend fun importAnnotations(data: SyncExport): Result<Unit>
}

class DataExporter(private val database: BibleStudioDatabase) {
    suspend fun exportAnnotations(): Result<SyncExport>
    suspend fun exportToOsis(bibleId: Long): Result<String>
}
```

---

## 4. Key Queries

| Query | `.sq` File | Description | Performance |
|-------|-----------|-------------|-------------|
| `insertVerse` (batch) | `Bible.sq` | Import verse data | Batch in transaction |
| `allNotes` | `Annotation.sq` | Export all notes | Full table scan |
| `allHighlights` | `Annotation.sq` | Export all highlights | Full table scan |
| `allBookmarks` | `Annotation.sq` | Export all bookmarks | Full table scan |

---

## 5. Migrations

No dedicated migrations. Uses existing tables.

---

## 6. Relations with Other Modules

| External Table | Relation | Type |
|---------------|----------|------|
| `bibles` + child tables | Import writes here | Direct write |
| `notes`, `highlights`, `bookmarks` | Export reads; sync import writes | Read/Write |
| `sermons` | Export reads | Read |
