# Note Editor — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `NoteEditorConfig.Main` | -- | Open empty; waits for VerseBus |
| `NoteEditorConfig.ForVerse(globalVerseId)` | `globalVerseId: Int` | Open notes for specific verse |
| `NoteEditorConfig.Edit(uuid)` | `uuid: String` | Open specific note by UUID |

---

## 2. Pane Opening (Workspace)

```kotlin
PaneRegistry.build("note_editor", config = mapOf("verseId" to "01001001"))
```

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://notes/{verseId}` | `biblestudio://notes/01001001` | Opens notes for Gen 1:1 |
| `biblestudio://note/{uuid}` | `biblestudio://note/abc-123` | Opens specific note |

---

## 4. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Subscriber** | Subscribes to `VerseSelected` to load notes for new verse |

---

## 5. Inter-Module Communication Diagram

```
+------------------+  VerseSelected     +------------------+
|  Bible Reader    | -----------------> |   Note Editor    |
+------------------+                    +------+-----------+
                                               |
                                               | NoteRepository
                                               v
                                        +------------------+
                                        | notes / fts_notes|
                                        +------------------+
```
