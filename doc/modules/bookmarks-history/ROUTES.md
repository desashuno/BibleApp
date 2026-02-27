# Bookmarks & History — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `BookmarksConfig.Main` | — | Bookmark manager (bookmarks tab) |
| `BookmarksConfig.History` | — | History tab |
| `BookmarksConfig.Folder(id)` | `folderId: String` | Open specific folder |

---

## 2. Consumed Configurations

| Target module | Config | Context |
|--------------|--------|---------|
| `bible-reader` | via VerseBus | Navigate to bookmarked verse |

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://bookmarks` | — | Opens bookmark manager |
| `biblestudio://history` | — | Opens history tab |

---

## 4. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `VerseSelected` (history + status); publishes on bookmark tap |
