# Workspace — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
┌───────────────────────────────────────────────────┐
│                       UI                          │
│  WorkspaceShell / LayoutNodeRenderer              │
│  PaneContainer / SplitPane / TabGroupPane         │
│  └── Observes WorkspaceComponent.state (StateFlow)│
├───────────────────────────────────────────────────┤
│                     LOGIC                         │
│  DefaultWorkspaceComponent (Decompose)            │
│  ├── Manages StateFlow<WorkspaceState>            │
│  ├── Builds / mutates LayoutNode tree             │
│  └── Calls WorkspaceRepository for persistence    │
│                                                   │
│  PaneRegistry                                     │
│  ├── Maps type keys → @Composable builders        │
│  └── Provides PaneMetadata for activity bar       │
├───────────────────────────────────────────────────┤
│                      DATA                         │
│  WorkspaceRepository (interface)                  │
│  WorkspaceRepositoryImpl                          │
│  └── SettingsQueries (SQLDelight)                 │
│       └── SQLite (workspaces, workspace_layouts)  │
└───────────────────────────────────────────────────┘
```

---

## 2. Internal Data Flow

```
User opens/switches workspace
  → DefaultWorkspaceComponent.loadWorkspace(id)
    → repository.getWorkspace(id) → Workspace + JSON layout
      → LayoutNodeDto deserialized → LayoutNode tree
    → _state.update { it.copy(layout = tree) }
  → WorkspaceShell recomposes via StateFlow
    → LayoutNodeRenderer recursively renders tree
      → PaneRegistry.Build(type) for each Leaf
```

### 2.1 Primary Flow — Layout Rendering

1. **Workspace loads** — `WorkspaceComponent` reads serialized layout from `workspace_layouts`.
2. **Tree built** — JSON deserializes to `LayoutNode` sealed class tree.
3. **Recursive render** — `LayoutNodeRenderer` renders `Split`, `Tabs`, or `Leaf` nodes.
4. **Pane instantiation** — Each `Leaf` calls `PaneRegistry.Build(type)` which invokes the registered composable.

### 2.2 Secondary Flows

- **Add pane** — User picks a module → `addPane(type)` inserts a new `Leaf` into the tree → layout re-renders.
- **Resize split** — User drags divider → `resizeSplit(path, newRatio)` mutates the `Split.ratio` → immediate visual update.
- **Close pane** — User clicks ✕ → `removePane(type)` prunes the `Leaf` from the tree → remaining panes fill space.
- **Apply preset** — User selects a quickstart layout → `applyPreset(preset)` replaces entire tree.
- **Save** — `saveWorkspace()` serializes current `LayoutNode` to JSON → stored in `workspace_layouts`.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Settings.sq` | `activeWorkspace` | — | `Workspace` | Gets the currently active workspace |
| `Settings.sq` | `workspaceById` | `uuid` | `Workspace` | Loads specific workspace |
| `Settings.sq` | `allWorkspaces` | — | `List<Workspace>` | Lists saved workspaces |
| `Settings.sq` | `layoutForWorkspace` | `workspaceId` | `WorkspaceLayout` | JSON layout for workspace |
| `Settings.sq` | `upsertLayout` | `workspaceId`, `json` | — | Save/update layout |
| `Settings.sq` | `insertWorkspace` | `uuid`, `name` | — | Create new workspace |

---

## 4. Dependency Injection

```kotlin
val workspaceModule = module {
    singleOf(::WorkspaceRepositoryImpl) bind WorkspaceRepository::class
    single { PaneRegistry().apply { init() } }
    factory { (ctx: ComponentContext) ->
        DefaultWorkspaceComponent(
            componentContext = ctx,
            repository = get(),
            paneRegistry = get(),
        )
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `WorkspaceRepositoryImpl` | Abstracts workspace persistence |
| Sealed class tree | `LayoutNode` | Type-safe recursive layout representation |
| Visitor (recursive rendering) | `LayoutNodeRenderer` | Each node type rendered differently |
| Preset / Template | `WorkspacePreset` | Quick-start layouts for new users |
| JSON serialization | `LayoutNodeDto` ↔ `LayoutNode` | Persistent storage in SQLite TEXT column |

---

## 6. Performance Considerations

- **Layout load < 5 ms** — Single JSON column read + deserialization.
- **Lazy pane initialization** — Only visible `Leaf` nodes instantiate their Compose subtree; tab groups only compose the active tab.
- **Debounced resize** — Split divider drag events are throttled to avoid excessive DB writes during interaction.
- **Auto-save** — Layout is persisted on a 2-second debounce after the last mutation, not on every change.

---

## 7. Design Decisions

| Decision | Alternatives considered | Justification |
|----------|------------------------|---------------|
| JSON-serialized layout in SQLite | Normalized relational tables | Tree structure maps naturally to JSON; simpler queries; < 5 KB typical payload |
| Sealed class `LayoutNode` | Map/dictionary tree | Full type safety; exhaustive `when` matching |
| PaneRegistry singleton | Direct module imports | Eliminates inter-module coupling; runtime extensibility |
| Workspace presets | Empty default | Provides immediate value to new users |
