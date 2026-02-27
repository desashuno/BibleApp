# Sermon Editor — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `SermonEditorConfig.List` | — | Sermon list |
| `SermonEditorConfig.Edit(id)` | `sermonId: Long` | Edit specific sermon |
| `SermonEditorConfig.Create` | — | New sermon |

---

## 2. Consumed Configurations

| Target module | Config | Context |
|--------------|--------|---------|
| `bible-reader` | via VerseBus | Navigate to verse reference |

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://sermons` | — | Sermon list |
| `biblestudio://sermons/{id}` | `biblestudio://sermons/42` | Edit sermon |
| `biblestudio://sermons/new` | — | Create new sermon |

---

## 4. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `VerseSelected` (insert ref); publishes on ref tap |
