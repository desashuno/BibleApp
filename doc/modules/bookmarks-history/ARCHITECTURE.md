# Bookmarks & History — Architecture

> Internal architecture, layers, data flow, and system integration.

---

## 1. Layer Diagram

```
+---------------------------------------------------+
|                       UI                          |
|  BookmarksPane / HistoryTab                       |
|  BookmarkFolderList / BookmarkItem                |
+---------------------------------------------------+
|                     LOGIC                         |
|  DefaultBookmarksComponent (Decompose)            |
|  +-- Manages StateFlow<BookmarksState>            |
|  +-- VerseBus subscriber                          |
|  +-- Calls BookmarkRepository                     |
+---------------------------------------------------+
|                      DATA                         |
|  BookmarkRepository (interface)                   |
|  BookmarkRepositoryImpl                           |
|  +-- AnnotationQueries (SQLDelight)               |
|       +-- SQLite (bookmarks)                      |
+---------------------------------------------------+
```

---

## 2. Internal Data Flow

### 2.1 Primary Flow — Create Bookmark

1. **User bookmarks verse** — Via context menu or bookmark button.
2. **Folder selection** — User picks a folder (or creates new).
3. **Repository** — `BookmarkRepository.create()` persists bookmark.
4. **State update** — Bookmark list refreshes.

### 2.2 Secondary Flows

- **History tracking** — Navigation events auto-recorded (last 100).
- **Folder management** — Create, rename, reorder folders.
- **Quick navigate** — Tap bookmark to publish VerseBus event.

---

## 3. SQLDelight Query Integration

| `.sq` File | Query | Parameters | Description |
|-----------|-------|------------|-------------|
| `Annotation.sq` | `bookmarksInFolder` | `folderId` | Bookmarks in folder |
| `Annotation.sq` | `allBookmarks` | — | All bookmarks |
| `Annotation.sq` | `insertBookmark` | all fields | Create bookmark |
| `Annotation.sq` | `deleteBookmark` | `uuid` | Soft delete |

---

## 4. Dependency Injection

```kotlin
val bookmarksModule = module {
    singleOf(::BookmarkRepositoryImpl) bind BookmarkRepository::class
    factory { (ctx: ComponentContext) ->
        DefaultBookmarksComponent(ctx, get(), get())
    }
}
```

---

## 5. Patterns Applied

| Pattern | Where | Why |
|---------|-------|-----|
| Repository | `BookmarkRepositoryImpl` | Abstracts bookmark queries |
| Observer | VerseBus subscriber | Auto-check bookmark status for active verse |
| Folder hierarchy | `folder_id` + `sort_order` | User-organized bookmark groups |

---

## 6. Performance Considerations

- **Bookmark lookup < 5 ms** — Indexed on `global_verse_id` and `folder_id`.
- **History auto-prune** — Keeps last 100 entries; auto-deletes older.

---

## 7. Design Decisions

| Decision | Alternatives | Justification |
|----------|-------------|---------------|
| Flat folder structure | Nested folders | Simpler UX; sufficient for Bible study |
| Auto-history | Manual history | Reduces user friction; always available |
| UUID primary key | Auto-increment | Sync-safe |
