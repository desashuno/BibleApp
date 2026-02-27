# Dashboard — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `DashboardConfig.Main` | — | Dashboard home |

---

## 2. Consumed Configurations

| Target module | Config | Context |
|--------------|--------|---------|
| `bible-reader` | via VerseBus | Verse of the day / bookmark tap |
| `reading-plans` | `ReadingPlansConfig.Main` | Open reading plan widget CTA |
| `note-editor` | `NoteEditorConfig.Edit(id)` | Open recent note |

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://dashboard` | — | Opens dashboard |

---

## 4. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Publisher** | Publishes `VerseSelected` / `PassageSelected` from widget taps |
