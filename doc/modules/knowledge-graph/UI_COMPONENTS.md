# Knowledge Graph — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("knowledge_graph") { config -> KnowledgeGraphPane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `knowledge_graph` |
| **Category** | Study |

---

## 2. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `KnowledgeGraphPane` | Main pane with category tabs + graph | No |
| `EntityBrowser` | Filterable entity list | No |
| `EntityDetail` | Entity attributes + verse refs + relationships | No |
| `RelationshipGraph` | Force-directed graph visualization | No |
| `EntityCard` | Compact entity summary | Yes |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [G] Knowledge Graph             [v] [x]  |
+------------------------------------------+
| [People] [Places] [Events]  [Search...]  |
+------------------------------------------+
| +- People --------+  +- Graph ---------+ |
| | [P] Abraham     |  |     Abraham     | |
| | [P] Moses       |  |    /   |   \    | |
| | [P] David       |  | Sarah Isaac Lot | |
| | [P] Paul        |  |        |        | |
| | [P] Peter       |  |      Jacob      | |
| +--+--------------+  +-----------------+ |
|    | Selected: Abraham                   |
|    | Father of Isaac, husband of Sarah   |
|    | Gen 12:1, Gen 15:1, Gen 22:1       |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** | Entity list only; graph in full-screen overlay |
| **Tablet** | List + graph side-by-side |
| **Desktop** | Workspace pane with resizable list/graph split |

---

## 5. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` | Shows entities mentioned in the verse |
| User taps verse reference | Publishes `VerseSelected(globalVerseId)` |

---

## 6. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Loading** | Shimmer cards | Initial load |
| **Content** | Entity list + graph | Data loaded |
| **Detail** | Entity detail panel | Entity selected |
| **Empty** | "No entities found" | Empty category/search |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Graph alt | Text description of relationships for screen readers |
| Entity list | Semantic labels: "Entity: [name], [type]" |
| Keyboard | Tab between entities; Enter for detail |
