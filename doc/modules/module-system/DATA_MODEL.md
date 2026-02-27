# Module System — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 InstalledModule

```kotlin
data class InstalledModule(
    val id: String,
    val type: ModuleType,
    val name: String,
    val version: String,
    val author: String?,
    val description: String?,
    val installedAt: String,
)

enum class ModuleType { Bible, Commentary, Dictionary, Lexicon, Maps, Audio }
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `String` | Unique module ID (e.g. "kjv") | No |
| `type` | `ModuleType` | Content type | No |
| `name` | `String` | Display name | No |
| `version` | `String` | Semantic version | No |
| `author` | `String?` | Author/publisher | Yes |
| `installedAt` | `String` | ISO 8601 install timestamp | No |

### 1.2 ModuleManifest

```kotlin
data class ModuleManifest(
    val id: String,
    val type: ModuleType,
    val name: String,
    val version: String,
    val minAppVersion: String,
    val author: String?,
    val tables: List<String>,
    val rowCount: Int,
)
```

---

## 2. SQLite Schema

Module system writes to existing tables. No dedicated tables.

| Table | Used for | Module types |
|-------|---------|--------------|
| `bibles` + `books` + `chapters` + `verses` | Bible text | Bible |
| `resources` + `resource_entries` | Commentaries/dictionaries | Commentary, Dictionary |
| `lexicon_entries` | Lexicon data | Lexicon |
| `geographic_locations` | Map data | Maps |
| `audio_timestamps` | Audio timing | Audio |

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface ModuleRepository {
    suspend fun getInstalledModules(): Result<List<InstalledModule>>
    suspend fun getModuleById(id: String): Result<InstalledModule?>
    suspend fun installModule(manifest: ModuleManifest, data: ByteArray): Result<Unit>
    suspend fun uninstallModule(id: String): Result<Unit>
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `allBibles` | `Bible.sq` | — | `List<Bible>` | Small set |
| `allResources` | `Resource.sq` | — | `List<Resource>` | Small set |
| `insertBible` | `Bible.sq` | all fields | — | Batch in transaction |

---

## 5. Migrations

Module system does not own migrations. It writes to tables created by other modules.

---

## 6. Relations with Other Modules

| External Table | Relation | Type |
|---------------|----------|------|
| `bibles` | Inserts Bible modules | Direct write |
| `resources` | Inserts commentaries/dictionaries | Direct write |
| `lexicon_entries` | Inserts lexicon data | Direct write |
| `audio_timestamps` | Inserts audio timing | Direct write |
