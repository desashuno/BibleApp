# Bookmarks & History — UI Components

> Composables, PaneRegistry registration, and responsive behavior.

---

## 1. PaneRegistry Registration

```kotlin
PaneRegistry.register("bookmarks") { config -> BookmarksPane(config) }
```

| Field | Value |
|-------|-------|
| **Type key** | `bookmarks` |
| **Category** | Tools |

---

## 2. Key Composables

| Composable | Description | Reusable |
|------------|-------------|----------|
| `BookmarksPane` | Main pane with Bookmarks/History tabs | No |
| `BookmarkFolderList` | Expandable folder list | No |
| `BookmarkItem` | Single bookmark with verse ref + label | Yes |
| `HistoryList` | Recent navigation history | No |
| `FolderCreateDialog` | Create/rename folder dialog | No |

---

## 3. Descriptive Wireframe

```
+------------------------------------------+
| [B] Bookmarks & History         [v] [x]  |
+------------------------------------------+
| [Bookmarks]  [History]                   |
+------------------------------------------+
| v Study Bookmarks (3)                    |
|   [*] Gen 1:1 "In the beginning"        |
|   [*] John 1:1 "In the beginning..."    |
|   [*] Rom 8:28 "And we know that..."    |
| v Prayer List (2)                        |
|   [*] Phil 4:6 "Be careful for..."      |
|   [*] 1 Thess 5:17 "Pray without..."   |
| + New Folder                             |
+------------------------------------------+
```

---

## 4. Responsive Behavior

| Breakpoint | Behavior |
|-----------|----------|
| **Mobile** | Full-screen with tab navigation |
| **Tablet** | Side panel alongside reader |
| **Desktop** | Workspace pane |

---

## 5. Verse Bus Interaction

| Event | UI Action |
|-------|-----------|
| Receives `VerseSelected` | Checks if verse is bookmarked |
| User taps bookmark | Publishes `VerseSelected(globalVerseId)` |

---

## 6. UI States

| State | Visual | Trigger |
|-------|--------|---------|
| **Loading** | Shimmer | Initial load |
| **Content** | Folder list | Data loaded |
| **Empty** | "No bookmarks yet" | No bookmarks |

---

## 7. Accessibility

| Requirement | Implementation |
|-------------|----------------|
| Semantic | "Bookmark: [label], [verse reference]" |
| Keyboard | Tab between items; Enter to navigate |
| Folder state | Announces expanded/collapsed |
