# Knowledge Graph — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  KnowledgeGraphPane                               |
|  EntityBrowser / RelationshipGraph / EntityDetail  |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultKnowledgeGraphComponent (Decompose)       |
|  +-- Manages StateFlow<KnowledgeGraphState>       |
|  +-- Subscribes to VerseBus VerseSelected         |
|  +-- Queries entity/relationship data             |
+---------------------------------------------------+
|                      DATA                         |
|  KnowledgeGraphRepository (interface)             |
|  KnowledgeGraphRepositoryImpl                     |
|  +-- KnowledgeQueries (SQLDelight)                |
|       +-- SQLite (entities, relationships)        |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Browse Entities

1. **User opens pane** — Loads entity categories (People, Places, Events).
2. **Category selection** — Queries entities by type.
3. **Entity detail** — Shows attributes, related entities, and verse references.
4. **Graph view** — Renders interactive relationship graph (force-directed layout).

### 2.2 Secondary Flows

- **VerseBus integration** — On `VerseSelected`, shows entities mentioned in that verse.
- **Entity search** — Full-text search across entity names/descriptions.
- **Relationship navigation** — Click an edge to see related entities.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Knowledge.sq` | `entitiesByType` | `type: String` | `List<Entity>` | Filter by People/Places/Events |
| `Knowledge.sq` | `entityById` | `id: Long` | `Entity?` | Single entity detail |
| `Knowledge.sq` | `relationshipsFor` | `entityId: Long` | `List<Relationship>` | Entity connections |
| `Knowledge.sq` | `entitiesForVerse` | `globalVerseId: Int` | `List<Entity>` | Entities in a verse |
| `Knowledge.sq` | `searchEntities` | `query: String` | `List<Entity>` | FTS search |

---

## 4. Dependency Injection

```kotlin
val knowledgeGraphModule = module {
    singleOf(::KnowledgeGraphRepositoryImpl) bind KnowledgeGraphRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultKnowledgeGraphComponent(ctx, get(), get())
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `KnowledgeGraphRepositoryImpl` | Abstracts entity/relationship queries |
| Observer | VerseBus subscriber | Shows entities for selected verse |
| Graph layout | Force-directed algorithm | Automatic relationship visualization |

---

## 6. Performance Considerations

- **Entity query < 10 ms** — Indexed on `type` and `global_verse_id`.
- **Graph rendering** — Limited to 50 visible nodes; pagination for large clusters.
- **Lazy loading** — Entity details loaded on demand, not pre-fetched.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| SQLite storage | Neo4j/graph DB | KMP portability; sufficient for biblical entity graph |
| Force-directed layout | Hierarchical | More intuitive for non-directional relationships |
| Pre-populated data | User-created only | Biblical entities are static reference data |
