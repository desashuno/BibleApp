# {Module Name} — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

<!-- Kotlin data classes representing the module's business data. -->

### 1.1 {EntityName}

```kotlin
// data class {EntityName}(
//     val id: Long,
//     val {field1}: String,
//     val {field2}: String?,
//     val createdAt: String,
//     val updatedAt: String,
// )
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `Long` | Unique identifier | No |
| `{field1}` | `String` | {description} | No |
| `{field2}` | `String?` | {description} | Yes |
| `createdAt` | `String` | Creation timestamp | No |
| `updatedAt` | `String` | Last modification timestamp | No |

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `{table_name}`

```sql
-- CREATE TABLE {table_name} (
--   id          INTEGER PRIMARY KEY AUTOINCREMENT,
--   {field1}    TEXT    NOT NULL,
--   {field2}    TEXT,
--   created_at  TEXT    NOT NULL DEFAULT (datetime('now')),
--   updated_at  TEXT    NOT NULL DEFAULT (datetime('now')),
--   is_deleted  INTEGER NOT NULL DEFAULT 0
-- );
```

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `INTEGER` | `PK AUTOINCREMENT` | Unique identifier |
| `{field1}` | `TEXT` | `NOT NULL` | {description} |
| `{field2}` | `TEXT` | — | {description} |
| `created_at` | `TEXT` | `NOT NULL DEFAULT now` | Creation timestamp |
| `updated_at` | `TEXT` | `NOT NULL DEFAULT now` | Modification timestamp |
| `is_deleted` | `INTEGER` | `NOT NULL DEFAULT 0` | Soft delete (LWW sync) |

#### Indexes

```sql
-- CREATE INDEX idx_{table}_{field} ON {table_name}({field1});
```

### 2.2 FTS5 Virtual Tables (if applicable)

```sql
-- CREATE VIRTUAL TABLE {table_name}_fts USING fts5(
--   {field1},
--   {field2},
--   content='{table_name}',
--   content_rowid='id'
-- );
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
// interface {Module}Repository {
//     suspend fun getAll(): List<{Entity}>
//     suspend fun getById(id: Long): {Entity}?
//     suspend fun create(entity: {Entity}): Long
//     suspend fun update(entity: {Entity})
//     suspend fun delete(id: Long)
//     suspend fun search(query: String): List<{Entity}>
// }
```

### 3.2 Implementation

```kotlin
// class {Module}RepositoryImpl(
//     private val queries: {Group}Queries,
// ) : {Module}Repository {
//
//     override suspend fun getAll(): List<{Entity}> =
//         queries.{queryAll}().executeAsList().map { it.toEntity() }
//     // ...
// }
```

---

## 4. Key Queries

<!-- Most relevant SQLDelight queries used by this module. -->

| Query | Description | Performance |
|-------|-------------|-------------|
| `{queryName}` | {description} | {O(1) / O(n) / indexed} |
| `{ftsQuery}` | Full-text search | FTS5 optimized |

---

## 5. Migrations

<!-- History of schema changes for this module. -->

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| `v{N}` | Created table `{table}` | `{N}.sqm` |

---

## 6. Relations with Other Modules

<!-- References to data in other modules (verse IDs, foreign keys, etc.). -->

```
{table_name}.global_verse_id → verses.global_verse_id (BBCCCVVV)
```

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | `{table}.global_verse_id → verses.global_verse_id` | Convention-based reference |
