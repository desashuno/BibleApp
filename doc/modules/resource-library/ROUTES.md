# Resource Library — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `ResourceLibraryConfig.Main` | -- | Open resource library browser |
| `ResourceLibraryConfig.ForVerse(globalVerseId)` | `globalVerseId: Int` | Open entries for specific verse |
| `ResourceLibraryConfig.Resource(uuid)` | `uuid: String` | Open specific resource |

---

## 2. Pane Opening (Workspace)

```kotlin
PaneRegistry.build("resource_library", config = mapOf("verseId" to "01001001"))
```

---

## 3. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://resources/{verseId}` | `biblestudio://resources/01001001` | Opens resource entries for Gen 1:1 |

---

## 4. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Subscriber** | Subscribes to `VerseSelected` to load entries for new verse |

---

## 5. Inter-Module Communication Diagram

```
+--------------+  VerseSelected     +-----------------------+
| Bible Reader | -----------------> |   Resource Library    |
+--------------+                    +---+-------------------+
                                        |
                                        | ResourceRepository
                                        v
                                 +-----------------------+
                                 | resources /           |
                                 | resource_entries /    |
                                 | fts_resources         |
                                 +-----------------------+
```
