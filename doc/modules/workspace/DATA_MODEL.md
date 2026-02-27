# Workspace — Data Model

> Domain entities, SQLite schema, repositories, and queries.

---

## 1. Domain Entities

### 1.1 Workspace

```kotlin
data class Workspace(
    val uuid: String,
    val name: String,
    val isActive: Boolean,
    val createdAt: String,
)
```

| Field | Type | Description | Nullable |
|-------|------|-------------|----------|
| `uuid` | `String` | Unique identifier (UUID v4) | No |
| `name` | `String` | User-facing workspace name | No |
| `isActive` | `Boolean` | Currently active workspace flag | No |
| `createdAt` | `String` | ISO 8601 creation timestamp | No |

### 1.2 LayoutNode (sealed class)

```kotlin
sealed class LayoutNode {
    data class Split(val axis: SplitAxis, val ratio: Float, val first: LayoutNode, val second: LayoutNode) : LayoutNode()
    data class Leaf(val paneType: String, val config: Map<String, String> = emptyMap()) : LayoutNode()
    data class Tabs(val children: List<Leaf>, val activeIndex: Int) : LayoutNode()
}
enum class SplitAxis { Horizontal, Vertical }
```

---

## 2. SQLite Schema

### 2.1 Tables

#### Table: `workspaces`

```sql
CREATE TABLE workspaces (
    uuid       TEXT    NOT NULL PRIMARY KEY,
    name       TEXT    NOT NULL,
    is_active  INTEGER NOT NULL DEFAULT 0,
    created_at TEXT    NOT NULL DEFAULT (datetime('now'))
);
```

#### Table: `workspace_layouts`

```sql
CREATE TABLE workspace_layouts (
    id           INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    workspace_id TEXT    NOT NULL REFERENCES workspaces(uuid),
    layout_json  TEXT    NOT NULL,
    updated_at   TEXT    NOT NULL DEFAULT (datetime('now'))
);
```

---

## 3. Repositories

### 3.1 Interface

```kotlin
interface WorkspaceRepository {
    suspend fun getAll(): Result<List<Workspace>>
    suspend fun getActive(): Result<Workspace?>
    suspend fun create(name: String): Result<Workspace>
    suspend fun setActive(uuid: String): Result<Unit>
    suspend fun delete(uuid: String): Result<Unit>
    suspend fun getLayout(workspaceId: String): Result<LayoutNode?>
    suspend fun saveLayout(workspaceId: String, layout: LayoutNode): Result<Unit>
}
```

---

## 4. Key Queries

| Query | `.sq` File | Parameters | Return | Performance |
|-------|-----------|------------|--------|-------------|
| `activeWorkspace` | `Settings.sq` | — | `Workspace?` | O(1) |
| `allWorkspaces` | `Settings.sq` | — | `List<Workspace>` | Small set |
| `layoutForWorkspace` | `Settings.sq` | `workspaceId` | `WorkspaceLayout` | < 5 ms |
| `upsertLayout` | `Settings.sq` | `workspaceId`, `json` | — | Single upsert |

---

## 5. Migrations

| DB Version | Change | Migration file |
|-----------|--------|----------------|
| v8 → v9 | Created `workspaces` table | `8.sqm` |
| v13 → v14 | Created `workspace_layouts` table | `13.sqm` |

---

## 6. Relations with Other Modules

| External Reference | Relation | Type |
|-------------------|----------|------|
| PaneRegistry type keys | `layout_json` contains `paneType` strings | Convention-based |
| `settings` table | Global app settings sibling | Same `Settings.sq` group |
