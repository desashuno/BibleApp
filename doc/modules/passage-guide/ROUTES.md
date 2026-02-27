# Passage Guide — Routes & Navigation

> Inter-module communication via Decompose navigation, Verse Bus, and deep links.

---

## 1. Exposed Configurations

| Config | Parameters | Description |
|--------|-----------|-------------|
| `PassageGuideConfig.Main` | -- | Open empty; waits for VerseBus |
| `PassageGuideConfig.ForVerse(globalVerseId)` | `globalVerseId: Int` | Open with pre-loaded report |

---

## 2. Consumed Configurations

| Target module | Config | Parameters sent | Context |
|--------------|--------|----------------|---------|
| `bible-reader` | via VerseBus `VerseSelected` | `globalVerseId` | When user taps a cross-reference |
| `word-study` | via VerseBus `StrongsSelected` | `strongsNumber` | When user taps a key word |

---

## 3. Pane Opening (Workspace)

```kotlin
PaneRegistry.build("passage_guide", config = mapOf("verseId" to "01001001"))
```

---

## 4. Deep Links

| Deep Link | Example | Resolution |
|-----------|---------|-----------|
| `biblestudio://passage/{verseId}` | `biblestudio://passage/01001001` | Opens passage guide for Gen 1:1 |

---

## 5. Verse Bus (LinkEvent)

| Role | Description |
|------|-------------|
| **Both** | Subscribes to `VerseSelected`; publishes `VerseSelected` and `StrongsSelected` on item taps |

---

## 6. Inter-Module Communication Diagram

```
┌──────────────┐  VerseSelected     ┌──────────────────┐
│ Bible Reader │ ──────────────────> │  Passage Guide   │
└──────────────┘                     └──────┬───────────┘
       ^                                    |
       | VerseSelected (cross-ref tap)      | reads from:
       +------------------------------------+ CrossRefRepo
                                            | ResourceRepo
       ┌──────────────┐  StrongsSelected    | NoteRepo
       │  Word Study  │ <──────────────────-+ MorphologyRepo
       └──────────────┘   (word tap)        | WordStudyRepo
```
