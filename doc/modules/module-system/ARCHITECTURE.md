# Module System — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  ModuleSystemPane / ModuleBrowser                 |
|  ModuleDetail / InstallProgress                   |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultModuleSystemComponent (Decompose)         |
|  +-- Manages StateFlow<ModuleSystemState>         |
|  +-- Validates .bsmodule packages                 |
|  +-- Coordinates install/uninstall via Importer   |
+---------------------------------------------------+
|                      DATA                         |
|  ModuleRepository (interface)                     |
|  ModuleRepositoryImpl                             |
|  +-- File system (.bsmodule packages)             |
|  +-- BibleQueries / ResourceQueries (SQLDelight)  |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Install Module

1. **User selects `.bsmodule` file** — File picker or drag-and-drop.
2. **Validation** — `ModuleValidator` checks ZIP structure, manifest, content integrity.
3. **Extraction** — Contents extracted to temp directory.
4. **Import** — `ModuleImporter` inserts data into SQLite tables within a transaction.
5. **FTS rebuild** — Affected FTS5 indexes rebuilt.
6. **State update** — Module appears in installed list.

### 2.2 Secondary Flows

- **Browse** — Lists installed modules from DB metadata.
- **Uninstall** — Removes all data for a module in a transaction.
- **Version check** — Compares installed vs manifest version.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Bible.sq` | `allBibles` | — | `List<Bible>` | Installed Bibles |
| `Bible.sq` | `insertBible` | all fields | — | Insert Bible module |
| `Resource.sq` | `allResources` | — | `List<Resource>` | Installed resources |
| `Resource.sq` | `insertResource` | all fields | — | Insert resource |

---

## 4. Dependency Injection

```kotlin
val moduleSystemModule = module {
    singleOf(::ModuleRepositoryImpl) bind ModuleRepository::class
    singleOf(::ModuleValidator)
    singleOf(::ModuleImporter)
    factory { (ctx: ComponentContext) ->
        DefaultModuleSystemComponent(ctx, get(), get(), get())
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `ModuleRepositoryImpl` | Abstracts module metadata |
| Strategy | `ModuleImporter` | Different import per content type |
| Validator | `ModuleValidator` | Input sanitization |
| Transaction | Import pipeline | Atomicity on failure |

---

## 6. Performance Considerations

- **Bible import < 30 s** — Batch inserts via `database.transaction {}`.
- **FTS rebuild** — Once after full import, not per-row.
- **Progress reporting** — Emits percentage via StateFlow.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| `.bsmodule` ZIP format | SQLite per module | ZIP allows validation; single DB simplifies queries |
| Bundled seed data | Download on first launch | No network dependency |
| Transaction-based install | Row-by-row | Atomicity — failed import leaves no partial data |
