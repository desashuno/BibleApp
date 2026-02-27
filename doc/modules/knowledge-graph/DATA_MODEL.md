# Knowledge Graph — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 Entity

```kotlin
data class Entity(
    val id: Long,
    val name: String,
    val type: EntityType,
    val description: String?,
    val aliases: List<String>,
    val verseReferences: List<Int>,
)

enum class EntityType { Person, Place, Event, Object, Concept }
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `id` | `Long` | Auto-increment PK | No |
| `name` | `String` | Primary name | No |
| `type` | `EntityType` | Category | No |
| `description` | `String?` | Brief description | Yes |
| `aliases` | `List<String>` | Alternative names (JSON) | No |
| `verseReferences` | `List<Int>` | `BBCCCVVV` list (JSON) | No |

### 1.2 Relationship

```kotlin
data class Relationship(
    val id: Long,
    val sourceEntityId: Long,
    val targetEntityId: Long,
    val type: RelationshipType,
    val description: String?,
)

enum class RelationshipType { ParentOf, SiblingOf, SpouseOf, LocationOf, PartOf, SuccessorOf, Associated }
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `entities`

```sql
CREATE TABLE entities (
    id                INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name              TEXT    NOT NULL,
    type              TEXT    NOT NULL,
    description       TEXT,
    aliases           TEXT    NOT NULL DEFAULT '[]',
    verse_references  TEXT    NOT NULL DEFAULT '[]'
);
```

#### Table: `relationships`

```sql
CREATE TABLE relationships (
    id                INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    source_entity_id  INTEGER NOT NULL REFERENCES entities(id),
    target_entity_id  INTEGER NOT NULL REFERENCES entities(id),
    type              TEXT    NOT NULL,
    description       TEXT
);
```

#### Table: `entity_verse_index` (materialized join)

```sql
CREATE TABLE entity_verse_index (
    entity_id       INTEGER NOT NULL REFERENCES entities(id),
    global_verse_id INTEGER NOT NULL,
    PRIMARY KEY (entity_id, global_verse_id)
);
```

#### Indexes

```sql
CREATE INDEX idx_entities_type ON entities(type);
CREATE INDEX idx_entity_verse ON entity_verse_index(global_verse_id);
CREATE INDEX idx_relationships_source ON relationships(source_entity_id);
CREATE INDEX idx_relationships_target ON relationships(target_entity_id);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface KnowledgeGraphRepository {
    suspend fun getByType(type: EntityType): Result<List<Entity>>
    suspend fun getById(id: Long): Result<Entity?>
    suspend fun getRelationshipsFor(entityId: Long): Result<List<Relationship>>
    suspend fun getEntitiesForVerse(globalVerseId: Int): Result<List<Entity>>
    suspend fun search(query: String): Result<List<Entity>>
}
```

---

## 4. Key Queries

| Query | Parameters | Return | Performance |
|-------|-----------|--------|-------------|
| `entitiesByType` | `type` | `List<Entity>` | Indexed on type |
| `entitiesForVerse` | `globalVerseId` | `List<Entity>` | Via `entity_verse_index` |
| `relationshipsFor` | `entityId` | `List<Relationship>` | Indexed on source/target |
| `searchEntities` | FTS query | `List<Entity>` | FTS5 on name/description |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v9 → v10 | Created `entities`, `relationships`, `entity_verse_index` | `9.sqm` |

---

## 6. Relations with Other Modules

| External Table | Relation | Type |
|---------------|----------|------|
| `verses` | `entity_verse_index.global_verse_id` | Convention-based |
| `geographic_locations` | Places entities link to geo data | Cross-reference |
| `timeline_events` | Events entities link to timeline | Cross-reference |
