# Sermon Editor — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  SermonEditorPane                                 |
|  SermonList / SermonEditView / SectionEditor      |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultSermonEditorComponent (Decompose)         |
|  +-- Manages StateFlow<SermonEditorState>         |
|  +-- Auto-save with debounce                      |
|  +-- Calls SermonRepository                       |
+---------------------------------------------------+
|                      DATA                         |
|  SermonRepository (interface)                     |
|  SermonRepositoryImpl                             |
|  +-- WritingQueries (SQLDelight)                  |
|       +-- SQLite (sermons, sermon_sections)       |
|       +-- fts_sermons (FTS5)                      |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Edit Sermon

1. **User creates or opens sermon** — Loads from `SermonRepository`.
2. **Section editing** — User types in structured sections (intro, points, conclusion).
3. **Auto-save** — Debounced (1.5s after last keystroke) persist to SQLite.
4. **FTS update** — Rebuilds `fts_sermons` index on save.

### 2.2 Secondary Flows

- **Sermon list** — Browse all sermons with FTS search.
- **Verse insertion** — Insert verse references that become VerseBus links.
- **Export** — Markdown or plain text export.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Return | Description |
|-----------|-------|------------|--------|-------------|
| `Writing.sq` | `allSermons` | — | `List<Sermon>` | All sermons |
| `Writing.sq` | `sermonById` | `id: Long` | `Sermon?` | Single sermon |
| `Writing.sq` | `sectionsForSermon` | `sermonId: Long` | `List<SermonSection>` | Sermon's sections |
| `Writing.sq` | `insertSermon` | all fields | — | Create sermon |
| `Writing.sq` | `updateSermon` | all fields | — | Update sermon |
| `Writing.sq` | `searchSermons` | FTS query | `List<Sermon>` | FTS5 search |

---

## 4. Dependency Injection

```kotlin
val sermonEditorModule = module {
    singleOf(::SermonRepositoryImpl) bind SermonRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultSermonEditorComponent(ctx, get(), get())
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `SermonRepositoryImpl` | Abstracts sermon/section queries |
| Auto-save | Debounced state persist | No manual save; prevents data loss |
| Structured content | Section-based editing | Sermon outline structure |

---

## 6. Performance Considerations

- **Auto-save < 50 ms** — Single transaction update.
- **FTS rebuild** — Per-sermon (not global) on save.
- **Sermon load < 10 ms** — Small document + sections in one query.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| Section-based editor | Free-form rich text | Structured for sermon preparation |
| Markdown support | WYSIWYG only | Simpler storage; export-friendly |
| Auto-save with debounce | Manual save | Better UX; matches modern editors |
