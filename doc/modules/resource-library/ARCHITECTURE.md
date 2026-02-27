# Resource Library — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  ResourceLibraryPane (@Composable)                |
|  +-- ResourceList, EntryViewer                    |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultResourceLibraryComponent (Decompose)      |
|  +-- Manages StateFlow<ResourceLibraryState>      |
|  +-- Subscribes to VerseBus VerseSelected         |
|  +-- Calls ResourceRepository methods             |
+---------------------------------------------------+
|                      DATA                         |
|  ResourceRepository (interface)                   |
|  ResourceRepositoryImpl                           |
|  +-- ResourceQueries (SQLDelight)                 |
|       +-- SQLite (resources, resource_entries,    |
|           fts_resources)                          |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Load Commentary for Verse

1. **VerseBus event** -- `VerseSelected(globalVerseId)` received.
2. **Query** -- `ResourceRepository.getEntriesForVerse(verseId)` fetches all entries across all resources.
3. **Group by resource** -- Entries grouped by `resource_id` for display.
4. **State updates** -- `ResourceLibraryState.entries` populated.
5. **UI renders** -- Sections per resource with entry content.

### 2.2 Secondary Flows

- **Resource import** -- User downloads/imports a resource package -> metadata + entries inserted.
- **Resource selection** -- User selects specific resource from dropdown -> entries filtered.

---

## 3. Dependency Injection

```kotlin
val resourceLibraryModule = module {
    singleOf(::ResourceRepositoryImpl) bind ResourceRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultResourceLibraryComponent(
            componentContext = ctx,
            repository = get(),
            verseBus = get(),
        )
    }
}
```

---

## 4. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `ResourceRepositoryImpl` | Abstracts resource CRUD + FTS |
| Observer (VerseBus) | VerseSelected subscription | Auto-loads on verse change |
| Group-by | Component groups entries by resource | Multiple resources shown per verse |

---

## 5. Performance Considerations

- **Resource import**: Bulk insert with transaction wrapping for large datasets (10K+ entries).
- **Entry lookup**: Indexed by `global_verse_id`; typical verse has 1-5 entries per resource.
- **FTS5 sync**: Trigger-based; no application maintenance needed.

---

## 6. Design Decisions

| Decision | Alternatives considered | Justification |
|----------|------------------------|---------------|
| UUID PK for resources | Auto-increment | Enables resource sharing/sync across devices |
| Per-verse entries | Per-chapter entries | Granular lookup; aligns with BBCCCVVV system |
| Separate `resources` and `resource_entries` tables | Single table | Clean metadata separation; supports multiple resources |
