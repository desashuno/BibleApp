# {Module Name} — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

<!-- Data flow within the module following the project's layered architecture. -->

```
┌──────────────────────────────────────────────┐
│                     UI                        │
│  {Module}Pane / {Module}Content (@Composable) │
│  └── Observes Component.state (StateFlow)    │
├──────────────────────────────────────────────┤
│                   LOGIC                       │
│  Default{Module}Component (Decompose)        │
│  ├── Manages StateFlow<{Module}State>        │
│  └── Calls Repository methods                │
├──────────────────────────────────────────────┤
│                    DATA                       │
│  {Module}Repository (interface)              │
│  {Module}RepositoryImpl                      │
│  └── SQLDelight generated Queries            │
│       └── SQLite (tables: ...)               │
└──────────────────────────────────────────────┘
```

---

## 2. Internal Data Flow

<!-- Typical sequence of a user action within this module. -->

```
User interacts with Composable UI
  → Component method called (e.g. onLoad())
    → coroutineScope.launch { repository.getXxx() }
      → SQLDelight query executes
    → _state.update { it.copy(...) }
  → Composable recomposes via StateFlow collection
```

### 2.1 Primary Flow

<!-- Describe the main user action flow of the module. -->

1. **{User action}** — {description}
2. **{Processing}** — {description}
3. **{Result}** — {description}

### 2.2 Secondary Flows

<!-- Describe alternative or secondary flows. -->

---

## 3. SQLDelight Query Integration

<!-- List the SQLDelight query group and key queries this module uses. -->

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `{Group}.sq` | `{queryName}` | `{params}` | `{type}` | {description} |

---

## 4. Dependency Injection

<!-- How the module's dependencies are registered and resolved via Koin. -->

```kotlin
// val {module}Module = module {
//     singleOf(::Default{Module}RepositoryImpl) bind {Module}Repository::class
//     factory { (ctx: ComponentContext) ->
//         Default{Module}Component(ctx, get(), get())
//     }
// }
```

---

## 5. Patterns Applied

<!-- Design patterns specific to this module. -->

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | Data layer | Abstracts SQLDelight queries behind interface |
| Component (Decompose) | Logic layer | Lifecycle-aware state management |
| StateFlow | Component → UI | Reactive unidirectional data flow |

---

## 6. Performance Considerations

<!-- Optimizations, lazy loading, caching, pagination, etc. -->

---

## 7. Design Decisions

<!-- ADRs (Architecture Decision Records) relevant to this module. -->

| Decision | Alternatives considered | Justification |
|----------|------------------------|---------------|
| {decision} | {alternatives} | {why this was chosen} |
